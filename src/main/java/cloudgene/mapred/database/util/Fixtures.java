package cloudgene.mapred.database.util;

import cloudgene.mapred.core.Template;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import genepi.db.Database;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Fixtures {

	private static final Log log = LogFactory.getLog(Fixtures.class);

	public static String USERNAME = "admin";

	public static String PASSWORD = "admin1978";

	public static void insert(Database database) {

		// insert user
		UserDao dao = new UserDao(database);
		User user = dao.findByUsername(USERNAME);
		if (user == null) {
			user = new User();
			user.setUsername(USERNAME);
			PASSWORD = HashUtil.hashPassword(PASSWORD);
			user.setPassword(PASSWORD);
			user.makeAdmin();

			dao.insert(user);
			log.info("User " + USERNAME + " created.");
		} else {	
			
			log.info("User " + USERNAME + " already exists.");
			
			if (!user.isAdmin()){
				user.makeAdmin();
				dao.update(user);
				log.info("User " + USERNAME + " has admin rights now.");
			}
		}

		// insert template messages
		TemplateDao htmlSnippetDao = new TemplateDao(database);

		for (Template defaultSnippet : Template.SNIPPETS) {

			Template snippet = htmlSnippetDao
					.findByKey(defaultSnippet.getKey());
			if (snippet == null) {
				htmlSnippetDao.insert(defaultSnippet);
				log.info("Template " + defaultSnippet.getKey() + " created.");
			} else {
				log.info("Template " + defaultSnippet.getKey()
						+ " already exists.");
			}

		}

	}

}
