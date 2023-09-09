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

	private String state = "";

	private Map<String, String> environment;

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
		appResponse.setWdlApp(app.getWdlApp());

		if (new File(app.getFilename()).exists()) {
			appResponse.setSource(FileUtil.readFileAsString(app.getFilename()));
		}

		return appResponse;

	}

	public static ApplicationResponse buildWithDetails(Application app, Settings settings,
			ApplicationRepository repository) {

		ApplicationResponse appResponse = build(app);

		appResponse.setState(updateState(app, settings));

		Map<String, String> environment = settings.buildEnvironment().addApplication(app.getWdlApp()).toMap();
		appResponse.setEnvironment(environment);

		Map<String, String> updatedConfig = repository.getConfig(app.getWdlApp());
		appResponse.setConfig(updatedConfig);

		return appResponse;

	}

	public static List<ApplicationResponse> buildWithDetails(List<Application> applications, Settings settings,
			ApplicationRepository repository) {
		List<ApplicationResponse> response = new Vector<ApplicationResponse>();
		for (Application app : applications) {
			response.add(ApplicationResponse.buildWithDetails(app, settings, repository));
		}
		return response;
	}

	private static String updateState(Application app, Settings settings) {
		// TODO: remove
		return "completed";
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public WdlApp getWdlApp() {
		return wdlApp;
	}

	public void setWdlApp(WdlApp wdlApp) {
		this.wdlApp = wdlApp;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

}
