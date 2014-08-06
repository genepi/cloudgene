package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameter;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.JobFactory;

public class JobDao extends Dao {

	private static final Log log = LogFactory.getLog(JobDao.class);

	public boolean insert(AbstractJob job) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into job (id, name, state, start_time, end_time, user_id, s3_url, type) ");
		sql.append("values (?,?,?,?,?,?,?,?)");

		try {

			Object[] params = new Object[8];
			params[0] = job.getId();
			params[1] = job.getName();
			params[2] = job.getState();
			params[3] = job.getStartTime();
			params[4] = job.getEndTime();
			params[5] = job.getUser().getId();
			params[6] = job.getS3Url();
			params[7] = job.getType();

			update(sql.toString(), params);

			connection.commit();

			ParameterDao dao = new ParameterDao();

			for (CloudgeneParameter parameter : job.getInputParams()) {
				parameter.setJobId(job.getId());
				dao.insert(parameter);
			}
			for (CloudgeneParameter parameter : job.getOutputParams()) {
				parameter.setJobId(job.getId());
				dao.insert(parameter);
			}

			if (job.getSteps() != null) {
				StepDao dao2 = new StepDao();
				for (CloudgeneStep step : job.getSteps()) {
					dao2.insert(step);
				}
			}

			connection.commit();
			log.info("insert job '" + job.getId() + "' successful.");

		} catch (SQLException e) {
			log.error("insert job '" + job.getId() + "' failed.", e);
			return false;
		}

