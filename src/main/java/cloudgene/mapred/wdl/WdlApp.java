package cloudgene.mapred.wdl;

import java.util.List;
import java.util.Map;

public class WdlApp implements Comparable<WdlApp>{

	private String source = "";

	private String description;

	private String version;

	private String website;

	private String name;

	private String category;

	private String author;

	private String id;

	private String submitButton = "Submit Job";

	private WdlWorkflow workflow;

	private Map<String, String> cluster;

	private List<Map<String, Object>> installation;

	private List<Map<String, Object>> deinstallation;

	private Map<String, String> properties;

	private String path;

	private String manifestFile;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Deprecated
	public WdlWorkflow getMapred() {
		return null;
	}

	@Deprecated
	public void setMapred(WdlWorkflow mapred) {
		this.workflow = mapred;
	}

	public WdlWorkflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(WdlWorkflow workflow) {
		this.workflow = workflow;
	}

	public Map<String, String> getCluster() {
		return cluster;
	}

	public void setCluster(Map<String, String> cluster) {
		this.cluster = cluster;
	}

	public void setInstallation(List<Map<String, Object>> installation) {
		this.installation = installation;
	}

	public List<Map<String, Object>> getInstallation() {
		return installation;
	}

	public void setDeinstallation(List<Map<String, Object>> deinstallation) {
		this.deinstallation = deinstallation;
	}

	public List<Map<String, Object>> getDeinstallation() {
		return deinstallation;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setSubmitButton(String submitButton) {
		this.submitButton = submitButton;
	}

	public String getSubmitButton() {
		return submitButton;
	}

	/* intern variables */

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setManifestFile(String manifestFile) {
		this.manifestFile = manifestFile;
	}

	public String getManifestFile() {
		return manifestFile;
	}

	public boolean needsInstallation() {
		return getInstallation() != null && getInstallation().size() > 0;
	}
	
	@Override
	public int compareTo(WdlApp o) {
			return getName().compareTo(o.getName());
	}

}
