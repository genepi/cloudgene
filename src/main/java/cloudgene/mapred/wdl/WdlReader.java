package cloudgene.mapred.wdl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlReader;

import cloudgene.mapred.util.ApplicationInstaller;
import genepi.io.FileUtil;

public class WdlReader {

	public static WdlApp loadAppFromString(String filename, String content) throws IOException {

		YamlReader reader = new YamlReader(new StringReader(content));

		reader.getConfig().setPropertyDefaultType(WdlApp.class, "workflow", WdlWorkflow.class);
		reader.getConfig().setPropertyDefaultType(WdlApp.class, "mapred", WdlWorkflow.class);
		reader.getConfig().setPropertyElementType(WdlWorkflow.class, "steps", WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlWorkflow.class, "setups", WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlWorkflow.class, "inputs", WdlParameterInput.class);
		reader.getConfig().setPropertyElementType(WdlWorkflow.class, "outputs", WdlParameterOutput.class);

		WdlApp app = reader.read(WdlApp.class);
		reader.close();

		updateApp(filename, app);

		return app;

	}

	public static WdlApp loadAppFromFile(String filename) throws IOException {

		YamlReader reader = new YamlReader(new FileReader(filename));

		reader.getConfig().setPropertyDefaultType(WdlApp.class, "workflow", WdlWorkflow.class);
		reader.getConfig().setPropertyDefaultType(WdlApp.class, "mapred", WdlWorkflow.class);
		reader.getConfig().setPropertyElementType(WdlWorkflow.class, "steps", WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlWorkflow.class, "setups", WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlWorkflow.class, "inputs", WdlParameterInput.class);
		reader.getConfig().setPropertyElementType(WdlWorkflow.class, "outputs", WdlParameterOutput.class);

		WdlApp app = reader.read(WdlApp.class);
		reader.close();

		updateApp(filename, app);

		return app;

	}

	private static void updateApp(String filename, WdlApp app) {

		WdlWorkflow config = app.getWorkflow();

		if (config != null) {
			String jar = config.getJar();
			String mapper = config.getMapper();
			String reducer = config.getReducer();

			String path = new File(new File(filename).getAbsolutePath()).getParentFile().getAbsolutePath();
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

			// load values from files
			if (config.getInputs() != null) {
				for (WdlParameter input : config.getInputs()) {
					if (input.getType().toLowerCase().equals("list")) {
						Map<String, String> values = input.getValues();
						if (values != null) {
							String source = values.get("source");
							String array = values.get("array");
							String key = values.get("key");
							String value = values.get("value");
							if (source != null && array != null && key != null && value != null) {
								input.setValues(new HashMap<String, String>());
								try {
									String sourceFilename = FileUtil.path(path, source);
									if (new File(sourceFilename).exists()) {
										YamlReader reader = new YamlReader(new FileReader(sourceFilename));
										Map<String, Object> data = reader.read(Map.class);
										List<Map> list = ApplicationInstaller.findArray(data, array);
										if (list != null) {
											Map<String, String> newValues = new HashMap<String, String>();
											for (Map map : list) {
												if (map.get(key) != null && map.get(value) != null) {
													String newKey = map.get(key).toString();
													String newValue = map.get(value).toString();
													newValues.put(newKey, newValue);
												}
											}
											input.setValues(newValues);
										}
										reader.close();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						}
					}
				}
			}

		}

	}

}
