package cloudgene.mapred.server.responses;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.mysql.cj.xdevapi.UpdateStatement;
import com.sun.source.doctree.ReturnTree;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.apps.ApplicationInstaller;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.Environment;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import genepi.io.FileUtil;

public class ApplicationResponse {

	String id = "";
	boolean enabled = false;
	String filename = "";
	boolean loaded = false;
	String errorMessage = "";
	boolean changed = false;
	String permission = "";
	WdlApp app = null;
	String source = "";
	String state = "";
	Map<String, String> environmentMap;
	Map<String, String> configMap;

	public static ApplicationResponse build(Application app, Settings settings) {

		ApplicationResponse appResponse = new ApplicationResponse();
		appResponse.setId(app.getId());
		appResponse.setEnabled(app.isEnabled());
		appResponse.setFilename(app.getFilename());
		appResponse.setLoaded(app.isLoaded());
		appResponse.setErrorMessage(app.getErrorMessage());
		appResponse.setChanged(app.isChanged());
		appResponse.setPermission(app.getPermission());
		appResponse.setApp(app.getWdlApp());

		if (new File(app.getFilename()).exists()) {
			appResponse.setSource(FileUtil.readFileAsString(app.getFilename()));
		}
		appResponse.setState(updateState(app, settings));

		Map<String, String> environment = Environment.getApplicationVariables(app.getWdlApp(), settings);
		appResponse.setEnvironmentMap(environment);

		return appResponse;

	}

	public static List<ApplicationResponse> build(List<Application> applications, Settings settings) {
		List<ApplicationResponse> response = new Vector<ApplicationResponse>();
		for (Application app : applications) {
			response.add(ApplicationResponse.build(app, settings));
		}
		return response;
	}

	private static String updateState(Application app, Settings settings) {
		WdlApp wdlApp = app.getWdlApp();
		if (wdlApp != null) {
			if (wdlApp.needsInstallation()) {
				boolean installed = ApplicationInstaller.isInstalled(wdlApp, settings);
				if (installed) {
					return "completed";
				} else {
					return "on demand";
				}
			} else {
				return "n/a";
			}
		}
		return "";
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
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
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

	public WdlApp getApp() {
		return app;
	}

	public void setApp(WdlApp app) {
		this.app = app;
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

	public Map<String, String> getEnvironmentMap() {
		return environmentMap;
	}

	public void setEnvironmentMap(Map<String, String> environmentMap) {
		this.environmentMap = environmentMap;
	}

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}

}
