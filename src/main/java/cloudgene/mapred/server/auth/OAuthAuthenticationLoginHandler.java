package cloudgene.mapred.server.auth;

import java.util.Optional;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.handlers.RedirectingLoginHandler;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OAuthAuthenticationLoginHandler implements RedirectingLoginHandler<HttpRequest<?>, MutableHttpResponse<?>> {

	public static int LIFETIME = 60 * 60 * 24;

	@Inject
	protected JwtTokenGenerator generator;

	@Override
	public MutableHttpResponse<String> loginSuccess(Authentication authentication, HttpRequest<?> request) {
		Optional<String> token = generator.generateToken(authentication, LIFETIME);

		String html = "<!DOCTYPE html>\n" + "<html><script>"
				+ "          localStorage.setItem('cloudgene',  JSON.stringify({token: '" + token.get() + "'}));\n"
				+ "\n" + "          var redirect = '/';\n" + "          window.location = redirect;"
				+ "</script></html>";

		return HttpResponse.ok(html).contentType(MediaType.TEXT_HTML);
	}

	@Override
	public MutableHttpResponse<?> loginRefresh(Authentication authentication, String refreshToken,
			HttpRequest<?> request) {
		return HttpResponse.badRequest();
	}

	@Override
	public MutableHttpResponse<?> loginFailed(AuthenticationResponse authenticationResponse, HttpRequest<?> request) {
		return HttpResponse.unauthorized();
	}

}
