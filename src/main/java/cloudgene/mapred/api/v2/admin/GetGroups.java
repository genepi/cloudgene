package cloudgene.mapred.api.v2.admin;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;

@Controller
public class GetGroups {
	
	@Inject
	protected cloudgene.mapred.server.Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/groups")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

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
