package cloudgene.mapred.server.controller;

import cloudgene.mapred.plugins.nextflow.NextflowInfo;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.services.DownloadService;
import cloudgene.mapred.server.services.JobService;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class NextflowController {

	protected static final Log log = LogFactory.getLog(DownloadController.class);

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected DownloadService downloadService;

	@Inject
	protected JobService jobService;
	
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
