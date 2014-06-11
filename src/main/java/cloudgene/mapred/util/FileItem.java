package cloudgene.mapred.util;

public class FileItem extends ExtJsTreeItem {

	private String path;
	
	private String size;
	
	private boolean disabled = false;
	

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public void setSize(String size) {
		this.size = size;
	}
	
	public String getSize() {
		return size;
	}

}
