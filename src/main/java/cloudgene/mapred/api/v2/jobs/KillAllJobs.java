package cloudgene.mapred.api.v2.jobs;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HadoopUtil;

public class KillAllJobs extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {
			return error401("The request requires user authentication.");
		}

		HadoopUtil.getInstance().killAll(user);
		return new StringRepresentation("Lukas");

	}

}
