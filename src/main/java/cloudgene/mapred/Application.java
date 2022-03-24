package cloudgene.mapred;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.security.Principal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.engine.Engine;
import org.restlet.ext.slf4j.Slf4jLoggerFacade;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.database.updates.BcryptHashUpdate;
import cloudgene.mapred.database.util.DatabaseConnectorFactory;
import cloudgene.mapred.database.util.Fixtures;
import cloudgene.mapred.jobs.PersistentWorkflowEngine;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.util.BuildUtil;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import genepi.db.Database;
import genepi.db.DatabaseConnector;
import genepi.db.DatabaseUpdater;
import genepi.io.FileUtil;
import io.micronaut.context.annotation.Context;
import io.micronaut.runtime.Micronaut;
import io.micronaut.security.authentication.Authentication;

@Context
public class Application {

	public static final String VERSION = "2.5.1";

	private Database database;

	private Settings settings;

	private WorkflowEngine engine;

	private Map<String, String> cacheTemplates;

	protected Log log = LogFactory.getLog(Application.class);

	public Application() throws Exception {

		// configure logger

		log.debug("Cloudgene " + VERSION);
		log.info(BuildUtil.getBuildInfos());

		// load cloudgene.conf file. contains path to settings, db, apps, ..
		Config config = new Config();
		if (new File(Config.CONFIG_FILENAME).exists()) {
			YamlReader reader = new YamlReader(new FileReader(Config.CONFIG_FILENAME));
			config = reader.read(Config.class);
		}

		settings = loadSettings(config);

		PluginManager pluginManager = PluginManager.getInstance();
		pluginManager.initPlugins(settings);

		database = new Database();

		String secretKey = settings.getSecretKey();
		if (secretKey == null || secretKey.isEmpty() || secretKey.equals(Settings.DEFAULT_SECURITY_KEY)) {
			secretKey = RandomStringUtils.randomAlphabetic(64);
			settings.setSecretKey(secretKey);
			settings.save();
		}

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

	protected Settings loadSettings(Config config) throws FileNotFoundException, YamlException {
		String settingsFilename = config.getSettings();

		// load default settings when not yet loaded

		if (new File(settingsFilename).exists()) {
			log.info("Loading settings from " + settingsFilename + "...");
			settings = Settings.load(config);
		} else {
			settings = new Settings(config);
		}

		if (!settings.testPaths()) {
			System.exit(1);
		}
		return settings;
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
		List<cloudgene.mapred.util.Template> templates = dao.findAll();

		cacheTemplates = new HashMap<String, String>();
		for (cloudgene.mapred.util.Template snippet : templates) {
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

	public User getUserByAuthentication(Authentication authentication) {

		User user = null;
		if (authentication != null) {
			UserDao userDao = new UserDao(database);
			user = userDao.findByUsername(authentication.getName());
			Map<String, Object> attributes = authentication.getAttributes();
			if (attributes.containsKey("token_type")) {
				if (attributes.get("token_type").equals("API")){
					if (user.getApiToken().equals(attributes.get("api_hash"))) {
						return user;
					} else {
						return null;
					}
				}
			}
			
		}

		return user;
	}
	
	protected void afterDatabaseConnection(Database database) {
		
	}

	public static void main(String[] args) {
		Micronaut.run(Application.class, args);
	}
}
