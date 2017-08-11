package cloudgene.mapred.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class ApplicationInstaller {

	public static void install(List<Map<String, Object>> installation, Map<String, String> environment)
			throws IOException {
		for (Map<String, Object> commandObject : installation) {
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
		case "hdfs_import":
			String source = (String) parameters.get("source");
			String target = env((String) parameters.get("target"), environment);
			runHdfsImport(source, target);
			break;
		case "yaml_add":
			String file = env((String) parameters.get("file"), environment);
			String array = (String) parameters.get("array");
			Map<String, String> object = (Map<String, String>) parameters.get("object");
			for (String key : object.keySet()) {
				String value = env(object.get(key), environment);
				object.replace(key, value);
			}
			runYamlAdd(file, array, object);
			break;
		case "yaml_remove":
			String file1 = env((String) parameters.get("file"), environment);
			String array1 = (String) parameters.get("array");
			String key1 = null;
			String value1 = null;
			if (parameters.get("filter") != null) {
				String[] tiles = parameters.get("filter").toString().split("=");
				key1 = tiles[0].trim();
				value1 = tiles[1].trim();
			}
			runYamlRemove(file1, array1, key1, value1);
			break;
		default:
			throw new IOException("Unknown command '" + command + "'");
		}
	}

	public static void runYamlAdd(String file, String array, Map<String, String> object) throws IOException {

		Map<String, Object> data = new HashMap<String, Object>();
		if (new File(file).exists()) {
			System.out.println("Loading yaml from file " + file + "...");
			YamlReader reader = new YamlReader(new FileReader(file));
			data = reader.read(Map.class);
			reader.close();
		} else {
			System.out.println("File " + file + " not found. Start with empty.");
			data.put(array, new Vector<Object>());
		}

		List<Map> dataArray = findArray(data, array);
		if (dataArray != null) {
			dataArray.add(object);

			System.out.println("Write updated yaml to " + file + "...");
			YamlWriter writer = new YamlWriter(new FileWriter(file));
			writer.write(data);
			writer.close();
		} else {
			System.out.println("Array " + array + " not found.");
		}

	}

	public static void runYamlRemove(String file, String array, String key, String value) throws IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		if (new File(file).exists()) {
			System.out.println("Loading yaml from file " + file + "...");
			YamlReader reader = new YamlReader(new FileReader(file));
			data = reader.read(Map.class);
			reader.close();
			List<Map> dataArray = findArray(data, array);
			if (dataArray != null) {
				Map object = null;
				for (Map map : dataArray) {
					if (map.get(key).toString().equals(value)) {
						System.out.println("Object found");
						object = map;
					}
				}
				if (object != null) {
					dataArray.remove(object);
					System.out.println("Write updated yaml to " + file + "...");
					YamlWriter writer = new YamlWriter(new FileWriter(file));
					writer.write(data);
					writer.close();
				} else {
					System.out.println("No object found with for '" + key + ": " + value + "'");
				}
			} else {
				System.out.println("Array " + array + " not found.");
			}
		} else {
			System.out.println("File " + file + " not found. Nothing to do.");
		}
	}

	public static void runHdfsImport(String source, String target) throws IOException {
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

	public static List<Map> findArray(Map<String, Object> data, String array) {

		String[] tiles = array.split("\\.");
		for (int i = 0; i < tiles.length; i++) {
			String tile = tiles[i];
			String[] filterTiles = tile.split("\\[");
			String arrayName = filterTiles[0];
			String key = "";
			String value = "";
			if (filterTiles.length > 1) {
				String[] temp = filterTiles[1].split("=");
				key = temp[0];
				value = temp[1].replaceAll("\\]", "");
			}
			System.out.println("  " + arrayName + " (" + key + " " + value + ")");
			Object object = data.get(arrayName);
			if (object instanceof Map) {
				data = (Map) object;
				System.out.println("Found object " + arrayName);
			} else if (object instanceof List) {
				System.out.println("Found array " + arrayName);
				if (i == tiles.length - 1) {
					System.out.println("Array " + arrayName + " is result");
					return (List) object;
				}
				if (key.isEmpty()) {
					System.out.println("Error: array needs filter!");
					return null;
				}
				List list = (List) object;
				Map found = null;
				for (Object obj : list) {
					Map map = (Map) obj;
					if (map.get(key).toString().equals(value)) {
						System.out.println("item found");
						found = map;
					}
				}
				if (found == null) {
					System.out.println("item not found");
					return null;
				} else {
					data = found;
				}

			} else {
				return null;
			}

		}
		return null;
	}

	public static String env(String value, Map<String, String> variables) {

		for (String key : variables.keySet()) {
			value = value.replaceAll("\\$\\{" + key + "\\}", variables.get(key));
		}

		return value;
	}

}
