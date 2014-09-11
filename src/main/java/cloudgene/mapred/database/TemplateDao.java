package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.util.Template;

public class TemplateDao extends Dao {

	private static final Log log = LogFactory.getLog(TemplateDao.class);

	public boolean insert(Template snippet) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into html_snippets (key, text) ");
		sql.append("values (?,?)");

		try {

			Object[] params = new Object[2];
			params[0] = snippet.getKey();
			params[1] = snippet.getText();

			update(sql.toString(), params);

			connection.commit();

			log.debug("insert html snippet successful.");

		} catch (SQLException e) {
			log.error("insert  html snippet  failed.", e);
			return false;
		}

		return true;
	}

	public boolean update(Template snippet) {
		StringBuilder sql = new StringBuilder();
		sql.append("update html_snippets SET text = ? where key = ? ");

		try {

			Object[] params = new Object[2];
			params[0] = snippet.getText();
			params[1] = snippet.getKey();

			update(sql.toString(), params);

			connection.commit();

			log.debug("update html snippet successful.");

		} catch (SQLException e) {
			log.error("update  html snippet  failed.", e);
			return false;
		}

		return true;
	}

	public List<Template> findAll() {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from html_snippets ");

		List<Template> result = new Vector<Template>();

		try {

			ResultSet rs = query(sql.toString());
			while (rs.next()) {

				Template snippet = new Template(rs.getString("key"),
						rs.getString("text"));

				result.add(snippet);
			}
			rs.close();

			log.debug("find all html snippets successful. results: "
					+ result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all html snippets failed", e);
			return null;
		}
	}

	public Template findByKey(String key) {

		StringBuffer sql = new StringBuffer();

		sql.append("select * ");
		sql.append("from html_snippets ");
		sql.append("where key = ?");

		Object[] params = new Object[1];
		params[0] = key;

		Template result = null;

		try {
			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				result = new Template(rs.getString("key"),
						rs.getString("text"));
			}
			rs.close();

			log.debug("find html snippet by key '" + key + "' successful.");

		} catch (SQLException e1) {

			log.error("find html snippet by key '" + key + "'  failed.", e1);

		}
		return result;
	}

}
