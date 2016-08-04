package cloudgene.mapred.util;

import genepi.hadoop.importer.FileItem;
import genepi.io.FileUtil;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class FileTree {

	public static FileItem[] getFileTree(String workspace, String name) {

		File file = new File(FileUtil.path(workspace, name));
		File[] files = file.listFiles();

		FileItem[] results = null;
		results = new FileItem[files.length];
		int count = 0;

		// folders
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				results[count] = new FileItem();
				results[count].setText(files[i].getName());
				results[count].setPath(name + "/" + files[i].getName());
				File[] files2 = files[i].listFiles();
				results[count].setId(name + "/" + files[i].getName());
				count++;
			}
		}

		// files
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				results[count] = new FileItem();
				results[count].setText(files[i].getName());
				results[count].setPath(name + "/" + files[i].getName());
				results[count].setId(name + "/" + files[i].getName());
				results[count].setSize(FileUtils
						.byteCountToDisplaySize(files[i].length()));
				count++;
			}
		}
		return results;

	}

}
