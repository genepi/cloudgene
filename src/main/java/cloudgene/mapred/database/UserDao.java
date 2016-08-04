package cloudgene.mapred.database;

import genepi.db.Database;
import genepi.db.IRowMapper;
import genepi.db.JdbcDataAccessObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;

public class UserDao extends JdbcDataAccessObject {

	private static final Log log = LogFactory.getLog(UserDao.class);

	public UserDao(Database database) {
		super(database);
	}

	public boolean insert(User user) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into user (username, password, full_name, aws_key, aws_secret_key, save_keys, export_to_s3, s3_bucket, mail, role, export_input_to_s3, activation_code, active,?) ");
		sql.append("values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		try {

			Object[] params = new Object[14];
			params[0] = user.getUsername().toLowerCase();
			params[1] = user.getPassword();
			params[2] = user.getFullName();
			params[3] = null;
			params[4] = null;
			params[5] = false;
			params[6] = false;
			params[7] = null;
			params[8] = user.getMail();
			params[9] = user.getRole();
			params[10] = false;
			params[11] = user.getActivationCode();
			params[12] = user.isActive();
			params[13] = user.getApiToken();

			int id = insert(sql.toString(), params);

			user.setId(id);

			log.debug("insert user '" + user.getUsername() + "' successful.");

		} catch (SQLException e) {
			log.error("insert user '" + user.getUsername() + "' failed.", e);
			return false;
		}

		return true;
	}

	public boolean update(User user) {
		StringBuilder sql = new StringBuilder();
		sql.append("update user set username = ?, password = ?, full_name = ?, aws_key = ?, aws_secret_key = ?, save_keys = ? , export_to_s3 = ?, s3_bucket = ?, mail = ?, role = ?, export_input_to_s3 = ?, active = ?, activation_code = ?, api_token = ? ");
		sql.append("where id = ?");

		try {

			Object[] params = new Object[15];
			params[0] = user.getUsername().toLowerCase();
			params[1] = user.getPassword();
			params[2] = user.getFullName();
			params[3] = null;
			params[4] = null;
			params[5] = false;
			params[6] = false;
			params[7] = null;
			params[8] = user.getMail();
			params[9] = user.getRole();
			params[10] = false;
			params[11] = user.isActive();
			params[12] = user.getActivationCode();
			params[13] = user.getApiToken();
			params[14] = user.getId();

			update(sql.toString(), params);

			log.debug("update user '" + user.getUsername() + "' successful.");

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

			result = (User) queryForObject(sql.toString(), params,
					new UserMapper());

			log.debug("find user by username '" + user + "' successful.");

		} catch (SQLException e1) {

			log.error("find user by username " + user + "' failed.", e1);

		}
		return result;
	}

	public User findByMail(String mail) {

		StringBuffer sql = new StringBuffer();

		sql.append("select * ");
		sql.append("from user ");
		sql.append("where mail = ?");

		Object[] params = new Object[1];
		params[0] = mail.toLowerCase();

		User result = null;

		try {
			result = (User) queryForObject(sql.toString(), params,
					new UserMapper());

			log.debug("find user by mail '" + mail + "' successful.");

		} catch (SQLException e1) {

			log.error("find user by mail " + mail + "' failed.", e1);

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

			result = (User) queryForObject(sql.toString(), params,
					new UserMapper());

			log.debug("find user by id '" + id + "' successful.");

		} catch (SQLException e1) {

			log.error("find user by id failed.", e1);

		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<User> findAll() {

		StringBuffer sql = new StringBuffer();

		sql.append("select * ");
		sql.append("from user ");

		List<User> result = new Vector<User>();

		try {
			result = query(sql.toString(), new UserMapper());

			log.debug("find all user successful. size = " + result.size());

		} catch (SQLException e1) {

			log.error("find all user failed.", e1);

		}
		return result;
	}

	public boolean delete(User user) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from user ");
		sql.append("where id = ? ");
		try {

			Object[] params = new Object[1];
			params[0] = user.getId();

			update(sql.toString(), params);

			log.debug("delete user successful.");

		} catch (SQLException e) {
			log.error("delete user failed", e);
			return false;
		}

		return true;
	}

	class UserMapper implements IRowMapper {

		@Override
		public Object mapRow(ResultSet rs, int row) throws SQLException {
			User user = new User();
			user.setId(rs.getInt("id"));
			user.setUsername(rs.getString("username"));
			user.setPassword(rs.getString("password"));
			user.setFullName(rs.getString("full_name"));
			user.setMail(rs.getString("mail"));
			user.setRole(rs.getString("role"));
			user.setActivationCode(rs.getString("activation_code"));
			user.setActive(rs.getBoolean("active"));
			user.setApiToken(rs.getString("api_token"));
			return user;
		}

	}

}
