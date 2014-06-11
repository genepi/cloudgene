package cloudgene.mapred.resources.jobs;

import net.sf.json.JSONArray;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.ClusterInformation;
import cloudgene.mapred.util.HadoopUtil;

public class GetClusterDetails extends ServerResource {

	@Get
	public Representation get() {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			ClusterInformation[] array = new ClusterInformation[1];
			array[0] = new ClusterInformation(HadoopUtil.getInstance()
					.getClusterDetails());

			JSONArray jsonArray = JSONArray.fromObject(array);

			return new StringRepresentation(jsonArray.toString(),
					MediaType.APPLICATION_JSON);

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}
	}

}
