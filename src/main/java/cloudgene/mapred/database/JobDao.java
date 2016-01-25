package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.database.util.IRowMapper;
import cloudgene.mapred.database.util.JdbcDataAccessObject;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneParameter;
import cloudgene.mapred.jobs.CloudgeneStep;

public class JobDao extends JdbcDataAccessObject {

	private static final Log log = LogFactory.getLog(JobDao.class);

	public JobDao(Database database) {
		super(database);
	}

	public boolean insert(AbstractJob job) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into job (id, name, state, start_time, end_time, user_id, s3_url, type, application, application_id) ");
		sql.append("values (?,?,?,?,?,?,?,?,?,?)");

		try {

			Object[] params = new Object[10];
			params[0] = job.getId();
			params[1] = job.getName();
			params[2] = job.getState();
			params[3] = job.getStartTime();
			params[4] = job.getEndTime();
			params[5] = job.getUser().getId();
			params[6] = "";
			params[7] = job.getType();
			params[8] = job.getApplication();
			params[9] = job.getApplicationId();

			update(sql.toString(), params);

			log.debug("insert job '" + job.getId() + "' successful.");

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
		sql.append("  user_id = ?, s3_url = ?, type = ?, deleted_on = ?, application = ?, application_id = ? ");
		sql.append("where id = ? ");
		try {

			Object[] params = new Object[11];
			params[0] = job.getName();
			params[1] = job.getState();
			params[2] = job.getStartTime();
			params[3] = job.getEndTime();
			params[4] = job.getUser().getId();
			params[5] = "";
			params[6] = job.getType();
			params[7] = job.getDeletedOn();
			params[8] = job.getApplication();
			params[9] = job.getApplicationId();
			params[10] = job.getId();

			update(sql.toString(), params);

			log.debug("update job successful.");

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

			log.debug("delete job '" + job.getId() + "' successful.");

		} catch (SQLException e) {
			log.error("delete job '" + job.getId() + "' failed", e);
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public List<AbstractJob> findAllByUser(User user) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where user_id = ? ");
		sql.append("order by id desc ");

		Object[] params = new Object[1];
		params[0] = user.getId();

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			result = query(sql.toString(), params, new JobMapper(false, false));

			log.debug("find all jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all jobs failed", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<AbstractJob> findAllNotRetiredJobs() {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where state != ? ");
		sql.append("order by id desc ");

		List<AbstractJob> result = new Vector<AbstractJob>();

		Object[] params = new Object[1];
		params[0] = AbstractJob.STATE_RETIRED;

		try {

			result = query(sql.toString(), params, new JobMapper(true, false));

			log.debug("find all jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all jobs failed", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<AbstractJob> findAllNotNotifiedJobs() {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where state != ? AND state != ? AND state != ? ");
		sql.append("order by id desc ");

		List<AbstractJob> result = new Vector<AbstractJob>();

		Object[] params = new Object[3];
		params[0] = AbstractJob.STATE_RETIRED;
		params[1] = AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND;
		params[2] = AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND;

		try {

			result = query(sql.toString(), params, new JobMapper(true, false));

			log.debug("find all jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all jobs failed", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<AbstractJob> findAllNotifiedJobs() {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where  state = ? or state = ? ");
		sql.append("order by id desc ");

		Object[] params = new Object[2];
		params[0] = AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND;
		params[1] = AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND;

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			result = query(sql.toString(), params, new JobMapper(true, false));

			log.debug("find all old jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all old jobs failed", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<AbstractJob> findAllOlderThan(int time, int state) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where  state = ? AND DATEDIFF('ms',now(), DATEADD('SECOND', end_time / 1000 + ?, DATE '1970-01-01')) < 0  ");
		sql.append("order by id desc ");

		Object[] params = new Object[2];
		params[0] = state;
		params[1] = time;

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			result = query(sql.toString(), params, new JobMapper(true, false));

			log.debug("find all old jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all old jobs failed", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<AbstractJob> findAllByState(int state) {

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append("from job ");
		sql.append("where state = ? ");
		sql.append("order by id desc ");

		Object[] params = new Object[1];
		params[0] = state;

		List<AbstractJob> result = new Vector<AbstractJob>();

		try {

			result = query(sql.toString(), params, new JobMapper(true, false));

			log.debug("find all old jobs successful. results: " + result.size());

			return result;
		} catch (SQLException e) {
			log.error("find all old jobs failed", e);
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

			job = (AbstractJob) queryForObject(sql.toString(), params,
					new JobMapper(true, loadParams));

			if (job instanceof CloudgeneJob) {
				((CloudgeneJob) job).updateProgress();
			}

			if (job != null) {
				log.debug("find job by id '" + id + "' successful.");
			} else {
				log.debug("job '" + id + "' not found");
			}

			return job;
		} catch (SQLException e) {
			log.error("find job by id '" + id + "' failed", e);
			return null;
		}
	}

	class JobMapper implements IRowMapper {

		private boolean loadUser;

		private boolean loadDetails;

		private UserDao userDao;

		private ParameterDao parameterDao;

		private StepDao stepDao;

		public JobMapper(boolean loadUser, boolean loadDetails) {
			this.loadUser = loadUser;
			this.loadDetails = loadDetails;
			userDao = new UserDao(database);
			parameterDao = new ParameterDao(database);
			stepDao = new StepDao(database);
		}

		@Override
		public Object mapRow(ResultSet rs, int row) throws SQLException {
			int type = rs.getInt("type");
			AbstractJob job = new CloudgeneJob();
			job.setId(rs.getString("id"));
			job.setName(rs.getString("name"));
			job.setState(rs.getInt("state"));
			job.setStartTime(rs.getLong("start_time"));
			job.setEndTime(rs.getLong("end_time"));
			job.setDeletedOn(rs.getLong("deleted_on"));
			job.setApplication(rs.getString("application"));
			job.setApplicationId(rs.getString("application_id"));

			if (loadUser) {

				User user = userDao.findById(rs.getInt("user_id"));
				job.setUser(user);
			}

			if (loadDetails) {

				List<CloudgeneParameter> inputParams = parameterDao
						.findAllInputByJob(job);
				List<CloudgeneParameter> outputParams = parameterDao
						.findAllOutputByJob(job);
				job.setInputParams(inputParams);
				job.setOutputParams(outputParams);

				if (job instanceof CloudgeneJob) {

					List<CloudgeneStep> steps = stepDao
							.findAllByJob((CloudgeneJob) job);
					job.setSteps(steps);

				}
			}

			return job;
		}

	}

}
