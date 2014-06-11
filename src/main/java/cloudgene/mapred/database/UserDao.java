package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.MySecretKey;

public class UserDao extends Dao {

	private static final Log log = LogFactory.getLog(UserDao.class);

	public boolean insert(User user) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into user (username, password, full_name, aws_key, aws_secret_key, save_keys, export_to_s3, s3_bucket, mail, role, export_input_to_s3, activation_code, active) ");
		sql.append("values (?,?,?,?,?,?,?,?,?,?,?,?,?)");

		try {

			Object[] params = new Object[13];
			params[0] = user.getUsername().toLowerCase();
			params[1] = user.getPassword();
			params[2] = user.getFullName();
			if (user.getAwsKey() != null & user.getAwsKey().isEmpty()) {
				params[3] = MySecretKey.encrypt(user.getAwsKey());
				params[4] = MySecretKey.encrypt(user.getAwsSecretKey());
			} else {
				params[3] = null;
				params[4] = null;
			}
			params[5] = user.isSaveCredentials();
			params[6] = user.isExportToS3();
			params[7] = user.getS3Bucket();
			params[8] = user.getMail();
			params[9] = user.getRole();
			params[10] = user.isExportInputToS3();
			params[11] = user.getActivationCode();
			params[12] = user.isActive();

			int id = updateAndGetKey(sql.toString(), params);

			user.setId(id);

			connection.commit();

			log.info("insert user '" + user.getUsername() + "' successful.");

		} catch (SQLException e) {
			log.error("insert user '" + user.getUsername() + "' failed.", e);
			return false;
		}

