package cloudgene.mapred.cron;

import cloudgene.mapred.server.Application;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class NotificationJob {

	@Inject
	protected Application application;
	
	@Scheduled(fixedDelay =  "${micronaut.autoRetireInterval}")
	public void execute()  {

		if (!application.getSettings().isAutoRetire()) {
			return;
		}
		
		CleanUpTasks.sendNotifications(application);

	}


}
