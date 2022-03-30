package cloudgene.mapred.api.v2.server;

import java.io.IOException;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import cloudgene.mapred.core.User;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;

@Controller
public class CloudgeneApps {

	@Get("/api/v2/server/cloudgene-apps")
	@Secured(User.ROLE_ADMIN)
	public String list() throws ResourceException, IOException {

		ClientResource clientResource = new ClientResource("http://apps.cloudgene.io/api/apps.json");
		return clientResource.get().getText();

	}

}
