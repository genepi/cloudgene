package cloudgene.mapred.core;

import java.util.Arrays;

import cloudgene.mapred.util.ExtJsTreeItem;
import cloudgene.mapred.wdl.WdlHeader;

public class Category extends ExtJsTreeItem implements Comparable<Category> {

	private WdlHeader[] children;

	public boolean isExpanded() {
		return false;
	}

	public boolean isLeaf() {
		return false;
	}

	public void setChildren(WdlHeader[] children) {
		this.children = children;
		sort();
	}

	public WdlHeader[] getChildren() {
		return children;
	}

	public void sort(){
		Arrays.sort(children);
	}
	
	@Override
	public int compareTo(Category o) {
		return getText().compareTo(o.getText());
	}

}
