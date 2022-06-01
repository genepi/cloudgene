package cloudgene.mapred.server.responses;

import cloudgene.mapred.core.User;

import java.util.Date;
import java.util.List;
import java.util.Vector;

public class UserResponse {

	private int id;

	private String username = "";

	private String fullName = "";

	private String lastLogin = "";

	private String lockedUntil = "";

	private boolean active = false;

	private int loginAttempts;

	private String role = "";

	private String mail = "";

	private boolean admin = false;

	private boolean hasApiToken = false;

	private String apiTokenMessage = "";

	private boolean apiTokenValid = true;

	public static final String MESSAGE_VALID_TOKEN = "API Token was created by %s and is valid until %s.";

	public static final String MESSAGE_EXPIRED_TOKEN = "API Token was created by %s and expired on %s.";

	public static UserResponse build(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setFullName(user.getFullName());
		response.setLastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : "");
		response.setLockedUntil(lockedUntilToString(user.getLockedUntil()));
		response.setActive(user.isActive());
		response.setLoginAttempts(user.getLoginAttempts());
		response.setRole(String.join(User.ROLE_SEPARATOR, user.getRoles()).toLowerCase());
		response.setMail(user.getMail());
		response.setAdmin(user.isAdmin());
		response.setHasApiToken(user.getApiToken() != null && !user.getApiToken().isEmpty());

		if (response.isHasApiToken() && user.getApiTokenExpiresOn() != null) {
			if (user.getApiTokenExpiresOn().getTime() > System.currentTimeMillis()) {
				response.setApiTokenValid(true);
				response.setApiTokenMessage(
						String.format(MESSAGE_VALID_TOKEN, user.getUsername(), user.getApiTokenExpiresOn()));
			} else {
				response.setApiTokenValid(false);
				response.setApiTokenMessage(
						String.format(MESSAGE_EXPIRED_TOKEN, user.getUsername(), user.getApiTokenExpiresOn()));
			}
		}

		return response;
	}

	public static List<UserResponse> build(List<User> users) {
		List<UserResponse> response = new Vector<UserResponse>();
		for (User user : users) {
			response.add(UserResponse.build(user));
		}
		return response;
	}

	public static String lockedUntilToString(Date date) {
		if (date != null) {
			if (date.after(new Date())) {
				return date.toString();
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

	public void setApiTokenMessage(String apiTokenMessage) {
		this.apiTokenMessage = apiTokenMessage;
	}

	public String getApiTokenMessage() {
		return apiTokenMessage;
	}

	public void setApiTokenValid(boolean apiTokenValid) {
		this.apiTokenValid = apiTokenValid;
	}

	public boolean isApiTokenValid() {
		return apiTokenValid;
	}

}
