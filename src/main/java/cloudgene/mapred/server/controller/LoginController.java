package cloudgene.mapred.server.controller;

import jakarta.validation.Valid;

import org.reactivestreams.Publisher;

import cloudgene.mapred.server.auth.DatabaseAuthenticationProvider;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.event.LoginFailedEvent;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.token.bearer.AccessRefreshTokenLoginHandler;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;

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
