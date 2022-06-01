package cloudgene.mapred.util;

import genepi.io.FileUtil;

import java.io.File;
import java.util.Map;

public class BinaryFinder {

	private String location = null;

	private String name;

	public BinaryFinder(String name) {

		this.name = name;

	}

	public BinaryFinder env(String variable) {

		if (location != null) {
			return this;
		}
		String path = System.getenv(variable);
		if (path != null && !path.isEmpty()) {
			String binary = FileUtil.path(path, name);
			if (new File(binary).exists()) {
				location = binary;
			}
		}

		return this;

	}

	public BinaryFinder settings(Settings settings, String plugin, String key) {

		if (location != null) {
			return this;
		}
		if (settings != null) {
			Map<String, String> config = settings.getPlugin(plugin);
			if (config != null) {
				String path = config.get(key);
				if (path != null && !path.isEmpty()) {
					String binary = FileUtil.path(path, name);
					if (new File(binary).exists()) {
						location = binary;
					}
				}
			}
		}

		return this;

	}

	public BinaryFinder path(String path) {

		if (location != null) {
			return this;
		}

		String binary = FileUtil.path(path, name);
		if (new File(binary).exists()) {
			location = binary;
		}

		return this;
	}

	public BinaryFinder envPath() {
		if (location != null) {
			return this;
		}

		String envPath = System.getenv("PATH");
		if (envPath != null && !envPath.isEmpty()) {
			String[] paths = envPath.split(":");
			for (String path : paths) {
				String binary = FileUtil.path(path, name);
				if (new File(binary).exists()) {
					location = binary;
					return this;
				}
			}
		}

		return this;
	}

	public String find() {
		return location;
	}

}
