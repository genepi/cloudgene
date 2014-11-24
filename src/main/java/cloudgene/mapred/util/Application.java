package cloudgene.mapred.util;

public class Application {

	private String filename;

	private String permission;

	private String id;

	public Application(String id, String permission, String filename) {
		this.id = id;
		this.permission = permission;
		this.filename = filename;
	}

	public Application() {

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

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

}
