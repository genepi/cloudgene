package cloudgene.mapred.wdl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import com.esotericsoftware.yamlbeans.YamlReader;

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

	private static void updateApp(String filename, WdlApp app) throws IOException {

		String path = new File(new File(filename).getAbsolutePath()).getParentFile().getAbsolutePath();
		app.setPath(path);
		app.setManifestFile(filename);
		app.setId(FileUtil.getFilename(app.getManifestFile()).replaceAll(".yaml", ""));
		// check mandatory fields errors
		/*
		 * if (app.getId() == null || app.getId().isEmpty()) { throw new
		 * IOException("No field 'id' found in file '" + filename + "'."); }
		 */
		if (app.getVersion() == null || app.getVersion().isEmpty()) {
			throw new IOException("No field 'version' found in file '" + filename + "'.");
		}
		if (app.getName() == null || app.getName().isEmpty()) {
			throw new IOException("No field 'name' found in file '" + filename + "'.");
		}
		// check id format
	}

}
