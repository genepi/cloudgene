package cloudgene.mapred.server.controller;

import java.util.List;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller("/api/v2/admin/server/templates")
public class TemplateController {

	private static final String MESSAGE_TEMPLATE_NOT_FOUND = "Template %s not found.";

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/")
	@Secured(User.ROLE_ADMIN)
	public List<Template> list() {

		TemplateDao dao = new TemplateDao(application.getDatabase());
		List<Template> templates = dao.findAll();

		return templates;

	}

	@Post("/{key}")
	@Secured(User.ROLE_ADMIN)
	public Template update(String key, String text) {

		Template template = new Template(key, text);

		TemplateDao dao = new TemplateDao(application.getDatabase());
		dao.update(template);

		application.reloadTemplates();

		return template;

	}

	@Get("/{key}")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public Template get(String key) {

		TemplateDao dao = new TemplateDao(application.getDatabase());

		Template template = dao.findByKey(key);

		if (template == null) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, String.format(MESSAGE_TEMPLATE_NOT_FOUND, key));
		}

		return template;

	}

}
