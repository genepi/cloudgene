package cloudgene.mapred.server.responses;

import java.util.Date;

import cloudgene.mapred.core.User;

public class ValidatedApiTokenResponse {

	private boolean valid = false;
	
	private String message;

	private String username;
	
	private String name;
	
	private String mail;
	
	private Date expire = new Date();
	
	protected ValidatedApiTokenResponse(String message, boolean valid) {
		this.message = message;
		this.valid = valid;
	}
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setMail(String mail) {
		this.mail = mail;
	}
	
	public String getMail() {
		return mail;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public Date getExpire() {
		return expire;
	}
	
	public void setExpire(Date expire) {
		this.expire = expire;
	}
	
	public static ValidatedApiTokenResponse valid(String message, User user) {
		ValidatedApiTokenResponse response = new ValidatedApiTokenResponse(message, true);
		response.setMail(user.getMail());
		response.setName(user.getFullName());
		response.setUsername(user.getUsername());
		return response;
	}
	
	
	public static ValidatedApiTokenResponse error(String message) {
		return new ValidatedApiTokenResponse(message, false);
	}
}
