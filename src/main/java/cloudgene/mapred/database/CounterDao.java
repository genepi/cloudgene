package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.jobs.AbstractJob;

public class CounterDao extends Dao {

	private static final Log log = LogFactory.getLog(CounterDao.class);

	public boolean insert(String name, int value, AbstractJob job) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into counters (name, job_id, value) ");
		sql.append("values (?,?,?)");

		try {

			Object[] params = new Object[3];
			params[0] = name;
			params[1] = job.getId();
			params[2] = value;

			update(sql.toString(), params);
			
			connection.commit();

			log.info("insert counter successful.");

		} catch (SQLException e) {
			log.error("insert counter failed.", e);
			return false;
		}

		return true;
	}

	public Map<String, Integer> getAll() {

		StringBuilder sql = new StringBuilder();
		sql.append("select name, sum(value) ");
		sql.append("from counters ");
		sql.append("group by name");

		Map<String, Integer> result = new HashMap<String, Integer>();

		try {

			ResultSet rs = query(sql.toString());
			while (rs.next()) {
				result.put(rs.getString(1), rs.getInt(2));
			}
			rs.close();

			log.info("find counters successful. results: " + result);

			return result;
		} catch (SQLException e) {
			log.error("find all counters failed", e);
		}
		
		return result;
	}

}
