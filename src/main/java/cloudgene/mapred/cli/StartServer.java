package cloudgene.mapred.cli;

import org.apache.hadoop.mapred.ClusterStatus;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import cloudgene.mapred.Main;
import cloudgene.mapred.util.HadoopCluster;
import cloudgene.mapred.util.RBinary;
import cloudgene.mapred.util.Technology;
import genepi.base.Tool;
import genepi.hadoop.HadoopUtil;

public class StartServer extends BaseTool {

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	public StartServer(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {
		addOptionalParameter("host", "Hadoop namenode hostname [default: localhost]", Tool.STRING);
		addOptionalParameter("user", "Hadoop username [default: " + DEFAULT_HADOOP_USER + "]", Tool.STRING);
		addOptionalParameter("port", "running webinterface on this port [default: 8082]", Tool.STRING);
		addOptionalParameter("conf", "adoop configuration folder", Tool.STRING);
	}

	@Override
	public int run() {

		if (getValue("conf") != null) {

			String conf = getValue("conf").toString();

			String username = null;
			if (getValue("user") != null) {
				username = getValue("user").toString();
			}
			System.out.println(
					"Use Haddop configuration folder " + conf + (username != null ? " with username " + username : ""));
			HadoopCluster.setConfPath(conf, username);

		} else if (getValue("host") != null) {

			String host = getValue("host").toString();

			String username = DEFAULT_HADOOP_USER;
			if (getValue("user") != null) {
				username = getValue("user").toString();
			}
			System.out.println("Use external Haddop cluster running on " + host + " with username " + username);
			HadoopCluster.setHostname(host, username);

		} else {
			if (settings.getCluster() == null) {
				System.out.println("No external Haddop cluster set. Be sure cloudgene is running on your namenode");
			}
		}

		// check cluster status
		ClusterStatus details = HadoopUtil.getInstance().getClusterDetails();
		boolean hadoopSupport = true;
		if (details != null) {
			int nodes = details.getActiveTrackerNames().size();
			printText(0, spaces("[INFO]", 8) + "Cluster has " + nodes + " nodes, " + details.getMaxMapTasks()
					+ " map tasks and " + details.getMaxReduceTasks() + " reduce tasks");
			if (nodes == 0) {
				printText(0,
						spaces("[WARN]", 8) + "Cluster seems unreachable or misconfigured. Hadoop support disabled.");
				hadoopSupport = false;
			}
		} else {
			printText(0, spaces("[WARN]", 8) + "Cluster seems unreachable. Hadoop support disabled.");
			hadoopSupport = false;
		}

		if (!hadoopSupport) {
			settings.disable(Technology.HADOOP_CLUSTER);
		}

		if (!RBinary.isInstalled()) {
			printText(0, spaces("[WARN]", 8) + "RScript not found. R Markdown report disabled.");
			settings.disable(Technology.R);
			settings.disable(Technology.R_MARKDOWN);
		} else {
			if (!RBinary.isMarkdownInstalled()) {
				printText(0, spaces("[WARN]", 8) + "R Markdown packages not found. R Markdown report disabled.");
				settings.disable(Technology.R_MARKDOWN);
			}
		}

		try {
			DockerClient docker = DefaultDockerClient.fromEnv().build();
			docker.info();
			docker.close();
		} catch (DockerException | DockerCertificateException | InterruptedException e1) {
			settings.disable(Technology.DOCKER);
			printText(0, spaces("[WARN]", 8) + "Docker not found. Docker support disabled.");
		}

		Main main = new Main();
		try {
			String[] newArgs = new String[] {};
			if (getValue("port") != null) {
				newArgs = new String[] { "--port", getValue("port").toString() };
			}
			main.runCloudgene(settings, newArgs);

			String port = "";
			if (getValue("port") != null) {
				port = getValue("port").toString();
			} else {
				port = config.getPort();
			}
			System.out.println();
			System.out.println("Server is running on http://localhost:" + port);
			System.out.println();
			System.out.println("Please press ctrl-c to stop.");
			while (true) {
				Thread.sleep(5000000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
}