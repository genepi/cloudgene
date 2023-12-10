package cloudgene.mapred.server.responses;

import java.util.List;
import cloudgene.mapred.wdl.WdlApp;
import com.fasterxml.jackson.annotation.JsonClassDescription;

@JsonClassDescription
public class WdlAppResponse {

	private String id;
	private String name;
	private String version;
	private String description;
	private String author;
	private String website;
	private String logo;
	private String submitButton;
	private List<WdlParameterInputResponse> params;
	private boolean s3Workspace;
	private String footer;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getSubmitButton() {
		return submitButton;
	}

	public void setSubmitButton(String submitButton) {
		this.submitButton = submitButton;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public boolean isS3Workspace() {
		return s3Workspace;
	}

	public void setS3Workspace(boolean s3Workspace) {
		this.s3Workspace = s3Workspace;
	}

	public List<WdlParameterInputResponse> getParams() {
		return params;
	}

	public void setParams(List<WdlParameterInputResponse> params) {
		this.params = params;
	}

	public static WdlAppResponse build(WdlApp app, List<WdlApp> apps) {
		WdlAppResponse response = new WdlAppResponse();
		response.setId(app.getId());
		response.setName(app.getName());
		response.setVersion(app.getVersion());
		response.setDescription(app.getDescription());
		response.setAuthor(app.getAuthor());
		response.setWebsite(app.getWebsite());
		if (app.getLogo() != null && !app.getLogo().isEmpty()) {
			response.setLogo(app.getLogo());
		}
		response.setSubmitButton(app.getSubmitButton());

		List<WdlParameterInputResponse> paramResponse = WdlParameterInputResponse.build(app.getWorkflow().getInputs(),
				apps);
		response.setParams(paramResponse);

		return response;
	}

}
