package cloudgene.mapred.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.wdl.WdlApp;
import genepi.hadoop.HdfsUtil;
import genepi.hadoop.io.HdfsLineWriter;
import genepi.io.FileUtil;

public class ApplicationInstaller {

	public static boolean isInstalled(WdlApp app, Settings settings) {
		Map<String, String> environment = Environment.getApplicationVariables(app, settings);
		String target = environment.get("hdfs_app_folder");
		String installationFile = HdfsUtil.path(target, "installed");
		return HdfsUtil.exists(installationFile);
	}

	public static void uninstall(WdlApp app, Settings settings) throws IOException {
		Map<String, String> environment = Environment.getApplicationVariables(app, settings);
		String target = environment.get("hdfs_app_folder");
		HdfsUtil.delete(target);
	}

	public static void install(WdlApp app, Settings settings) throws IOException {
		Map<String, String> environment = Environment.getApplicationVariables(app, settings);
		String target = environment.get("hdfs_app_folder");
		String installationFile = HdfsUtil.path(target, "installed");
		HdfsUtil.delete(target);
		ApplicationInstaller.runCommands(app.getInstallation(), environment);
		HdfsLineWriter lineWriter = new HdfsLineWriter(installationFile);
		lineWriter.write(System.currentTimeMillis() + "");
		lineWriter.close();
	}

	public static void runCommands(List<Map<String, Object>> commands, Map<String, String> environment)
			throws IOException {

		for (Map<String, Object> commandObject : commands) {
			runCommand(commandObject, environment);
		}
	}

	public static void runCommand(Map<String, Object> commandObject, Map<String, String> environment)
			throws IOException {
		if (commandObject.keySet().size() == 1) {
			for (String command : commandObject.keySet()) {
				Object parametersObject = commandObject.get(command);
				Map<String, Object> parameters = (Map) parametersObject;
				runCommand(command, parameters, environment);
			}
		} else {
			throw new IOException("Unknown command structure.");
		}
	}

	public static void runCommand(String command, Map<String, Object> parameters, Map<String, String> environment)
			throws IOException {
		switch (command) {
		case "import":
			String source = Environment.env((String) parameters.get("source"), environment);
			String target = Environment.env((String) parameters.get("target"), environment);
			System.out.println("Import data from " + source + " to " + target + "...");
			runImportCommand(source, target);
			break;
		default:
			throw new IOException("Unknown command '" + command + "'");
		}
	}

	public static void runImportCommand(String source, String target) throws IOException {
		String tempFilename = FileUtil.path("temp", "hdfs_import.tempfile");
		if (source.startsWith("http://") || source.startsWith("https://")) {
			// download
			FileUtils.copyURLToFile(new URL(source), new File(tempFilename));

			if (source.endsWith(".zip")) {
				HdfsUtil.createDirectory(target);
				HdfsUtil.putZip(tempFilename, target);
			} else if (source.endsWith(".gz")) {
				HdfsUtil.createDirectory(target);
				HdfsUtil.putTarGz(tempFilename, target);
			} else {
				HdfsUtil.put(tempFilename, target);
			}

		} else {
			if (source.endsWith(".zip")) {
				HdfsUtil.createDirectory(target);
				HdfsUtil.putZip(source, target);
			} else if (source.endsWith(".gz")) {
				HdfsUtil.createDirectory(target);
				HdfsUtil.putTarGz(source, target);
			} else {
				HdfsUtil.put(source, target);
			}
		}

		FileUtil.deleteFile(tempFilename);
	}

}
