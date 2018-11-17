package cloudgene.mapred.api.v2.admin;

import java.util.List;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.JSONConverter;
import cloudgene.mapred.util.PageUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GetUsers extends BaseResource {

	public static final int DEFAULT_PAGE_SIZE = 100;

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		String page = getQueryValue("page");
		int pageSize = DEFAULT_PAGE_SIZE;

		int offset = 0;
		if (page != null) {

			offset = Integer.valueOf(page);
			if (offset < 1) {
				offset = 1;
			}
			offset = (offset - 1) * pageSize;
		}

		UserDao dao = new UserDao(getDatabase());
		int count = dao.findAll().size();
		List<User> users = null;
		if (page != null) {
			users = dao.findAll(offset, pageSize);
		} else {
			users = dao.findAll();
			page = "1";
			pageSize = count;
		}

		JSONArray jsonArray = JSONConverter.convertUsers(users);

		JSONObject object = PageUtil.createPageObject(Integer.parseInt(page), pageSize, count);
		object.put("data", jsonArray);

		return new StringRepresentation(object.toString());

	}

}
