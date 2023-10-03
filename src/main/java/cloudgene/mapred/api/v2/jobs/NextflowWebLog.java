package cloudgene.mapred.api.v2.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import cloudgene.mapred.plugins.nextflow.NextflowInfo;
import cloudgene.mapred.util.BaseResource;
import net.sf.json.JSONObject;

public class NextflowWebLog extends BaseResource {

	private static final Log log = LogFactory.getLog(NextflowWebLog.class);

	@Post
	public Representation post(Representation entity) {

		String job = getAttribute("job");

		try {

			NextflowInfo info = NextflowInfo.getInstance();
			JSONObject event = JSONObject.fromObject(entity.getText());
			if (event.has("trace")) {
				info.addEvent(job, event);
			} else {
				//System.out.println(event.toString());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ok("");

	}

}
