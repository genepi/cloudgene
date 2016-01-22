package cloudgene.mapred.wdl;

public class WdlHeader {

	private boolean installed = false;
	private String source = "";
	private String description;
	private String version;
	private String website;
	private String name;
	private String category;
	private String author;
	private String id;

	public boolean isExpanded() {
		return false;
	}

	public boolean isLeaf() {
		return true;
	}

	public WdlHeader[] getChildren() {
		return null;
	}

	public boolean isInstalled() {
		return installed;
	}

	public String getText() {
		return getName();
	}

	public void setText(String text) {
		setName(text);
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

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

}
