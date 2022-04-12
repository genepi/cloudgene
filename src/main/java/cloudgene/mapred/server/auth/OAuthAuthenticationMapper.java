package cloudgene.mapred.server.auth;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.services.UserService;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.MailUtil;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.config.AuthenticationModeConfiguration;
import io.micronaut.security.oauth2.configuration.OpenIdAdditionalClaimsConfiguration;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.DefaultOpenIdAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdClaims;
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdTokenResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Replaces(DefaultOpenIdAuthenticationMapper.class)
public class OAuthAuthenticationMapper extends DefaultOpenIdAuthenticationMapper {

	private static final String MESSAGE_LOGIN_FAILED = "Login Failed! Wrong Username or Password.";

	private static final String MESSAGE_ACCOUNT_IS_INACTIVE = "Login Failed! User account is not activated.";

	private static final String MESSAGE_ACCOUNT_LOCKED = "The user account is locked for %d minutes. Too many failed logins.";

	public static final int MAX_LOGIN_ATTEMMPTS = 5;

	public static final int LOCKING_TIME_MIN = 30;

	@Inject
	protected Application application;

	public OAuthAuthenticationMapper(OpenIdAdditionalClaimsConfiguration openIdAdditionalClaimsConfiguration,
			AuthenticationModeConfiguration authenticationModeConfiguration) {
		super(openIdAdditionalClaimsConfiguration, authenticationModeConfiguration);
	}

	@Override
	@NonNull
	public AuthenticationResponse createAuthenticationResponse(String providerName, OpenIdTokenResponse tokenResponse,
			OpenIdClaims openIdClaims, @Nullable State state) {

		String email = openIdClaims.getEmail();

		UserDao dao = new UserDao(application.getDatabase());
		User user = dao.findByMail(email);
		
		if (user != null) {

			if (!user.isActive()) {
				throw AuthenticationResponse.exception(MESSAGE_ACCOUNT_IS_INACTIVE);
			}

			if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMMPTS) {
				if (user.getLockedUntil() == null || user.getLockedUntil().after(new Date())) {

					throw AuthenticationResponse.exception(String.format(MESSAGE_ACCOUNT_LOCKED, LOCKING_TIME_MIN));

				} else {
					// penalty time is over. set to zero
					user.setLoginAttempts(0);
				}
			}

			user.setLoginAttempts(0);
			user.setLastLogin(new Date());
			dao.update(user);

			return AuthenticationResponse.success(user.getUsername(), Arrays.asList(user.getRoles()));

		} else {

			user = new User();
			user.setUsername(email);
			user.setFullName(openIdClaims.getName());
			user.setMail(email);
			user.setRoles(new String[] { UserService.DEFAULT_ROLE });
			user.setPassword(HashUtil.hashPassword(RandomStringUtils.randomAlphanumeric(30)));
			dao.insert(user);
			
			return AuthenticationResponse.success(user.getUsername(), Arrays.asList(user.getRoles()));

		}

	}
}