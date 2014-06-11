package cloudgene.mapred.util;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HdfsTree {

	public static HdfsItem[] getFileTree(String workspace, String name,
			boolean details) {

		Configuration conf = new Configuration();
		HdfsItem[] results = null;
		try {
			FileSystem fileSystem = FileSystem.get(conf);

			FileStatus[] files = fileSystem.listStatus(new Path(HdfsUtil.path(
					workspace, name)));

			int count = 0;
			if (details && !name.equals("")) {
				results = new HdfsItem[files.length + 1];

				String[] tiles = name.replaceFirst("/","").split("/", 2);

				String parent = name.substring(0, name.lastIndexOf("/"));

				results[count] = new HdfsItem();
				results[count].setText("..");
				results[count].setPath(parent);
				results[count].setId(name+"...");
				results[count].setLeaf(false);
				count++;
			} else {
				results = new HdfsItem[files.length];
			}

			// folders
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDir()) {
					results[count] = new HdfsItem();
					results[count].setText(files[i].getPath().getName());
					results[count].setPath(name + "/"
							+ files[i].getPath().getName());
					results[count].setId(name + "/"
							+ files[i].getPath().getName());
					results[count].setLeaf(false);
					count++;
				}
			}

			// files
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDir()) {
					results[count] = new HdfsItem();
					results[count].setText(files[i].getPath().getName());
					results[count].setPath(name + "/"
							+ files[i].getPath().getName());
					results[count].setId(name + "/"
							+ files[i].getPath().getName());
					results[count].setLeaf(!files[i].isDir());
					if (details) {
						results[count].setSize(FileUtils
								.byteCountToDisplaySize(files[i].getLen()));
						results[count].setModificationTime(new Date(files[i]
								.getModificationTime()).toString());
					}
					count++;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return results;

	}

	public static HdfsItem[] getFolderTree(String workspace, String name) {

		Configuration conf = new Configuration();
		HdfsItem[] results = null;
		try {
			FileSystem fileSystem = FileSystem.get(conf);

			FileStatus[] files = fileSystem.listStatus(new Path(HdfsUtil.path(
					workspace, name)));
			int countFolders = 0;
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDir()) {
						countFolders++;
					}
				}
			}

			if (countFolders > 0) {

				results = new HdfsItem[countFolders];
				countFolders = 0;
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDir()) {
						results[countFolders] = new HdfsItem();
						results[countFolders].setText(files[i].getPath()
								.getName());
						results[countFolders].setPath(name + "/"
								+ files[i].getPath().getName());
						results[countFolders].setId(name + "/"
								+ files[i].getPath().getName());
						results[countFolders].setLeaf(false);
						countFolders++;
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return results;

	}

}
