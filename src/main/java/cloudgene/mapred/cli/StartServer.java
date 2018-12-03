package cloudgene.mapred.cli;

import cloudgene.mapred.Main;
import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.PluginManager;
import genepi.base.Tool;
import genepi.hadoop.HadoopCluster;
import genepi.hadoop.HadoopUtil;

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
				port = settings.getPort();
			}

			// show supported plugins
			PluginManager manager = PluginManager.getInstance();
			manager.initPlugins(settings);
			for (IPlugin plugin: manager.getPlugins()) {
				if (manager.isEnabled(plugin)) {
					printText(0, spaces("[INFO]", 8) + plugin.getStatus());
				}else {
					printText(0, spaces("[WARN]", 8) + plugin.getStatus());
				}
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