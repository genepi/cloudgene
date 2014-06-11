package cloudgene.mapred.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileUtil {

	public static boolean deleteFile(String filename) {
		return new File(filename).delete();
	}

	public static void deleteDirectory(String directory) {

		try {
			FileUtils.deleteDirectory(new File(directory));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static boolean createDirectory(String directory) {
		if (!new File(directory).exists()) {
			new File(directory).mkdirs();
			return true;
		} else {
			return false;
		}
	}

	public static String path(String... paths) {
		String result = "";
		for (int i = 0; i < paths.length; i++) {
			String path = paths[i];
			if (path != null && !path.isEmpty()) {
				if (i > 0 && !path.startsWith(File.separator)
						&& !result.endsWith(File.separator)) {
					if (result.isEmpty()) {
						result += path;
					} else {
						result += File.separator + path;
					}
				} else {
					result += path;
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(path("", "hadoop"));
		System.out.println(path(null, "hadoop"));
	}

	public static String getFilename(String filename) {
		return new File(filename).getName();
	}

	public static void writeStringBufferToFile(String filename,
			StringBuffer buffer) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			String outText = buffer.toString();
			out.write(outText);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String readFileAsString(String filePath) {
		try {
			StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return fileData.toString();
		} catch (Exception e) {

			return "";
		}
	}

	/*public static String getOutputFilename(int id) {
		String path = Settings.getInstance().getOutputPath();
		return path(path, "output_" + id + ".txt");
	}*/


	public static String getTempFilename(String filename) {
		String path = Settings.getInstance().getTempPath();
		String name = getFilename(filename);
		return path(path, name);
	}

}
