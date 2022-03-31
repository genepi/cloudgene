package cloudgene.mapred.server.services;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.Group;
import cloudgene.mapred.core.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GroupService {
	@Inject
	protected cloudgene.mapred.server.Application application;

	public List<Group> getAll() {

		List<Group> groups = new Vector<Group>();
		groups.add(new Group(User.ROLE_ADMIN));
		groups.add(new Group(User.ROLE_USER));
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

		return groups;

	}

}
