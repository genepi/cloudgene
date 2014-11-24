package cloudgene.mapred.util;

public class Application {

	private String filename;

	private String permission;
	
	public  Application(String permission, String filename) {
		this.permission = permission;
		this.filename = filename;
	}
	
	public Application(){
		
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

}
