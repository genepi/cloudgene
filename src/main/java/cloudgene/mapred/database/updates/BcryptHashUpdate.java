package cloudgene.mapred.database.updates;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import genepi.db.Database;
import genepi.db.IUpdateListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

public class BcryptHashUpdate implements IUpdateListener {

	private static final Log log = LogFactory.getLog(BcryptHashUpdate.class);

	
	@Override
	public void afterUpdate(Database database) {

	}

	@Override
	public void beforeUpdate(Database database) {
		log.info("Updating all hashes to new bcrypt method...");
		UserDao dao = new UserDao(database);
		List<User> users = dao.findAll();
		for (User user: users) {
			String oldHash = user.getPassword();
			String newHash = BCrypt.hashpw(oldHash, BCrypt.gensalt());
			user.setPassword(newHash);
			dao.update(user);
		}
		log.info("All hashes updated.");
	}

}
