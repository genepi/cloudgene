package cloudgene.mapred.responses;

import java.util.Date;

import cloudgene.mapred.core.User;

public class UserResponse {

	int id;
	String username = "";
	String fullName = "";
	String lastLogin = "";
	String lockedUntil = "";
	boolean active = false;
	int loginAttempts;
	String role = "";
	String mail = "";
	boolean admin = false;
	boolean hasApiToken = false;

	public static UserResponse build(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setFullName(user.getFullName());
		response.setLastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : "");
		response.setLockedUntil(getLockedUntil(user));
		response.setActive(user.isActive());
		response.setLoginAttempts(user.getLoginAttempts());
		response.setRole(String.join(User.ROLE_SEPARATOR, user.getRoles()).toLowerCase());
		response.setMail(user.getMail());
		response.setAdmin(user.isAdmin());
		response.setHasApiToken(user.getApiToken() != null && !user.getApiToken().isEmpty());
		return response;
	}

	public static String getLockedUntil(User user) {
		if (user.getLockedUntil() != null) {
			if (user.getLockedUntil().after(new Date())) {
				return user.getLockedUntil().toString();
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setLastLogin(String lastLogin) {
		this.lastLogin = lastLogin;
	}

	public void setLockedUntil(String lockedUntil) {
		this.lockedUntil = lockedUntil;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setLoginAttempts(int loginAttempts) {
		this.loginAttempts = loginAttempts;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public void setHasApiToken(boolean hasApiToken) {
		this.hasApiToken = hasApiToken;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public String getFullName() {
		return fullName;
	}

	public String getLastLogin() {
		return lastLogin;
	}

	public String getLockedUntil() {
		return lockedUntil;
	}

	public boolean isActive() {
		return active;
	}

	public int getLoginAttempts() {
		return loginAttempts;
	}

	public String getRole() {
		return role;
	}

	public String getMail() {
		return mail;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isHasApiToken() {
		return hasApiToken;
	}

}
