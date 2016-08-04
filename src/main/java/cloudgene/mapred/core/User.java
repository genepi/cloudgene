package cloudgene.mapred.core;

public class User {

	private String username;

	private String password;

	private int id;

	private String fullName = "";

	private String mail;

	private String role;

	private boolean active = true;

	private String activationKey = null;

	private String apiToken = "";

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String pwd) {
		this.password = pwd;
	}

	public String getPassword() {
		return password;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
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

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public String getApiToken() {
		return apiToken;
	}

	@Override
	public boolean equals(Object obj) {
		return ((User) obj).getUsername().equals(username);
	}

}