		return true;
	}

	public boolean update(AbstractJob job) {
		StringBuilder sql = new StringBuilder();
		sql.append("update job ");
		sql.append("  set name = ?, state = ?, ");
		sql.append("  start_time = ?, end_time = ?, ");
		sql.append("  user_id = ?, s3_url = ?, type = ? ");
		sql.append("where id = ? ");
		try {

			Object[] params = new Object[8];
			params[0] = job.getName();
			params[1] = job.getState();
			params[2] = job.getStartTime();
			params[3] = job.getEndTime();
			params[4] = job.getUser().getId();
			params[5] = job.getS3Url();
			params[6] = job.getType();
			params[7] = job.getId();

			update(sql.toString(), params);

			connection.commit();

			log.info("update job successful.");

		} catch (SQLException e) {
			log.error("update job '" + job.getId() + "' failed", e);
			return false;
		}

		return true;
	}

	public boolean delete(AbstractJob job) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete job ");
		sql.append("where id = ? ");
		try {

			Object[] params = new Object[1];
			params[0] = job.getId();

			update(sql.toString(), params);

			connection.commit();

			log.info("delete job '" + job.getId() + "' successful.");

		} catch (SQLException e) {
			log.error("delete job '" + job.getId() + "' failed", e);
			return false;
		}

		return true;
	}

	public List<AbstractJob> findAllByUser(User user, boolean loadParameters,
			int limit) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where user_id = ? ");
		sql.append("order by start_time desc ");

		if (limit > 0) {
			sql.append("limit " + limit);
		}

		Object[] params = new Object[1];
		params[0] = user.getId();

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			ParameterDao dao = new ParameterDao();

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				int type = rs.getInt("type");

				AbstractJob job = JobFactory.create(type);
				job.setId(rs.getString("id"));
				job.setName(rs.getString("name"));
				job.setState(rs.getInt("state"));
				job.setStartTime(rs.getLong("start_time"));
				job.setEndTime(rs.getLong("end_time"));
				job.setS3Url(rs.getString("s3_url"));
				job.setUser(user);

				if (loadParameters) {
					List<CloudgeneParameter> inputParams = dao
							.findAllInputByJob(job);
					List<CloudgeneParameter> outputParams = dao
							.findAllOutputByJob(job);
					job.setInputParams(inputParams);
					job.setOutputParams(outputParams);

				}

				if (job instanceof CloudgeneJob) {

					StepDao stepDao = new StepDao();
					List<CloudgeneStep> steps = stepDao
							.findAllByJob((CloudgeneJob) job);
					job.setSteps(steps);

				}

				result.add(job);
			}
			rs.close();

			log.info("find all jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all jobs failed", e);
			return null;
		}
	}

	public List<AbstractJob> findAllYoungerThan(long time) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where state != ? AND ");
		sql.append("DATEDIFF('ms',now(), DATEADD('SECOND', end_time / 1000 + ?, DATE '1970-01-01')) > 0  ");

		sql.append("order by start_time desc ");

		List<AbstractJob> result = new Vector<AbstractJob>();

		Object[] params = new Object[2];
		params[0] = AbstractJob.STATE_RETIRED;
		params[1] = time;

		try {

			UserDao dao = new UserDao();

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				int type = rs.getInt("type");

				AbstractJob job = JobFactory.create(type);
				job.setId(rs.getString("id"));
				job.setName(rs.getString("name"));
				job.setState(rs.getInt("state"));
				job.setStartTime(rs.getLong("start_time"));
				job.setEndTime(rs.getLong("end_time"));
				job.setS3Url(rs.getString("s3_url"));

				User user = dao.findById(rs.getInt("user_id"));
				job.setUser(user);

				result.add(job);
			}
			rs.close();

			log.info("find all jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all jobs failed", e);
			return null;
		}
	}

	public List<AbstractJob> findAllOlderThan(int time) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where  state != ? AND DATEDIFF('ms',now(), DATEADD('SECOND', end_time / 1000 + ?, DATE '1970-01-01')) < 0  ");
		sql.append("order by start_time desc ");

		Object[] params = new Object[2];
		params[0] = AbstractJob.STATE_RETIRED;
		params[1] = time;

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			UserDao dao = new UserDao();

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				int type = rs.getInt("type");

				AbstractJob job = JobFactory.create(type);
				job.setId(rs.getString("id"));
				job.setName(rs.getString("name"));
				job.setState(rs.getInt("state"));
				job.setStartTime(rs.getLong("start_time"));
				job.setEndTime(rs.getLong("end_time"));
				job.setS3Url(rs.getString("s3_url"));

				User user = dao.findById(rs.getInt("user_id"));
				job.setUser(user);

				result.add(job);
			}
			rs.close();

			log.info("find all old jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all old jobs failed", e);
			return null;
		}
	}

	public List<AbstractJob> findAllOlderThan(int time, int state) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where  state = ? AND DATEDIFF('ms',now(), DATEADD('SECOND', end_time / 1000 + ?, DATE '1970-01-01')) < 0  ");
		sql.append("order by start_time desc ");

		Object[] params = new Object[2];
		params[0] = state;
		params[1] = time;

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			UserDao dao = new UserDao();

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				int type = rs.getInt("type");

				AbstractJob job = JobFactory.create(type);
				job.setId(rs.getString("id"));
				job.setName(rs.getString("name"));
				job.setState(rs.getInt("state"));
				job.setStartTime(rs.getLong("start_time"));
				job.setEndTime(rs.getLong("end_time"));
				job.setS3Url(rs.getString("s3_url"));

				User user = dao.findById(rs.getInt("user_id"));
				job.setUser(user);

				result.add(job);
			}
			rs.close();

			log.info("find all old jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all old jobs failed", e);
			return null;
		}
	}
	
	public List<AbstractJob> findAllByState(int state) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where state = ? ");
		sql.append("order by start_time desc ");

		Object[] params = new Object[1];
		params[0] = state;

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			UserDao dao = new UserDao();

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				int type = rs.getInt("type");

				AbstractJob job = JobFactory.create(type);
				job.setId(rs.getString("id"));
				job.setName(rs.getString("name"));
				job.setState(rs.getInt("state"));
				job.setStartTime(rs.getLong("start_time"));
				job.setEndTime(rs.getLong("end_time"));
				job.setS3Url(rs.getString("s3_url"));

				User user = dao.findById(rs.getInt("user_id"));
				job.setUser(user);

				result.add(job);
			}
			rs.close();

			log.info("find all old jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all old jobs failed", e);
			return null;
		}
	}

	public List<AbstractJob> findAllByUserAndFormat(User user, String format) {

		StringBuilder sql = new StringBuilder();
		sql.append("select j.type, j.id, j.name, j.state, j.start_time, j.end_time, j.s3_url, p.value ");
		sql.append("from job j ");
		sql.append("join parameter p on j.id = p.job_id ");
		sql.append("where user_id = ? and format = ? and j.state = ? and p.input = false ");
		sql.append("order by start_time desc ");

		Object[] params = new Object[3];
		params[0] = user.getId();
		params[1] = format;
		params[2] = AbstractJob.STATE_SUCCESS;

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				int type = rs.getInt("type");

				AbstractJob job = JobFactory.create(type);
				job.setId(rs.getString("id"));
				job.setName(rs.getString("name"));
				job.setState(rs.getInt("state"));
				job.setStartTime(rs.getLong("start_time"));
				job.setEndTime(rs.getLong("end_time"));
				job.setS3Url(rs.getString("s3_url"));
				job.setUser(user);

				List<CloudgeneParameter> outputParams = new Vector<CloudgeneParameter>();

				CloudgeneParameter parameter = new CloudgeneParameter();
				parameter.setValue(rs.getString("value"));
				outputParams.add(parameter);

				job.setOutputParams(outputParams);

				result.add(job);
			}
			rs.close();

			log.info("find all jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all jobs failed", e);
			return null;
		}
	}

	public AbstractJob findById(String id) {

		return findById(id, true);

	}

	public AbstractJob findById(String id, boolean loadParams) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where id = ?");

		Object[] params = new Object[1];
		params[0] = id;

		AbstractJob job = null;

		try {

			ParameterDao dao = new ParameterDao();
			UserDao userDao = new UserDao();

			ResultSet rs = query(sql.toString(), params);
			while (rs.next()) {

				int type = rs.getInt("type");

				job = JobFactory.create(type);
				job.setId(rs.getString("id"));
				job.setName(rs.getString("name"));
				job.setState(rs.getInt("state"));
				job.setStartTime(rs.getLong("start_time"));
				job.setEndTime(rs.getLong("end_time"));
				job.setS3Url(rs.getString("s3_url"));

				User user = userDao.findById(rs.getInt("user_id"));
				job.setUser(user);

				if (loadParams) {
					List<CloudgeneParameter> inputParams = dao
							.findAllInputByJob(job);
					List<CloudgeneParameter> outputParams = dao
							.findAllOutputByJob(job);
					job.setInputParams(inputParams);
					job.setOutputParams(outputParams);
				}

				if (job instanceof CloudgeneJob) {

					StepDao stepDao = new StepDao();
					List<CloudgeneStep> steps = stepDao
							.findAllByJob((CloudgeneJob) job);
					job.setSteps(steps);

				}

			}
			rs.close();

			if (job instanceof CloudgeneJob) {
				((CloudgeneJob) job).updateProgress();
			}

			if (job != null) {
				log.info("find job by id '" + id + "' successful.");
			} else {
				log.info("job '" + id + "' not found");
			}

			return job;
		} catch (SQLException e) {
			log.error("find job by id '" + id + "' failed", e);
			return null;
		}
	}
}
