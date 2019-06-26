package cloudgene.mapred.cli;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.PluginManager;

public class ShowPlugins extends BaseTool {

	public ShowPlugins(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {
	}

	@Override
	public int run() {

		PluginManager plugins = PluginManager.getInstance();
		for (IPlugin plugin: plugins.getPlugins()) {
			if (plugin.isInstalled()) {
				System.out.println(plugin.getName());
				System.out.println("  " + plugin.getDetails().replace("\n", "\n  "));
				System.out.println();
			}
		}
		
		return 0;

	}
}