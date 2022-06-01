package cloudgene.mapred.server.controller;

import cloudgene.mapred.server.auth.DatabaseAuthenticationProvider;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.event.LoginFailedEvent;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.token.jwt.bearer.AccessRefreshTokenLoginHandler;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import javax.validation.Valid;

@Controller
public class LoginController {

	@Inject
	protected AccessRefreshTokenLoginHandler loginHandler;
	
	@Inject
	protected ApplicationEventPublisher eventPublisher;
	
	@Inject
	protected DatabaseAuthenticationProvider authenticator;

	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON })
	@Post("/login")
	@SingleResult
	public Publisher<MutableHttpResponse<?>> login(@Valid @Body UsernamePasswordCredentials usernamePasswordCredentials,
			HttpRequest<?> request) {

		return Flux.from(authenticator.authenticate(request, usernamePasswordCredentials))
				.map(authenticationResponse -> {
					if (authenticationResponse.isAuthenticated()
							&& authenticationResponse.getAuthentication().isPresent()) {
						Authentication authentication = authenticationResponse.getAuthentication().get();
						eventPublisher.publishEvent(new LoginSuccessfulEvent(authentication));
						return loginHandler.loginSuccess(authentication, request);
					} else {
						eventPublisher.publishEvent(new LoginFailedEvent(authenticationResponse));
						return loginHandler.loginFailed(authenticationResponse, request);
					}
				}).defaultIfEmpty(HttpResponse.status(HttpStatus.UNAUTHORIZED));
	}

}
