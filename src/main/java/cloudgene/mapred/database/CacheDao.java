package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.jobs.cache.CacheEntry;

public class CacheDao extends Dao {

	private static final Log log = LogFactory.getLog(CacheDao.class);

	public boolean insert(CacheEntry entry) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into cache_entries (signature, used, last_used_on, created_on, execution_time, size, user_id, job_id, output) ");
		sql.append("values (?,?,?,?,?,?,?,?,?)");

		try {

			Object[] params = new Object[9];
			params[0] = entry.getSignature();
			params[1] = entry.getUsed();
			params[2] = entry.getLastUsedOn();
			params[3] = entry.getCreatedOn();
			params[4] = entry.getExecutionTime();
			params[5] = entry.getSize();
			params[6] = null;
			params[7] = null;
			params[8] = entry.getOutput();

			update(sql.toString(), params);

			connection.commit();

			log.debug("insert cache directory successful.");

		} catch (SQLException e) {
			log.error("insert cache directory failed.", e);
			return false;
		}

		return true;
	}

	public CacheEntry findBySignature(String signature) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from cache_entries ");
		sql.append("where signature = ? ");

		Object[] params = new Object[1];
		params[0] = signature;

		CacheEntry result = null;
		
		try {

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {
				result = new CacheEntry();
				result.setExecutionTime(rs.getLong("execution_time"));
				result.setCreatedOn(rs.getLong("created_on"));
				result.setLastUsedOn(rs.getLong("last_used_on"));
				result.setOutput(rs.getString("output"));
				result.setSignature(rs.getString("signature"));
				result.setSize(rs.getLong("size"));
				result.setUsed(rs.getInt("used"));
			}
			rs.close();

			log.debug("find chache entry by signature successful. result: "
					+ signature);

			return result;
		} catch (SQLException e) {
			log.error("find all downloads failed", e);
			return null;
		}
	}

	public boolean update(CacheEntry cacheEntry) {

		return true;
	}

	public CacheEntry getUnused() {

		return null;

	}

	public long getMemorySize() {

		return -1;

	}
	
	public boolean clear(){
		StringBuilder sql = new StringBuilder();
		sql.append("delete from cache_entries");
		try{

			update(sql.toString(), new Object[0]);

			connection.commit();

			log.debug("clear cache directory successful.");

		} catch (SQLException e) {
			log.error("clear cache directory failed.", e);
			return false;
		}

		return true;
	}

}
