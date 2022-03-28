package cloudgene.mapred.api.v2.admin.server;

import java.util.List;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.util.Template;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import cloudgene.mapred.exceptions.JsonHttpStatusException;

@Controller
public class GetTemplates {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/server/templates")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		TemplateDao dao = new TemplateDao(application.getDatabase());
		List<Template> templates = dao.findAll();

		JSONArray jsonArray = JSONArray.fromObject(templates);

		return jsonArray.toString();

	}

}
