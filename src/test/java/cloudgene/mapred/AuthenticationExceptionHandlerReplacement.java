package cloudgene.mapred;

import static io.micronaut.http.HttpHeaders.WWW_AUTHENTICATE;
import static io.micronaut.http.HttpStatus.UNAUTHORIZED;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthenticationExceptionHandler;
import jakarta.inject.Singleton;


@Singleton
@Replaces(AuthenticationExceptionHandler.class) 
public class AuthenticationExceptionHandlerReplacement extends AuthenticationExceptionHandler {

	public AuthenticationExceptionHandlerReplacement(ApplicationEventPublisher eventPublisher) {
		super(eventPublisher);
	}

	@Override
	public MutableHttpResponse<?> handle(HttpRequest request, AuthenticationException e) {
	       	    	
	        return HttpResponse.status(UNAUTHORIZED).body("{message: \"" + e.getMessage() + "\"}")
	                .header(WWW_AUTHENTICATE, "Basic realm=\"Cloudgene\"");
	}
	
 
}