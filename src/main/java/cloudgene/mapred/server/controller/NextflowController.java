package cloudgene.mapred.server.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.plugins.nextflow.NextflowInfo;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.services.DownloadService;
import cloudgene.mapred.server.services.JobService;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import net.sf.json.JSONObject;


@Controller
public class NextflowController {

	protected static final Logger log = LoggerFactory.getLogger(DownloadController.class);

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected DownloadService downloadService;

	@Inject
	protected JobService jobService;
	
	@Post("/api/v2/collect/{job}")
	@Secured(SecurityRule.IS_ANONYMOUS)
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
