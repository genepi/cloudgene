package cloudgene.mapred.wdl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import com.esotericsoftware.yamlbeans.YamlReader;

public class WdlReader {


	public static WdlApp loadAppFromString(String filename, String content)
			throws IOException {

		YamlReader reader = new YamlReader(new StringReader(content));

		reader.getConfig().setPropertyDefaultType(WdlApp.class, "mapred",
				WdlMapReduce.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "steps",
				WdlStep.class);
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "setups",
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
		reader.getConfig().setPropertyElementType(WdlMapReduce.class, "setups",
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

}
