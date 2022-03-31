package cloudgene.mapred.api.v2.admin;

import java.util.List;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PageUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class GetUsers {

	public static final int DEFAULT_PAGE_SIZE = 100;

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/api/v2/admin/users")
	@Secured(SecurityRule.IS_AUTHENTICATED)
	public String get(Authentication authentication, @Nullable @QueryValue("page") String page, @Nullable @QueryValue("query") String query) {

		User user = authenticationService.getUserByAuthentication(authentication);

		if (!user.isAdmin()) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "The request requires administration rights.");
		}

		int pageSize = DEFAULT_PAGE_SIZE;

		int offset = 0;
		if (page != null) {

			offset = Integer.valueOf(page);
			if (offset < 1) {
				offset = 1;
			}
			offset = (offset - 1) * pageSize;
		}

		UserDao dao = new UserDao(application.getDatabase());

		List<User> users = null;
		int count = 0;

		if (query != null && !query.isEmpty()) {
			users = dao.findByQuery(query);
			page = "1";
			count = users.size();
			pageSize = count;
		} else {
			if (page != null) {
				users = dao.findAll(offset, pageSize);
				count = dao.findAll().size();
			} else {
				users = dao.findAll();
				page = "1";
				count = users.size();
				pageSize = count;
			}
		}

		JSONArray jsonArray = JSONConverter.convertUsers(users);

		JSONObject object = PageUtil.createPageObject(Integer.parseInt(page), pageSize, count);
		object.put("data", jsonArray);

		return object.toString();

	}

}
