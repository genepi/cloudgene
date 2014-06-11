package cloudgene.mapred.core;

public class User {
	private String username;
	private String pwd;
	private int id;
	private String awsKey = "";
	private String awsSecretKey = "";
	private String fullName = "";
	private String mail;
	private String role;
	private boolean saveCredentials = false;
	private boolean exportToS3 = false;
	private boolean exportInputToS3 = false;
	private String s3Bucket = null;
	private boolean active = true;
	private String activationKey = null;

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String pwd) {
		this.pwd = pwd;
	}

	public String getPassword() {
		return pwd;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAwsKey() {
		return awsKey;
	}

	public void setAwsKey(String awsKey) {
		this.awsKey = awsKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setSaveCredentials(boolean saveCredentials) {
		this.saveCredentials = saveCredentials;
	}

	public boolean isSaveCredentials() {
		return saveCredentials;
	}

	public void setExportToS3(boolean exportToS3) {
		this.exportToS3 = exportToS3;
	}

	public boolean isExportToS3() {
		return exportToS3;
	}

	public void setExportInputToS3(boolean exportInputToS3) {
		this.exportInputToS3 = exportInputToS3;
	}

	public boolean isExportInputToS3() {
		return exportInputToS3;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMail() {
		return mail;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}

	public boolean isAdmin() {
		if (role != null) {
			return role.equalsIgnoreCase("admin");
		} else {
			return false;
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getActivationCode() {
		return activationKey;
	}

	public void setActivationCode(String activationKey) {
		this.activationKey = activationKey;
	}

	@Override
	public boolean equals(Object obj) {
		return ((User) obj).getUsername().equals(username);
	}

}
