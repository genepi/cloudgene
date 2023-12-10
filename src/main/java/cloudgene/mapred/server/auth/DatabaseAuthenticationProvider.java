package cloudgene.mapred.server.auth;

import java.util.Arrays;
import java.util.Date;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.HashUtil;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class DatabaseAuthenticationProvider implements AuthenticationProvider<HttpRequest<?>> {

	private static Logger log = LoggerFactory.getLogger(DatabaseAuthenticationProvider.class);
	
	private static final String MESSAGE_LOGIN_FAILED = "Login Failed! Wrong Username or Password.";

	private static final String MESSAGE_ACCOUNT_IS_INACTIVE = "Login Failed! User account is not activated.";

	private static final String MESSAGE_ACCOUNT_LOCKED = "The user account is locked for %d minutes. Too many failed logins.";

	public static final int MAX_LOGIN_ATTEMPTS = 5;

	public static final int LOCKING_TIME_MIN = 30;

	@Inject
	protected Application application;

	@Override
	public Publisher<AuthenticationResponse> authenticate(HttpRequest<?> httpRequest,
			AuthenticationRequest<?, ?> authenticationRequest) {

		return Mono.<AuthenticationResponse>create(emitter -> {

			String loginUsername = authenticationRequest.getIdentity().toString();
			String loginPassword = authenticationRequest.getSecret().toString();

			UserDao dao = new UserDao(application.getDatabase());
			User user = dao.findByUsername(loginUsername);

			if (user != null) {

				if (!user.isActive()) {

					log.info(String.format(
							"Authorization failure: User account is not activated for account %s (ID %s - email %s)",
							user.getUsername(), user.getId(), user.getMail()));

					emitter.error(AuthenticationResponse.exception(MESSAGE_ACCOUNT_IS_INACTIVE));
					return;
				}

				if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
					if (user.getLockedUntil() == null || user.getLockedUntil().after(new Date())) {

						log.info(String.format(
								"Authorization failure: login retries are currently locked for account %s (ID %s - email %s)",
								user.getUsername(), user.getId(), user.getMail()));

						emitter.error(AuthenticationResponse
								.exception(String.format(MESSAGE_ACCOUNT_LOCKED, LOCKING_TIME_MIN)));
						return;

					} else {
						// penalty time is over. set to zero
						log.info(String.format(
								"Authorization: Account login lock has expired; releasing for account %s (ID %s - email %s)",
								user.getUsername(), user.getId(), user.getMail()));
						user.setLoginAttempts(0);
					}
				}

				if (HashUtil.checkPassword(loginPassword, user.getPassword())) {

					user.setLoginAttempts(0);
					user.setLastLogin(new Date());
					dao.update(user);

					String message = String.format("Authorization success: user login %s (ID %s - email %s)",
							user.getUsername(), user.getId(), user.getMail());
					if (user.isAdmin()) {
						// Note: Admin user logins are called out explicitly, to aid log analysis in the
						// event of a breach
						message += " (ADMIN)";
					}
					log.info(message);

					emitter.success(AuthenticationResponse.success(user.getUsername(), Arrays.asList(user.getRoles())));

				} else {

					// count failed logins
					int attempts = user.getLoginAttempts();
					attempts++;
					user.setLoginAttempts(attempts);

					// too many, lock user
					if (attempts >= MAX_LOGIN_ATTEMPTS) {

						log.warn(String.format(
								"Authorization failure: User account %s (ID %s - email %s) locked due to too many failed logins",
								user.getUsername(), user.getId(), user.getMail()));

						user.setLockedUntil(new Date(System.currentTimeMillis() + (LOCKING_TIME_MIN * 60 * 1000)));
					}
					dao.update(user);

					log.warn(String.format("Authorization failure: Invalid password for username: %s", loginUsername));

					emitter.error(AuthenticationResponse.exception(MESSAGE_LOGIN_FAILED));
					return;
				}
			} else {
				log.warn(String.format("Authorization failure: unknown username: %s", loginUsername));
				emitter.error(AuthenticationResponse.exception(MESSAGE_LOGIN_FAILED));
				return;
			}

		});
	}
}