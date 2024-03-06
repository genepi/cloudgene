package cloudgene.mapred.util;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.jobs.JobResultsTreeItem;

public class JobResultsTreeUtil {

	public static List<JobResultsTreeItem> createTree(CloudgeneParameterOutput param) {
		List<JobResultsTreeItem> items = new Vector<JobResultsTreeItem>();
		for (Download file : param.getFiles()) {
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
				_items.sort(new JobsResultsTreeItemComparator());
			}
			JobResultsTreeItem item = new JobResultsTreeItem();
			item.setName(tiles[tiles.length - 1]);
			if (param.getHash() != null) {
				item.setPath("/browse/" + param.getHash() + "/" + file.getName());
			} else {
				item.setPath("/share/results/" + file.getHash() + "/" + file.getName());
			}
			item.setHash(file.getHash());
			item.setSize(file.getSize());
			item.setFolder(false);
			if (root == null) {
				items.add(item);
				items.sort(new JobsResultsTreeItemComparator());
			} else {
				root.getChilds().add(item);
				root.getChilds().sort(new JobsResultsTreeItemComparator());
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
	
	protected static class JobsResultsTreeItemComparator implements Comparator<JobResultsTreeItem> {
		@Override
		public int compare(JobResultsTreeItem arg0, JobResultsTreeItem arg1) {
			if (arg0.isFolder() != arg1.isFolder()) {
				return arg0.isFolder() ? -1 : 1;
			}
			
			return arg0.getName().compareToIgnoreCase(arg1.getName());
		}
	}

}
