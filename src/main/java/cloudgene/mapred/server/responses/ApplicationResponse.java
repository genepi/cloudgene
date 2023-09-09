package cloudgene.mapred.server.responses;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import genepi.io.FileUtil;

public class ApplicationResponse {

	private String id = "";

	private boolean enabled = false;

	private String filename = "";

	private boolean loaded2 = false;

	private String errorMessage = "";

	private boolean changed = false;

	private String permission = "";

	private WdlApp wdlApp = null;

	private String source = "";

	private List<Environment.Variable> environment;

	private Map<String, String> config;

	public static ApplicationResponse build(Application app) {

		ApplicationResponse appResponse = new ApplicationResponse();
		appResponse.setId(app.getId());
		appResponse.setEnabled(app.isEnabled());
		appResponse.setFilename(app.getFilename());
		appResponse.setLoaded(app.isLoaded());
		appResponse.setErrorMessage(app.getErrorMessage());
		appResponse.setChanged(app.isChanged());
		appResponse.setPermission(app.getPermission());
		// TODO: check if we need wdl app and file? only in details?
		appResponse.setWdlApp(app.getWdlApp());

		if (new File(app.getFilename()).exists()) {
			appResponse.setSource(FileUtil.readFileAsString(app.getFilename()));
		}

		return appResponse;

	}

	public static ApplicationResponse buildWithDetails(Application app, Settings settings,
			ApplicationRepository repository) {

		ApplicationResponse appResponse = build(app);

		List<Environment.Variable> environment = settings.buildEnvironment().addApplication(app.getWdlApp()).toList();
		appResponse.setEnvironment(environment);

		Map<String, String> updatedConfig = repository.getConfig(app.getWdlApp());
		appResponse.setConfig(updatedConfig);

		return appResponse;

	}

	public static List<ApplicationResponse> buildWithDetails(List<Application> applications, Settings settings,
			ApplicationRepository repository) {
		List<ApplicationResponse> response = new Vector<ApplicationResponse>();
		for (Application app : applications) {
			response.add(ApplicationResponse.build(app));
		}
		return response;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean isLoaded() {
		return loaded2;
	}

	public void setLoaded(boolean loaded) {
		this.loaded2 = loaded;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public WdlApp getWdlApp() {
		return wdlApp;
	}

	public void setWdlApp(WdlApp wdlApp) {
		this.wdlApp = wdlApp;
	}

	public List<Environment.Variable> getEnvironment() {
		return environment;
	}

	public void setEnvironment(List<Environment.Variable> environment) {
		this.environment = environment;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

}
