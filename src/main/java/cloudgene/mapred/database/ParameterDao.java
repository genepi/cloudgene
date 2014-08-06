package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneParameter;
import cloudgene.mapred.jobs.Download;

public class ParameterDao extends Dao {

	private static final Log log = LogFactory.getLog(ParameterDao.class);

	public boolean insert(CloudgeneParameter parameter) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into parameter (name, value, input, job_id, type, variable, download, format) ");
		sql.append("values (?,?,?,?,?,?,?,?)");

		try {

			Object[] params = new Object[8];
			params[0] = parameter.getDescription();
			params[1] = parameter.getValue();
			params[2] = parameter.isInput();
			params[3] = parameter.getJob().getId();
			params[4] = parameter.getType();
			params[5] = parameter.getName();
			params[6] = parameter.isDownload();
			params[7] = parameter.getFormat();

			int paramId = updateAndGetKey(sql.toString(), params);

			// insert daos

			if (!parameter.isInput() && parameter.isDownload()) {

				if (((CloudgeneParameter) parameter).getFiles() != null) {

					DownloadDao dao = new DownloadDao();
					for (Download download : parameter.getFiles()) {
						download.setParameterId(paramId);
						download.setParameter(parameter);
						dao.insert(download);
					}

				}

			}

			connection.commit();

			log.debug("insert parameter '" + parameter.getId() + "' successful.");

		} catch (SQLException e) {
			log.error("insert parameter '" + parameter.getId() + "' failed.", e);
			return false;
		}

		return true;
	}

	public List<CloudgeneParameter> findAllInputByJob(AbstractJob job) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from parameter ");
		sql.append("where job_id = ? and input = true");

		Object[] params = new Object[1];
		params[0] = job.getId();

		List<CloudgeneParameter> result = new Vector<CloudgeneParameter>();

		try {

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				CloudgeneParameter parameter = new CloudgeneParameter();
				parameter.setDescription(rs.getString("name"));
				parameter.setValue(rs.getString("value"));
				parameter.setName(rs.getString("variable"));
				parameter.setInput(rs.getBoolean("input"));
				parameter.setJobId(rs.getString("job_id"));
				parameter.setType(rs.getString("type"));
				parameter.setDownload(rs.getBoolean("download"));
				parameter.setFormat(rs.getString("format"));
				parameter.setId(rs.getInt("id"));
				result.add(parameter);
			}
			rs.close();

			log.debug("find all input parameters for job '" + job.getId()
					+ "' successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all input parameters for job '" + job.getId()
					+ "' failed.", e);
			return null;
		}
	}

	public List<CloudgeneParameter> findAllOutputByJob(AbstractJob job) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from parameter ");
		sql.append("where job_id = ? and input = false");

		Object[] params = new Object[1];
		params[0] = job.getId();

		List<CloudgeneParameter> result = new Vector<CloudgeneParameter>();

		try {

			DownloadDao dao = new DownloadDao();

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				CloudgeneParameter parameter = new CloudgeneParameter();
				parameter.setDescription(rs.getString("name"));
				parameter.setName(rs.getString("variable"));
				parameter.setValue(rs.getString("value"));
				parameter.setInput(rs.getBoolean("input"));
				parameter.setJobId(rs.getString("job_id"));
				parameter.setType(rs.getString("type"));
				parameter.setDownload(rs.getBoolean("download"));
				parameter.setFormat(rs.getString("format"));
				parameter.setId(rs.getInt("id"));
				List<Download> downloads = dao.findAllByParameter(parameter);
				for (Download download : downloads) {
					download.setUsername(job.getUser().getUsername());
				}
				parameter.setFiles(downloads);
				result.add(parameter);
			}
			rs.close();

			log.debug("find all output parameters for job '" + job.getId()
					+ "' successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all output parameters for job '" + job.getId()
					+ "' failed.", e);
			return null;
		}
	}

}
