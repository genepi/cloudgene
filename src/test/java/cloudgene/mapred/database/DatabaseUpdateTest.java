package cloudgene.mapred.database;

import java.io.InputStream;

import org.junit.Test;

import cloudgene.mapred.Main;
import cloudgene.mapred.util.TestCluster;
import cloudgene.mapred.util.TestServer;
import genepi.db.Database;
import genepi.db.DatabaseUpdater;
import junit.framework.TestCase;

public class DatabaseUpdateTest extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();
	}

	
	@Test
	public void testVersionNoUpdateFromDB()   {

		Database database = TestServer.getInstance().getDatabase();
		InputStream is = Main.class.getResourceAsStream("/updates.sql");
		DatabaseUpdater databaseUpdater = new DatabaseUpdater(database, null, is,
				Main.VERSION);
	
		assertEquals(false, databaseUpdater.needUpdate());

	}

}
