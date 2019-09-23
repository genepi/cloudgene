package cloudgene.mapred.util;

import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.JobResultsTreeItem;

public class JobResultsTreeUtil {

	public static List<JobResultsTreeItem> createTree(List<Download> files) {
		List<JobResultsTreeItem> items = new Vector<JobResultsTreeItem>();
		for (Download file : files) {
			String[] tiles = file.getName().split("/");
			JobResultsTreeItem root = null;
			for (int i = 0; i < tiles.length - 1; i++) {
				List<JobResultsTreeItem> _items = null;
				if (root == null) {
					_items = items;
				} else {
					_items = root.getChilds();
				}
				root = get(_items, tiles[i]);
				if (root == null) {
					root = new JobResultsTreeItem();
					root.setName(tiles[i]);
					root.setFolder(true);
					_items.add(root);
				}

			}
			JobResultsTreeItem item = new JobResultsTreeItem();
			item.setName(tiles[tiles.length - 1]);
			item.setPath(file.getPath());
			item.setHash(file.getHash());
			item.setSize(file.getSize());
			item.setFolder(false);
			if (root == null) {
				items.add(item);
			} else {
				root.getChilds().add(item);
			}
		}
		return items;
	}

	public static JobResultsTreeItem get(List<JobResultsTreeItem> items, String name) {
		for (JobResultsTreeItem item: items) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}

}
