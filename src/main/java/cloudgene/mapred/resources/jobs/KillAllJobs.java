package cloudgene.mapred.resources.jobs;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HadoopUtil;

public class KillAllJobs extends BaseResource {

	@Get
	public Representation get() {

		User user = getUser(getRequest());

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		HadoopUtil.getInstance().killAll(user);
		return new StringRepresentation("Lukas");

	}

}
