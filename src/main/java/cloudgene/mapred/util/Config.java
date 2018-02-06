package cloudgene.mapred.util;

public class Config {

	public static final String CONFIG_FILENAME = "cloudgene.conf";

	private String database = null;

	private String settings = "config/settings.yaml";

	private String apps = "apps";

	private String workspace = null;

	private String pages = "pages";

	private String port = "8082";
	
	private String version = "version.txt";

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getSettings() {
		return settings;
	}

	public void setSettings(String settings) {
		this.settings = settings;
	}

	public String getApps() {
		return apps;
	}

	public void setApps(String apps) {
		this.apps = apps;
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}
	
	public String getPort() {
		return port;
	}
	
	public void setPort(String port) {
		this.port = port;
	}

}
