package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CounterHistoryDao extends Dao {

	private static final Log log = LogFactory.getLog(CounterHistoryDao.class);

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yy-MM-dd HH:mm");

	public boolean insert(long timestamp, String name, long value) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into counters_history (time_stamp, name, value) ");
		sql.append("values (?,?,?)");

		try {

			Object[] params = new Object[3];
			params[0] = timestamp;
			params[1] = name;
			params[2] = value;

			update(sql.toString(), params);

			connection.commit();

			log.debug("insert counter history successful.");

		} catch (SQLException e) {
			log.error("insert counter history failed.", e);
			return false;
		}

		return true;
	}

	public List<Map<String, String>> getAll() {

		StringBuilder sql = new StringBuilder();
		sql.append("select time_stamp, name, value ");
		sql.append("from counters_history ");
		sql.append("order by time_stamp, name");

		List<Map<String, String>> result = new Vector<Map<String, String>>();

		try {

			String old = "";
			Map<String, String> counters = null;

			ResultSet rs = query(sql.toString());
			while (rs.next()) {

				if (!old.equals(rs.getString(1))) {
					counters = new HashMap<String, String>();
					result.add(counters);
					counters.put("timestamp",
							DATE_FORMAT.format(new Date(rs.getLong(1))));
					old = rs.getString(1);
				}
				counters.put(rs.getString(2), rs.getString(3));
			}
			rs.close();

			log.debug("find counter history successful. results: "
					+ result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all counter history failed", e);
		}

		return result;
	}

}
