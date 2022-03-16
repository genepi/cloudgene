package cloudgene.mapred.cli;

import cloudgene.mapred.Application;
import genepi.base.Tool;
import genepi.hadoop.HadoopCluster;

public class StartServer extends BaseTool {

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	public StartServer(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {
		addOptionalParameter("user", "Hadoop username [default: " + DEFAULT_HADOOP_USER + "]", Tool.STRING);
		addOptionalParameter("port", "running webinterface on this port [default: 8082]", Tool.STRING);
		addOptionalParameter("conf", "Hadoop configuration folder", Tool.STRING);
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
			HadoopCluster.setConfPath("Unknown", conf, username);

		} else {
			if (settings.getCluster() == null) {
				System.out.println("No external Haddop cluster set.");
			}
		}

		try {
			Application application = new Application();

			System.out.println("TODO!!");
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
	
}