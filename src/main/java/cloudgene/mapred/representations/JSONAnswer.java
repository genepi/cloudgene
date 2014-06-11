package cloudgene.mapred.representations;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;

public class JSONAnswer extends StringRepresentation {

	public JSONAnswer(String text, boolean success) {

		super("", MediaType.TEXT_HTML);

		JSONObject answer = new JSONObject();
		try {
			answer.put("success", success);
			answer.put("message", text);
			answer.put("type", "plain");
			setText(answer.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

}
