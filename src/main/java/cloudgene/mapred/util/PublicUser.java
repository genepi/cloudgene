package cloudgene.mapred.util;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.database.util.Database;

public class PublicUser {

	public static User getUser(Database database) {
		UserDao dao = new UserDao(database);
		User user = dao.findByUsername("public");
		if (user == null) {
			user = new User();
			user.setUsername("public");
			String password = HashUtil.getMD5("public-password");
			user.setPassword(password);
			user.setRole("public");
			dao.insert(user);
		}
		return user;
	}
}
