package cloudgene.mapred.cron;

import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.Settings;
import genepi.db.Database;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RetireJob {

	@Inject
	protected Application application;

	@Scheduled(fixedDelay =  "${micronaut.autoRetireInterval}")

	public void execute() {

		if (!application.getSettings().isAutoRetire()) {
			return;
		}

		
		Database database = application.getDatabase();
		Settings settings = application.getSettings();

		CleanUpTasks.executeRetire(database, settings);

	}

}
