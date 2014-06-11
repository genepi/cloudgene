package cloudgene.mapred.resources.apps;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.json.JSONArray;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;

public class GetAppDetails extends ServerResource {

	public class InfoObject {

		private String jobName;
		private String name;
		private String description;

		public String getJobName() {
			return jobName;
		}

		public void setJobName(String jobName) {
			this.jobName = jobName;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());

		if (user != null) {

			Form form = new Form(entity);
			// AppMetaData app = YamlLoader.loadApp(form.getFirstValue("tool"));

			InfoObject infoObject = new InfoObject();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
			infoObject.setJobName(form.getFirstValue("tool") + "-"
					+ sdf.format(new Date()));

			JSONArray jsonArray = JSONArray.fromObject(infoObject);

			return new StringRepresentation(jsonArray.toString());

		} else {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED );
			return new StringRepresentation("The request requires user authentication.");

		}
	}

}
