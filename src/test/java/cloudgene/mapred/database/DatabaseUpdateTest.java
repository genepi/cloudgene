package cloudgene.mapred.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import cloudgene.mapred.Application;
import cloudgene.mapred.TestApplication;
import genepi.db.Database;
import genepi.db.DatabaseUpdater;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class DatabaseUpdateTest {
	
	@Inject
	TestApplication application;	
	
	@Test
	public void testVersionNoUpdateFromDB()   {

		Database database = application.getDatabase();
		InputStream is = Application.class.getResourceAsStream("/updates.sql");
		DatabaseUpdater databaseUpdater = new DatabaseUpdater(database, null, is,
				Application.VERSION);
	
		assertEquals(false, databaseUpdater.needUpdate());

	}

}
