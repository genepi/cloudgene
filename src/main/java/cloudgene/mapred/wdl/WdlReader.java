package cloudgene.mapred.wdl;

import genepi.io.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.Application;
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
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "steps",
				WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "inputs",
				WdlParameterInput.class);
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
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "steps",
				WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "inputs",
				WdlParameterInput.class);
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
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "steps",
				WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "inputs",
				WdlParameterInput.class);
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

		File dir = new File(name);

		File manifest = null;

		if (dir.isDirectory()) {
			// old style
			manifest = new File(FileUtil.path(dir.getAbsolutePath(),
					"cloudgene.yaml"));
		} else {
			// new style
			manifest = new File(name + ".yaml");
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

	public static List<WdlHeader> loadApps(User user, Settings setttings) {

		List<WdlHeader> listApps = new Vector<WdlHeader>();

		String appPath = "apps";

		if (new File(appPath).exists()) {

			File folder = new File(appPath);
			File[] listOfFiles = folder.listFiles();

			String filename = "";

			for (int i = 0; i < listOfFiles.length; i++) {

				File dir = listOfFiles[i];
				filename = FileUtil.path(dir.getAbsolutePath(),
						"cloudgene.yaml");
				File manifest = new File(filename);

				// old style

				if (dir.isDirectory() && manifest.exists()) {
					filename = manifest.getAbsolutePath();
					try {

						WdlApp app = loadAppFromFile(filename);

						WdlHeader meta = (WdlHeader) app;

						if (meta != null && app.getMapred() != null) {
							meta.setId(dir.getName());
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

									meta.setId(dir.getName());
									listApps.add(meta);

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

		}

		for (Application application : setttings.getApps()) {

			boolean using = true;

			if (!user.isAdmin()) {

				if (!application.getPermission().toLowerCase()
						.equals(user.getRole().toLowerCase())) {
					using = false;
				}
			}

			if (using) {

				String filename = application.getFilename();

				try {
					WdlApp app = loadAppFromFile(filename);
					WdlHeader meta = (WdlHeader) app;
					app.setId(application.getId());
					listApps.add(meta);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		return listApps;

	}

}
