package cloudgene.mapred.server.tasks;

import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.services.JobCleanUpService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobTasks {

	@Inject
	protected Application application;

	@Inject
	protected JobCleanUpService cleanUpService;

	@Scheduled(fixedDelay = "${micronaut.autoRetireInterval}")
	public void executeRetire() {

		if (application.getSettings().isAutoRetire()) {
			cleanUpService.executeRetire();
		}

	}

	@Scheduled(fixedDelay = "${micronaut.autoRetireInterval}")
	public void sendNotifications() {

		if (application.getSettings().isAutoRetire()) {
			cleanUpService.sendNotifications();
		}

	}

}
