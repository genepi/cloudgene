package cloudgene.mapred.server;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.updates.BcryptHashUpdate;
import cloudgene.mapred.database.util.DatabaseConnectorFactory;
import cloudgene.mapred.database.util.Fixtures;
import cloudgene.mapred.jobs.PersistentWorkflowEngine;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import genepi.db.Database;
import genepi.db.DatabaseConnector;
import genepi.db.DatabaseUpdater;
import genepi.io.FileUtil;
import io.micronaut.context.annotation.Context;

@Context
public class Application {

	public static final String VERSION = "3.0.0-beta1";

	private Database database;

	public static Settings settings;
	
	public static Config config;

	private WorkflowEngine engine;

	private Map<String, String> cacheTemplates;

	protected Log log = LogFactory.getLog(Application.class);

	public Application() throws Exception {

		PluginManager pluginManager = PluginManager.getInstance();
		pluginManager.initPlugins(settings);

		database = new Database();

		// create h2 or mysql connector
		DatabaseConnector connector = DatabaseConnectorFactory.createConnector(settings.getDatabase());

		if (connector == null) {

			log.error("Unknown database driver");
			System.exit(1);

		}

		// connect do database
		try {

			database.connect(connector);

			log.info("Establish connection successful");

		} catch (SQLException e) {

			log.error("Establish connection failed", e);
			System.exit(1);

		}

		// update database schema if needed
		log.info("Setup Database...");
		InputStream is = Application.class.getResourceAsStream("/updates.sql");

		DatabaseUpdater updater = new DatabaseUpdater(database, config.getVersion(), is, VERSION);
		updater.addUpdate("2.3.0", new BcryptHashUpdate());

		if (!updater.updateDB()) {
			System.exit(-1);
		}

		reloadTemplates();

		// create directories
		FileUtil.createDirectory(settings.getTempPath());
		FileUtil.createDirectory(settings.getLocalWorkspace());

		// insert fixtures
		Fixtures.insert(database);

		afterDatabaseConnection(database);

		// start workflow engine
		try {

			engine = new PersistentWorkflowEngine(database, settings.getThreadsQueue(),
					settings.getThreadsSetupQueue());
			new Thread(engine).start();

		} catch (Exception e) {

			log.error("Can't launch the web server.\nAn unexpected " + "exception occured:", e);

			database.disconnect();

			System.exit(1);

		}

	}

	public WorkflowEngine getWorkflowEngine() {
		return engine;
	}

	public Settings getSettings() {
		return settings;
	}

	public Database getDatabase() {
		return database;
	}

	public void reloadTemplates() {
		TemplateDao dao = new TemplateDao(database);
		List<cloudgene.mapred.core.Template> templates = dao.findAll();

		cacheTemplates = new HashMap<String, String>();
		for (cloudgene.mapred.core.Template snippet : templates) {
			cacheTemplates.put(snippet.getKey(), snippet.getText());
		}
	}

	public String getTemplate(String key) {

		String template = cacheTemplates.get(key);

		if (template != null) {
			return template;
		} else {
			return "!" + key;
		}

	}

	public String getTemplate(String key, Object... strings) {

		String template = cacheTemplates.get(key);

		if (template != null) {
			return String.format(template, strings);
		} else {
			return "!" + key;
		}

	}

	protected void afterDatabaseConnection(Database database) {

	}

}