		return true;
	}

	public boolean update(User user) {
		StringBuilder sql = new StringBuilder();
		sql.append("update user set username = ?, password = ?, full_name = ?, aws_key = ?, aws_secret_key = ?, save_keys = ? , export_to_s3 = ?, s3_bucket = ?, mail = ?, role = ?, export_input_to_s3 = ?, active = ?");
		sql.append("where id = ?");

		try {

			Object[] params = new Object[13];
			params[0] = user.getUsername().toLowerCase();
			params[1] = user.getPassword();
			params[2] = user.getFullName();
			if (user.isSaveCredentials()) {
				params[3] = MySecretKey.encrypt(user.getAwsKey());
				params[4] = MySecretKey.encrypt(user.getAwsSecretKey());
			} else {
				params[3] = null;
				params[4] = null;
			}
			params[5] = user.isSaveCredentials();
			params[6] = user.isExportToS3();
			params[7] = user.getS3Bucket();
			params[8] = user.getMail();
			params[9] = user.getRole();
			params[10] = user.isExportInputToS3();
			params[11] = user.isActive();
			params[12] = user.getId();

			update(sql.toString(), params);

			connection.commit();

			log.info("update user '" + user.getUsername() + "' successful.");

		} catch (SQLException e) {
			log.error("update user '" + user.getUsername() + "' failed.", e);
			return false;
		}

		return true;
	}

	public User findByUsername(String user) {

		StringBuffer sql = new StringBuffer();

		sql.append("select * ");
		sql.append("from user ");
		sql.append("where username = ?");

		Object[] params = new Object[1];
		params[0] = user.toLowerCase();

		User result = null;

		try {
			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {
				result = new User();
				result.setId(rs.getInt("id"));
				result.setUsername(rs.getString("username"));
				result.setPassword(rs.getString("password"));
				result.setFullName(rs.getString("full_name"));
				if (rs.getString("aws_key") != null) {
					result.setAwsKey(MySecretKey.decrypt(rs
							.getString("aws_key")));
				}
				if (rs.getString("aws_secret_key") != null) {
					result.setAwsSecretKey(MySecretKey.decrypt(rs
							.getString("aws_secret_key")));
				}
				result.setSaveCredentials(rs.getBoolean("save_keys"));
				result.setExportToS3(rs.getBoolean("export_to_s3"));
				result.setS3Bucket(rs.getString("s3_bucket"));
				result.setMail(rs.getString("mail"));
				result.setRole(rs.getString("role"));
				result.setExportInputToS3(rs.getBoolean("export_input_to_s3"));
				result.setActivationCode(rs.getString("activation_code"));
				result.setActive(rs.getBoolean("active"));
			}
			rs.close();

			log.info("find user by username '" + user + "' successful.");

		} catch (SQLException e1) {

			log.error("find user by username " + user + "' failed.", e1);

		}
		return result;
	}

	public User findById(int id) {

		StringBuffer sql = new StringBuffer();

		sql.append("select * ");
		sql.append("from user ");
		sql.append("where id = ?");

		Object[] params = new Object[1];
		params[0] = id;

		User result = null;

		try {
			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {
				result = new User();
				result.setId(rs.getInt("id"));
				result.setUsername(rs.getString("username"));
				result.setPassword(rs.getString("password"));
				result.setFullName(rs.getString("full_name"));
				if (rs.getString("aws_key") != null) {
					result.setAwsKey(MySecretKey.decrypt(rs
							.getString("aws_key")));
				}
				if (rs.getString("aws_secret_key") != null) {
					result.setAwsSecretKey(MySecretKey.decrypt(rs
							.getString("aws_secret_key")));
				}
				result.setSaveCredentials(rs.getBoolean("save_keys"));
				result.setExportToS3(rs.getBoolean("export_to_s3"));
				result.setS3Bucket(rs.getString("s3_bucket"));
				result.setMail(rs.getString("mail"));
				result.setRole(rs.getString("role"));
				result.setExportInputToS3(rs.getBoolean("export_input_to_s3"));
				result.setActivationCode(rs.getString("activation_code"));
				result.setActive(rs.getBoolean("active"));
			}
			rs.close();

			log.info("find user by id '" + id + "' successful.");

		} catch (SQLException e1) {

			log.error("find user by id failed.", e1);

		}
		return result;
	}

	public List<User> findAll() {

		StringBuffer sql = new StringBuffer();

		sql.append("select * ");
		sql.append("from user ");

		List<User> result = new Vector<User>();

		try {
			ResultSet rs = query(sql.toString());
			while (rs.next()) {
				User user = new User();
				user.setId(rs.getInt("id"));
				user.setUsername(rs.getString("username"));
				user.setPassword(rs.getString("password"));
				user.setFullName(rs.getString("full_name"));
				/*if (rs.getString("aws_key") != null && !rs.getString("aws_key").isEmpty()) {
					user.setAwsKey(MySecretKey.decrypt(rs.getString("aws_key")));
				}
				if (rs.getString("aws_secret_key") != null) {
					user.setAwsSecretKey(MySecretKey.decrypt(rs
							.getString("aws_secret_key")));
				}*/
				user.setSaveCredentials(rs.getBoolean("save_keys"));
				user.setExportToS3(rs.getBoolean("export_to_s3"));
				user.setS3Bucket(rs.getString("s3_bucket"));
				user.setMail(rs.getString("mail"));
				user.setRole(rs.getString("role"));
				user.setExportInputToS3(rs.getBoolean("export_input_to_s3"));
				user.setActivationCode(rs.getString("activation_code"));
				user.setActive(rs.getBoolean("active"));
				result.add(user);

			}
			rs.close();

			log.info("find all user successful. size = " + result.size());

		} catch (SQLException e1) {

			log.error("find all user failed.", e1);

		}
		return result;
	}

	public boolean delete(User user) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete user ");
		sql.append("where id = ? ");
		try {

			Object[] params = new Object[1];
			params[0] = user.getId();

			update(sql.toString(), params);

			connection.commit();

			log.info("delete user successful.");

		} catch (SQLException e) {
			log.error("delete user failed", e);
			return false;
		}

		return true;
	}

}
