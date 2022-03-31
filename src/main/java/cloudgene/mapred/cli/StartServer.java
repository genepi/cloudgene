package cloudgene.mapred.cli;

import cloudgene.mapred.server.Application;
import genepi.base.Tool;
import genepi.hadoop.HadoopCluster;
import io.micronaut.runtime.Micronaut;

public class StartServer extends Tool {

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	private String[] args;

	public StartServer(String[] args) {
		super(args);
		this.args = args;
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
			// if (settings.getCluster() == null) {
			// System.out.println("No external Haddop cluster set.");
			// }
		}

		try {
			Micronaut.run(Application.class, args);
			// TODO: check why we need this?
			System.out.println();
			System.out.println("Server is running");
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

	@Override
	public void init() {

	}

}