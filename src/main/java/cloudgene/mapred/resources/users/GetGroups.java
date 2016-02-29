package cloudgene.mapred.resources.users;

import java.util.List;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.BaseResource;

public class GetGroups extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		List<Group> groups = new Vector<Group>();
		groups.add(new Group("admin"));
		groups.add(new Group("User"));
		for (Application application : getSettings().getApps()) {
			if (!groups.contains(new Group(application.getPermission()))) {
				groups.add(new Group(application.getPermission()));
			}
		}

		JSONArray jsonArray = JSONArray.fromObject(groups);

		return new StringRepresentation(jsonArray.toString());

	}

	public static class Group {

		private String name;

		public Group(String name) {
			this.name = name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
		
		@Override
		public boolean equals(Object obj) {			
			Group g = (Group) obj;			
			return g.getName().toLowerCase().equals(getName().toLowerCase());
		}
	}

}
