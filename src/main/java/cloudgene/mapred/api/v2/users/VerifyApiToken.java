package cloudgene.mapred.api.v2.users;

import org.json.JSONObject;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cloudgene.mapred.Application;
import cloudgene.mapred.core.User;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.validator.JwtTokenValidator;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

@Controller
public class VerifyApiToken {

	@Inject
	protected Application application;

	@Inject
	protected JwtTokenValidator validator;

	@Post(uri = "/api/v2/tokens/verify", consumes = MediaType.APPLICATION_FORM_URLENCODED)
	@Secured(SecurityRule.IS_ANONYMOUS)
	public Publisher<String> verifyApiKey(String token) {

		Publisher<Authentication> authentication = validator.validateToken(token, null);

		return Mono.<String>create(emitter -> {
			authentication.subscribe(new Subscriber<Authentication>() {

				private Subscription s;

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub

				}

				@Override
				public void onError(Throwable arg0) {
					emitter.error(arg0);
				}

				@Override
				public void onNext(Authentication arg0) {

					JSONObject result = new JSONObject(arg0.getAttributes());
					User user = application.getUserByAuthentication(arg0);
					if (user == null) {
						result.put("valid", false);
						result.put("message", "Invalid Usern mae in API Token.");
					} else {
						result.put("valid", true);
						result.put("message", "API Token was created by " + user.getUsername() + " and is valid.");
					}
					emitter.success(result.toString());
					s.request(1);
				}

				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					s.request(1);
				}

			});
		});

	}

}
