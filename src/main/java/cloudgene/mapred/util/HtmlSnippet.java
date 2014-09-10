package cloudgene.mapred.util;

public class HtmlSnippet {

	public static final String MAINTENANCE_MESSAGE = "MAINTENANCE_MESSAGE";

	public static final String FOOTER = "FOOTER";

	private String key;

	private String text;

	public HtmlSnippet(String key, String text) {
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
