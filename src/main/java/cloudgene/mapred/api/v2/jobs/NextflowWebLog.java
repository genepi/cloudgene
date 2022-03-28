package cloudgene.mapred.api.v2.jobs;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.plugins.nextflow.NextflowInfo;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import net.sf.json.JSONObject;

public class NextflowWebLog {

	private static final Log log = LogFactory.getLog(NextflowWebLog.class);

	@Post("/api/v2/collect/{job}")
	public String post(String job, @Body Map<Object, Object> event) {

		try {

			NextflowInfo info = NextflowInfo.getInstance();
			JSONObject eventObject = new JSONObject();
			eventObject.putAll(event);
			if (eventObject.has("trace")) {
				info.addEvent(job, eventObject);
			} else {
				// System.out.println(event.toString());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";

	}

}
