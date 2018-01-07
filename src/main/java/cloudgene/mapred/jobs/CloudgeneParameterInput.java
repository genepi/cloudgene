package cloudgene.mapred.jobs;

import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;

public class CloudgeneParameterInput {

	private int id;

	private String description;

	private String value = "";

	private WdlParameterInputType type;

	private String name = "";

	private CloudgeneJob job;

	private String jobId;

	private boolean adminOnly = false;

	public CloudgeneParameterInput() {

	}

	public CloudgeneParameterInput(WdlParameterInput parameter) {
		setName(parameter.getId());
		setValue(parameter.getValue());
		setType(parameter.getTypeAsEnum());
		setDescription(parameter.getDescription());
		setAdminOnly(parameter.isAdminOnly());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public WdlParameterInputType getType() {
		return type;
	}

	public void setType(WdlParameterInputType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setJob(CloudgeneJob job) {
		this.job = job;
	}

	public CloudgeneJob getJob() {
		return job;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public boolean isAdminOnly() {
		return adminOnly;
	}

}
