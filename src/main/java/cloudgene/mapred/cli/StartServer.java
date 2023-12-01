package cloudgene.mapred.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import ch.qos.logback.classic.util.ContextInitializer;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import genepi.base.Tool;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.Micronaut;

public class StartServer extends Tool {

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	public static final String SECURITY_FILENAME = "config/security.yaml";

	private String[] args;

	public StartServer(String[] args) {
		super(args);
		this.args = args;
	}

	@Override
	public void createParameters() {
		addOptionalParameter("port", "running webinterface on this port [default: 8082]", Tool.STRING);
	}

	@Override
	public int run() {

		if (new File("webapp").exists()) {
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback.xml");
		} else {
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "logback-dev.xml");
		}


		try {

			// load cloudgene.conf file. contains path to settings, db, apps, ..
			Application.config = new Config();
			if (new File(Config.CONFIG_FILENAME).exists()) {
				YamlReader reader = new YamlReader(new FileReader(Config.CONFIG_FILENAME));
				Application.config = reader.read(Config.class);
			}

			// load setting.yaml. contains applications, server configuration, ...
			Application.settings = loadSettings(Application.config);

			String port = Application.settings.getPort();
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("micronaut.server.port", port);
			if (Application.settings.getUploadLimit() != -1) {
				properties.put("micronaut.server.maxRequestSize", Application.settings.getUploadLimit() + "MB");
				properties.put("micronaut.server.multipart.maxFileSize", Application.settings.getUploadLimit() + "MB");
			}

			String secretKey = Application.settings.getSecretKey();
			if (secretKey == null || secretKey.isEmpty() || secretKey.equals(Settings.DEFAULT_SECURITY_KEY)) {
				secretKey = RandomStringUtils.randomAlphabetic(64);
				Application.settings.setSecretKey(secretKey);
				Application.settings.save();
			}

			properties.put("micronaut.security.token.jwt.signatures.secret.generator.secret",
					Application.settings.getSecretKey());
			properties.put("micronaut.autoRetireInterval", Application.settings.getAutoRetireInterval() + "h");

			if (new File(SECURITY_FILENAME).exists()) {
				System.out.println("Use config file " + SECURITY_FILENAME);
				System.setProperty("micronaut.config.files", SECURITY_FILENAME);
			} else {

			}

			String baseUrl = Application.settings.getBaseUrl();
			if (!baseUrl.trim().isEmpty()) {
				if (!baseUrl.startsWith("/") || baseUrl.endsWith("/")){
					System.out.println("Error: baseUrl has wrong format. Example: \"/path\" or \"/path/subpath\".");
					System.exit(1);
				}
				properties.put("micronaut.server.context-path", baseUrl);
			} else {

			}

			if (new File("webapp").exists()) {

				Micronaut.build(args).mainClass(Application.class).properties(properties).start();

			} else {

				System.out.println("Start in DEVELOPMENT mode");

				Micronaut.build(args).mainClass(Application.class).properties(properties)
						.defaultEnvironments(Environment.DEVELOPMENT).start();

			}

			System.out.println();
			System.out.println("Server is running");
			System.out.println();
			System.out.println("Please press ctrl-c to stop.");
			while (true) {
				Thread.sleep(5000000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

	@Override
	public void init() {

	}

	protected Settings loadSettings(Config config) throws FileNotFoundException, YamlException {
		String settingsFilename = config.getSettings();

		// load default settings when not yet loaded
		Settings settings;
		if (new File(settingsFilename).exists()) {
			System.out.println("Loading settings from " + settingsFilename + "...");
			settings = Settings.load(config);
		} else {
			settings = new Settings(config);
		}

		if (settings.getServerUrl() == null || settings.getServerUrl().trim().isEmpty()) {
			System.out.println("Error: serverUrl not set. Please set serverUrl in file '" + settingsFilename + "'");
			System.exit(1);
		}

		return settings;
	}

}