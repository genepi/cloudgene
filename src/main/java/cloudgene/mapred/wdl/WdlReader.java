package cloudgene.mapred.wdl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.core.Category;
import cloudgene.mapred.util.FileUtil;
import cloudgene.mapred.util.Settings;

import com.esotericsoftware.yamlbeans.YamlReader;

public class WdlReader {

	public static WdlApp loadAppFromUrl(String filename) throws IOException {

		URL url2 = new URL(filename);
		HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
		YamlReader reader = new YamlReader(new InputStreamReader(
				conn.getInputStream()));

		reader.getConfig().setPropertyDefaultType(WdlApp.class, "mapred",
				WdlMapReduce.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"steps", WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"inputs", WdlParameterInput.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"outputs", WdlParameterOutput.class);

		WdlApp app = reader.read(WdlApp.class);

		updateApp(filename, app);

		return app;

	}

	public static WdlApp loadAppFromString(String filename, String content)
			throws IOException {

		YamlReader reader = new YamlReader(new StringReader(content));

		reader.getConfig().setPropertyDefaultType(WdlApp.class, "mapred",
				WdlMapReduce.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"steps", WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"inputs", WdlParameterInput.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"outputs", WdlParameterOutput.class);

		WdlApp app = reader.read(WdlApp.class);

		updateApp(filename, app);

		return app;

	}

	public static WdlApp loadAppFromFile(String filename) throws IOException {

		YamlReader reader = new YamlReader(new FileReader(filename));

		reader.getConfig().setPropertyDefaultType(WdlApp.class, "mapred",
				WdlMapReduce.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"steps", WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"inputs", WdlParameterInput.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class,
				"outputs", WdlParameterOutput.class);

		WdlApp app = reader.read(WdlApp.class);

		updateApp(filename, app);

		return app;

	}

	private static void updateApp(String filename, WdlApp app) {

		WdlMapReduce config = app.getMapred();

		if (config != null) {
			String jar = config.getJar();
			String mapper = config.getMapper();
			String reducer = config.getReducer();

			String path = new File(new File(filename).getAbsolutePath())
					.getParentFile().getAbsolutePath();
			config.setPath(path);
			config.setManifestFile(filename);

			// default step
			if (jar != null) {
				WdlStep step = new WdlStep();
				step.setJar(jar);
				step.setParams(config.getParams());
				config.getSteps().add(step);
			}

			if (mapper != null && reducer != null) {
				WdlStep step = new WdlStep();
				step.setMapper(mapper);
				step.setReducer(reducer);
				step.setParams(config.getParams());
				config.getSteps().add(step);
			}

		}

	}

	public static WdlApp loadApp(String name) {

		Settings settings = Settings.getInstance();

		File dir = new File(FileUtil.path(settings.getAppsPath(), name));

		File manifest = null;

		if (dir.isDirectory()) {
			// old style
			manifest = new File(FileUtil.path(dir.getAbsolutePath(),
					"cloudgene.yaml"));
		} else {
			// new style
			manifest = new File(FileUtil.path(settings.getAppsPath(), name)
					+ ".yaml");
		}

		if (manifest.exists()) {
			try {
				return loadAppFromFile(manifest.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {

			return null;
		}

	}

	public static List<Category> loadApps() {

		Settings settings = Settings.getInstance();

		File folder = new File(settings.getAppsPath());
		File[] listOfFiles = folder.listFiles();

		String filename = "";

		List<Category> result = new Vector<Category>();

		Map<String, List<WdlHeader>> categories = new HashMap<String, List<WdlHeader>>();

		for (int i = 0; i < listOfFiles.length; i++) {

			File dir = listOfFiles[i];
			filename = FileUtil.path(dir.getAbsolutePath(), "cloudgene.yaml");
			File manifest = new File(filename);

			// old style

			if (dir.isDirectory() && manifest.exists()) {
				filename = manifest.getAbsolutePath();
				try {

					WdlApp app = loadAppFromFile(filename);

					WdlHeader meta = (WdlHeader) app;

					if (meta != null && app.getMapred() != null) {

						meta.setId(dir.getName());

						List<WdlHeader> listApps = categories.get(meta
								.getCategory());
						if (listApps == null) {
							listApps = new Vector<WdlHeader>();
							categories.put(meta.getCategory(), listApps);
						}
						listApps.add(meta);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			// new style: all other yaml files.
			if (dir.isDirectory()) {
				File[] filesInDir = dir.listFiles();
				for (File file : filesInDir) {
					if (file.getName().endsWith(".yaml")
							&& !file.getName().equals("cloudgene.yaml")) {
						try {
							filename = file.getAbsolutePath();
							WdlApp app = loadAppFromFile(filename);

							WdlHeader meta = (WdlHeader) app;

							if (meta != null && app.getMapred() != null) {

								meta.setId(dir.getName() + "/"
										+ (file.getName()).replace(".yaml", ""));

								List<WdlHeader> listApps = categories
										.get(meta.getCategory());
								if (listApps == null) {
									listApps = new Vector<WdlHeader>();
									categories
											.put(meta.getCategory(), listApps);
								}
								listApps.add(meta);

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		for (String text : categories.keySet()) {
			Category tool = new Category();
			tool.setText(text);
			tool.setLeaf(false);

			List<WdlHeader> listApps = categories.get(text);
			WdlHeader[] children = new WdlHeader[listApps.size()];
			for (int i = 0; i < listApps.size(); i++) {
				children[i] = listApps.get(i);
			}
			tool.setChildren(children);
			result.add(tool);

		}

		Collections.sort(result);

		return result;

	}

}
