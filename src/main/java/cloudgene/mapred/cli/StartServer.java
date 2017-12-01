package cloudgene.mapred.cli;

import cloudgene.mapred.Main;
import cloudgene.mapred.util.DockerHadoopCluster;
import cloudgene.mapred.util.HadoopCluster;
import genepi.base.Tool;

public class StartServer extends BaseTool {

	public static final String DEFAULT_DOCKER_IMAGE = "seppinho/cdh5-hadoop-mrv1";

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	public StartServer(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {
		addFlag("docker", "use docker hadoop cluster");
		addOptionalParameter("image", "use custom docker image [default: " + DEFAULT_DOCKER_IMAGE + "]", Tool.STRING);
		addOptionalParameter("host", "Hadoop namenode hostname [default: localhost]", Tool.STRING);
		addOptionalParameter("user", "Hadoop username [default: " + DEFAULT_HADOOP_USER + "]", Tool.STRING);
		addOptionalParameter("port", "running webinterface on this port [default: 8082", Tool.STRING);

	}

	@Override
	public int run() {

		if (getValue("host") != null) {

			String host = getValue("host").toString();

			String username = DEFAULT_HADOOP_USER;
			if (getValue("user") != null) {
				username = getValue("user").toString();
			}
			System.out.println("Use external Haddop cluster running on " + host + " with username " + username);
			HadoopCluster.init(host, username);

		} else if (isFlagSet("docker")) {

			String image = DEFAULT_DOCKER_IMAGE;
			if (getValue("image") != null) {
				image = getValue("image").toString();
			}

			DockerHadoopCluster cluster = new DockerHadoopCluster();
			try {
				cluster.start(image);
			} catch (Exception e) {
				printError("Error starting cluster.");
				printError(e.getMessage());
				return 1;
			}

			HadoopCluster.init(cluster.getIpAddress(), "cloudgene");

		} else {
			System.out.println("No external Haddop cluster set. Be sure cloudgene is running on your namenode");
		}

		Main main = new Main();
		try {
			String[] newArgs = new String[] {};
			if (getValue("port") != null) {
				newArgs = new String[] { "--port", getValue("port").toString() };
			}
			main.runCloudgene(newArgs);
			
			String port = "";
			if (getValue("port") != null){
				port = getValue("port").toString();
			}else{
				port = "8082";
			}
			System.out.println();
			System.out.println("Server is running on http://localhost:" + port );
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