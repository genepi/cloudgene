package cloudgene.mapred.server.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.CounterHistoryDao;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.auth.AuthenticationService;
import cloudgene.mapred.server.responses.NextflowConfigResponse;
import cloudgene.mapred.server.responses.ServerResponse;
import cloudgene.mapred.server.responses.StatisticsResponse;
import cloudgene.mapred.server.services.ServerService;
import genepi.io.FileUtil;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.oauth2.configuration.OauthClientConfigurationProperties;
import jakarta.inject.Inject;

@Controller("/api/v2/admin/server")
@Secured(User.ROLE_ADMIN)

public class ServerAdminController {

	private static final String LOG_FILENAME = "logs/cloudgene.log";

	public static String CLOUDGENE_APPS_ENDPOINT = "http://apps.cloudgene.io/api/apps.json";

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Inject
	protected List<OauthClientConfigurationProperties> clients;

	@Inject
	protected ServerService serverService;

	@Get("/queue/block")
	@Produces(MediaType.TEXT_PLAIN)
	public String blockQueue() {
		application.getWorkflowEngine().block();
		return "Queue blocked.";
	}

	@Get("/queue/open")
	@Produces(MediaType.TEXT_PLAIN)
	public String openQueue() {
		application.getWorkflowEngine().resume();
		return "Queue opened.";
	}

	@Get("/maintenance/enter")
	@Produces(MediaType.TEXT_PLAIN)
	public String enterMaintenance() {
		application.getSettings().setMaintenance(true);
		application.getSettings().save();
		return "Enter Maintenance mode.";
	}

	@Get("/maintenance/exit")
	@Produces(MediaType.TEXT_PLAIN)
	public String exitMaintenance() {
		application.getSettings().setMaintenance(false);
		application.getSettings().save();
		return "Exit Maintenance mode.";
	}

	@Get("/cluster")
	public String getDetails() {

		return serverService.getClusterDetails();

	}

	@Get("/logs/cloudgene.log")
	public String getLogs() {
		File file = new File(LOG_FILENAME);
		if (file.exists()) {
			String content = serverService.tail(file, 1000);
			return content;
		} else {
			return "No log file available.";
		}

	}

	@Get("/settings")
	public ServerResponse getSettings() {

		return ServerResponse.build(application.getSettings());

	}

	@Post("/settings/update")
	public ServerResponse updateSettings(String name, String adminName, String adminMail, String serverUrl,
			String backgroundColor, String foregroundColor, String googleAnalytics, boolean mail, String mailSmtp,
			String mailUser, String mailPassword, String mailPort, String mailName) {

		serverService.updateSettings(name, adminName, adminMail, serverUrl, backgroundColor, foregroundColor,
				googleAnalytics, String.valueOf(mail), mailSmtp, mailPort, mailUser, mailPassword, mailName);

		return ServerResponse.build(application.getSettings());

	}

	@Get("/nextflow/config")
	public NextflowConfigResponse getNextflowConfig() {

		return NextflowConfigResponse.build(application.getSettings());

	}

	@Post("/nextflow/config/update")
	public NextflowConfigResponse updateNextflowConfig(String content) {

		serverService.updateNextflowConfig(content);

		return NextflowConfigResponse.build(application.getSettings());

	}

	@Get("/cloudgene-apps")
	public String list() throws IOException {
		URL url = new URL(CLOUDGENE_APPS_ENDPOINT);
		String content = FileUtil.readFileAsString(url.openStream());
		return content;
	}

	public String[] counters = new String[] { "runningJobs", "waitingJobs", "completeJobs", "users" };

	@Get("/statistics")
	public List<Map<String, String>> getStatistics(@Nullable @QueryValue("days") Integer days) {

		if (days == null) {
			days = 1;
		}

		CounterHistoryDao dao = new CounterHistoryDao(application.getDatabase());

		List<Map<String, String>> stats = dao.getAllBeetween(
				System.currentTimeMillis() - (1000L * 60L * 60L * 24L * days), System.currentTimeMillis());

		return StatisticsResponse.build(stats);

	}

}
