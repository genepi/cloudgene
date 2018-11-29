package cloudgene.mapred.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.plugins.docker.DockerPlugin;
import cloudgene.mapred.plugins.hadoop.HadoopPlugin;
import cloudgene.mapred.plugins.rscript.RMarkdownPlugin;
import cloudgene.mapred.plugins.rscript.RScriptPlugin;
import cloudgene.mapred.util.Settings;

public class PluginManager {

	private List<IPlugin> plugins;

	private Map<String, IPlugin> pluginsIndex;

	private static PluginManager instance = null;

	public static PluginManager getInstance() {
		if (instance == null) {
			instance = new PluginManager();
		}
		return instance;
	}

	private PluginManager() {
		plugins = new Vector<IPlugin>();
		plugins.add(new HadoopPlugin());
		plugins.add(new DockerPlugin());
		plugins.add(new RScriptPlugin());
		plugins.add(new RMarkdownPlugin());
	}

	public boolean initPlugins(Settings settings) {
		pluginsIndex = new HashMap<String, IPlugin>();
		for (IPlugin plugin : plugins) {
			plugin.configure(settings);
			pluginsIndex.put(plugin.getId(), plugin);
		}

		return true;
	}

	public boolean isEnabled(IPlugin plugin) {
		return plugin.isInstalled();
	}

	public boolean isEnabled(String id) {
		IPlugin plugin = pluginsIndex.get(id);
		if (plugin != null) {
			return plugin.isInstalled();
		} else {
			return false;
		}
	}

	public List<IPlugin> getPlugins() {
		return plugins;
	}

}
