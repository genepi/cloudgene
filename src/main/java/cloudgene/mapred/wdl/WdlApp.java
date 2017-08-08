package cloudgene.mapred.wdl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class WdlApp extends WdlHeader implements Comparable<WdlApp> {

	private WdlMapReduce mapred;

	private Map<String, String> cluster;

	private List<Map<String, Object>> installation;

	private List<Map<String, Object>> deinstallation;

	public WdlMapReduce getMapred() {
		return mapred;
	}

	public void setMapred(WdlMapReduce mapred) {
		this.mapred = mapred;
	}

	public Map<String, String> getCluster() {
		return cluster;
	}

	public void setCluster(Map<String, String> cluster) {
		this.cluster = cluster;
	}

	public void setInstallation(List<Map<String, Object>> installation) {
		this.installation = installation;
	}

	public List<Map<String, Object>> getInstallation() {
		return installation;
	}

	public void setDeinstallation(List<Map<String, Object>> deinstallation) {
		this.deinstallation = deinstallation;
	}

	public List<Map<String, Object>> getDeinstallation() {
		return deinstallation;
	}

	public void install() throws IOException {
		// todo: set application folders etc....
		if (installation != null) {
			Map<String, String> environment = new HashMap<String, String>();
			for (Map<String, Object> commandObject : installation) {
				runCommand(commandObject, environment);
			}
		}
	}

	public void deinstall() throws IOException {
		// todo: set application folders etc....
		if (deinstallation != null) {
			Map<String, String> environment = new HashMap<String, String>();
			for (Map<String, Object> commandObject : deinstallation) {
				runCommand(commandObject, environment);
			}
		}
	}

	public void runCommand(Map<String, Object> commandObject, Map<String, String> environment) throws IOException {
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

	public void runCommand(String command, Map<String, Object> parameters, Map<String, String> environment)
			throws IOException {
		switch (command) {
		case "hdfs_import":
			String source = (String) parameters.get("source");
			String target = (String) parameters.get("target");
			runHdfsImport(source, target);
			break;
		case "yaml_add":
			System.out.println("update yaml file " + parameters.get("file"));

			break;
		case "yaml_remove":
			System.out.println("update yaml file " + parameters.get("file"));

			break;
		default:
			throw new IOException("Unknown command '" + command + "'");
		}
	}

	public void runHdfsImport(String source, String target) throws IOException {
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
			} else {
				HdfsUtil.put(source, target);
			}
		}

		FileUtil.deleteFile(tempFilename);
	}

	@Override
	public int compareTo(WdlApp o) {
		return getName().compareTo(o.getName());
	}

}
