package cloudgene.mapred.api.v2.admin.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.hadoop.mapred.ClusterStatus;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import cloudgene.mapred.Main;
import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;
import cloudgene.mapred.util.HadoopCluster;
import cloudgene.mapred.util.RBinary;
import cloudgene.mapred.util.Technology;
import genepi.hadoop.HadoopUtil;
import genepi.hadoop.command.Command;
import genepi.hadoop.rscript.RScript;
import genepi.io.FileUtil;
import net.sf.json.JSONObject;

public class GetClusterDetails extends BaseResource {

	@Get
	public Representation get() {

		User user = getAuthUser();

		if (user == null) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires user authentication.");
		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation("The request requires administration rights.");
		}

		JSONObject object = new JSONObject();

		// general settigns
		object.put("maintenance", getSettings().isMaintenance());
		object.put("blocked", !getWorkflowEngine().isRunning());
		object.put("version", Main.VERSION);
		object.put("maintenance", getSettings().isMaintenance());
		object.put("blocked", !getWorkflowEngine().isRunning());
		object.put("threads_setup", getSettings().getThreadsSetupQueue());
		object.put("threads", getSettings().getThreadsQueue());
		object.put("max_jobs_user", getSettings().getMaxRunningJobsPerUser());
		URLClassLoader cl = (URLClassLoader) Main.class.getClassLoader();
		try {
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attr = manifest.getMainAttributes();
			String buildVesion = attr.getValue("Version");
			String buildTime = attr.getValue("Build-Time");
			String builtBy = attr.getValue("Built-By");
			object.put("built_by", builtBy);
			object.put("built_time", buildTime);

		} catch (IOException E) {
			// handle
		}

		// workspace and hdd
		File workspace = new File(getSettings().getLocalWorkspace());
		object.put("workspace_path", workspace.getAbsolutePath());
		object.put("free_disc_space", workspace.getUsableSpace() / 1024 / 1024 / 1024);
		object.put("total_disc_space", workspace.getTotalSpace() / 1024 / 1024 / 1024);

		// hadoop
		if (getSettings().isEnable(Technology.HADOOP_CLUSTER)) {
			try {

				try {
					object.put("hadoop_safemode", HadoopUtil.getInstance().isInSafeMode());
				} catch (Exception e) {
					object.put("hadoop_safemode", false);
				}

				ClusterStatus cluster = HadoopUtil.getInstance().getClusterDetails();
				StringBuffer state = new StringBuffer();
				state.append("JobTracker: " + HadoopCluster.getJobTracker() + "\n");
				state.append("Default FS: " + HadoopCluster.getDefaultFS() + "\n");
				state.append("State: " + cluster.getJobTrackerStatus().toString() + "\n");
				state.append("MapTask: " + cluster.getMaxMapTasks() + "\n");
				state.append("ReduceTask: " + cluster.getMaxReduceTasks() + "\n");
				state.append("Nodes\n");
				for (String tracker : cluster.getActiveTrackerNames()) {
					state.append("  " + tracker + "\n");
				}
				state.append("Blacklist:\n");
				for (String tracker : cluster.getBlacklistedTrackerNames()) {
					state.append("  " + tracker + "\n");
				}
				object.put("hadoop_details", state.toString());
				object.put("hadoop_enabled", true);

			} catch (Exception e) {
				object.put("hadoop_enabled", false);
				object.put("hadoop_error", "Hadoop cluster is unreachable");
			}
		} else {
			object.put("hadoop_enabled", false);
			object.put("hadoop_error", "Hadoop support is disabled. Please check your configuration.");
		}

		// r
		if (getSettings().isEnable(Technology.R)) {
			object.put("r_enabled", true);
			object.put("r_details", RBinary.getVersion());
		} else {
			object.put("r_enabled", false);
			object.put("r_error", "R support is disabled. Please check your configuration.");
		}

		if (getSettings().isEnable(Technology.R_MARKDOWN)) {
			object.put("rmarkdown_enabled", true);
			object.put("rmarkdown_details", "'knitr' and 'markdown' are installed.");
		} else {
			object.put("rmarkdown_enabled", false);
			object.put("rmarkdown_error", "R Markdown support is disabled. Please check your configuration.");
		}

		if (getSettings().isEnable(Technology.DOCKER)) {
			try {
				DockerClient docker = DefaultDockerClient.fromEnv().build();
				object.put("docker_enabled", true);
				object.put("docker_details", "Docker is installed and running (Client version: "
						+ docker.version().version() + ", Client API Version: " + docker.version().apiVersion() + ")");
				docker.close();
			} catch (DockerException | DockerCertificateException | InterruptedException e1) {
				object.put("docker_enabled", false);
				object.put("docker_error", "Docker support is disabled. " + e1.toString());
			}
		} else {
			object.put("docker_enabled", false);
			object.put("docker_error", "Docker support is disabled. Please install or start Docker.");
		}

		// database
		object.put("db_max_active", getDatabase().getDataSource().getMaxActive());
		object.put("db_active", getDatabase().getDataSource().getNumActive());
		object.put("db_max_idle", getDatabase().getDataSource().getMaxIdle());
		object.put("db_idle", getDatabase().getDataSource().getNumIdle());
		object.put("db_max_open_prep_statements", getDatabase().getDataSource().getMaxOpenPreparedStatements());

		return new StringRepresentation(object.toString(), MediaType.APPLICATION_JSON);

	}
}
