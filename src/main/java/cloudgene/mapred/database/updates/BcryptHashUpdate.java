package cloudgene.mapred.database.updates;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.UserDao;
import genepi.db.Database;
import genepi.db.IUpdateListener;

public class BcryptHashUpdate implements IUpdateListener {

	private static final Logger log = LoggerFactory.getLogger(BcryptHashUpdate.class);

	
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
