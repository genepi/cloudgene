package cloudgene.mapred.server.controller;

import cloudgene.mapred.core.Group;
import cloudgene.mapred.core.User;
import cloudgene.mapred.server.services.GroupService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

import java.util.List;

@Controller("/api/v2/admin/groups")
public class GroupController {

	@Inject
	protected GroupService groupService;

	@Get("/")
	@Secured(User.ROLE_ADMIN)
	public List<Group> list() {

		return groupService.getAll();

	}

}
