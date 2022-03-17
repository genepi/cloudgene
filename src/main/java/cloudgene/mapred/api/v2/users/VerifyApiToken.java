package cloudgene.mapred.api.v2.users;

import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.ApiToken;
import cloudgene.mapred.util.BaseResource;

public class VerifyApiToken extends BaseResource {

	@Post
	public Representation verifyApiKey(Representation entity) {

		Form form = new Form(entity);

		String token = form.getFirstValue("token", "");
		JSONObject result = ApiToken.verify(token, getSettings().getSecretKey(), getDatabase());

		return new StringRepresentation(result.toString());

	}

}
