package cloudgene.mapred.util;

import java.io.IOException;

import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;
import genepi.io.FileUtil;

public class Application implements Comparable<Application> {

	private String filename;

	private String permission;

	private String id;

	private boolean syntaxError = false;

	private boolean changed = true;

	private WdlApp wdlApp = null;

	private String errorMessage = "";

	private boolean enabled = true;

	private String wdlContent;

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

	public void loadWdlApp() throws IOException {
		try {
			wdlApp = WdlReader.loadAppFromFile(getFilename());
			syntaxError = false;
			errorMessage = "";
			wdlContent = FileUtil.readFileAsString(getFilename());
		} catch (IOException e) {
			syntaxError = true;
			wdlApp = null;
			errorMessage = e.getMessage();
			wdlContent = FileUtil.readFileAsString(getFilename());
			throw e;
		}
	}

	public WdlApp getWdlApp() {
		return wdlApp;
	}

	public boolean hasSyntaxError() {
		return syntaxError;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public boolean isLoaded() {
		return wdlApp != null;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public void checkForChanges() {
		String newWdlContent = FileUtil.readFileAsString(getFilename());
		setChanged(!wdlContent.equals(newWdlContent));
	}

	public boolean isInstalled(String hdfsAppFolder) {
		return false;
	}

	public String getType() {
		if (isLoaded()) {
			if (wdlApp.getWorkflow() != null) {
				return "Application";
			} else {
				if (wdlApp.getCategory() != null && !wdlApp.getCategory().isEmpty()) {
					return wdlApp.getCategory();
				} else {
					return "Package";
				}
			}
		} else {
			return "-";
		}
	}

	@Override
	public int compareTo(Application o) {
		// sort by category
		int result = getType().compareTo(o.getType());
		if (result != 0) {
			return result;
		} else {
			// sort by id
			return getId().compareTo(o.getId());
		}
	}

}
