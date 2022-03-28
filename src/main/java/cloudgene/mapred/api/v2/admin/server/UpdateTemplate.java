package cloudgene.mapred.api.v2.admin.server;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.Template;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;

@Controller
public class UpdateTemplate {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Post(uri = "/api/v2/admin/server/templates/{id}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String post(Authentication authentication, String id, String text) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "The request requires administration rights.");
		}

		Template template = new Template(id, text);

		TemplateDao dao = new TemplateDao(application.getDatabase());
		dao.update(template);

		application.reloadTemplates();

		JSONObject jsonObject = JSONObject.fromObject(template);
		return jsonObject.toString();

	}

	@Get("/api/v2/admin/server/templates/{id}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication, String id) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (user == null) {
			throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires user authentication.");
		}

		TemplateDao dao = new TemplateDao(application.getDatabase());

		Template template = dao.findByKey(id);

		if (template == null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "Template " + id + " not found.");
		}

		JSONObject jsonObject = JSONObject.fromObject(template);

		return jsonObject.toString();

	}

}
