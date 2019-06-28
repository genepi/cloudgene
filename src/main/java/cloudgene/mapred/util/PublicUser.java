package cloudgene.mapred.util;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import genepi.db.Database;

public class PublicUser {

	public static User getUser(Database database) {
		UserDao dao = new UserDao(database);
		User user = dao.findByUsername("public");
		if (user == null) {
			user = new User();
			user.setUsername("public");
			String password = HashUtil.getMD5("public-password");
			user.setPassword(password);
			user.setRoles(new String[]{"public"});
			dao.insert(user);
		}
		return user;
	}
}
