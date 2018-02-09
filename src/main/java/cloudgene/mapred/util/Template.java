package cloudgene.mapred.util;

public class Template {

	public static final String MAINTENANCE_MESSAGE = "MAINTENANCE_MESSAGE";

	public static final String FOOTER = "FOOTER";

	public static final String REGISTER_MAIL = "REGISTER_MAIL";

	public static final String RECOVERY_MAIL = "RECOVERY_MAIL";

	public static final String RETIRE_JOB_MAIL = "RETIRE_JOB_MAIL";

	public static final Template[] SNIPPETS = new Template[] {

			new Template(MAINTENANCE_MESSAGE,
					"Sorry, our service is currently under maintenance. Imputation Server is expected to be down until <b>Tuesday 08:00 AM EDT</b>."),

			new Template(FOOTER,
					"<p>powered by <a href=\"http://cloudgene.uibk.ac.at\">Cloudgene</a> and supported by the <a href=\"http://www.nih.gov\">U.S. National Institutes of Health</a> and the <a href=\"http://www.fwf.ac.at\">FWF Austrian Science Fund</a></p>"),

			new Template(REGISTER_MAIL,
					"Dear %s,\nThis email has been sent automatically by the \"%s\" system to confirm that your profile has now been registered.\n\n"
							+ "To confirm your email address, please click on this activation link %s"),

			new Template(RETIRE_JOB_MAIL,
					"Dear %s,\nYour job retires in %s days! All imputation results will be deleted at that time.\n\n"
							+ "Please ensure that you have downloaded all results from https://imputationserver.sph.umich.edu/start.html#!jobs/%s"),

			new Template(RECOVERY_MAIL, "Dear %s,\nThis email has been sent automatically by the \"%s\" system.\n\n"
					+ "To reset your password, please click on this link %s. "),

	};

	private String key;

	private String text;

	public Template(String key, String text) {
		this.key = key;
		this.text = text;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
