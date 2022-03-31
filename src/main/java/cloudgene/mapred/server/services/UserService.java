package cloudgene.mapred.server.services;

import java.util.List;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.Page;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserService {

	@Inject
	protected Application application;

	public Page<User> getAll(String query, String page, int pageSize) {

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

		Page<User> result = new Page<User>();
		result.setCount(count);
		result.setPage(Integer.parseInt(page));
		result.setPageSize(pageSize);

		return result;

	}

}
