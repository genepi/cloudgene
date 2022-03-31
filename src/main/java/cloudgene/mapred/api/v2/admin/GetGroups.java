package cloudgene.mapred.api.v2.admin;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.server.auth.AuthenticationService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;

@Controller
public class GetGroups {
	
	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/groups")
	@Secured(User.ROLE_ADMIN)
	public String list() {

		List<Group> groups = new Vector<Group>();
		groups.add(new Group("admin"));
		groups.add(new Group("user"));
		application.getSettings();
		
		ApplicationRepository repository = application.getSettings().getApplicationRepository();

		for (Application application : repository.getAll()) {
			Group group = new Group(application.getPermission());
			if (!groups.contains(group)) {
				group.addApp(application.getId());
				groups.add(group);
			} else {
				int index = groups.indexOf(group);
				group = groups.get(index);
				group.addApp(application.getId());
			}
		}

		JSONArray jsonArray = JSONArray.fromObject(groups);

		return jsonArray.toString();

	}

	public static class Group {

		private String name;

		private List<String> apps = new Vector<String>();

		public Group(String name) {
			this.name = name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setApps(List<String> apps) {
			this.apps = apps;
		}

		public List<String> getApps() {
			return apps;
		}
		
		public void addApp(String app){
			apps.add(app);
		}

		@Override
		public boolean equals(Object obj) {
			Group g = (Group) obj;
			return g.getName().toLowerCase().equals(getName().toLowerCase());
		}
	}

}
