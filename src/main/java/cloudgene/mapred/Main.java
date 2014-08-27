package cloudgene.mapred;

import genepi.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.LocalReference;

import cloudgene.mapred.core.User;
import cloudgene.mapred.cron.CronJobScheduler;
import cloudgene.mapred.database.DatabaseUpdater;
import cloudgene.mapred.database.H2Connector;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Settings;

public class Main {

	public static String VERSION = "1.0.7";

	private static final Log log = LogFactory.getLog(Main.class);

	public static void main(String[] args) throws IOException {

		System.out.println("Cloudgene MapRed 1.0\n");

		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options options = new Options();
		Option portOption = new Option(null, "port", true,
				"runs cloudgene on port <PORT>");
		portOption.setRequired(false);
		portOption.setArgName("PORT");
		options.addOption(portOption);

		Option usernameOption = new Option(null, "add-user", true,
				"creates a new user");
		usernameOption.setRequired(false);
		usernameOption.setArgs(2);
		usernameOption.setArgName("USERNAME> <PASSWORD");
		options.addOption(usernameOption);

		Option bucketOption = new Option(null, "bucket", true,
				"exports automatically all result files to a S3 bucket");
		bucketOption.setRequired(false);
		bucketOption.setArgName("BUCKET");
		options.addOption(bucketOption);

		Option md5Option = new Option(null, "md5", false,
				"the password is provided as md5-hash");
		md5Option.setRequired(false);
		options.addOption(md5Option);

		Option adminOption = new Option(null, "admin", false,
				"the user has admin rights");
		adminOption.setRequired(false);
		options.addOption(adminOption);

		// parse the command line arguments
		CommandLine line = null;
		try {

			line = parser.parse(options, args);

		} catch (Exception e) {

			System.out.println(e.getMessage() + "\n");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("hadoop jar cloudgene-mapred.jar", options);

			System.exit(1);

		}

		H2Connector connector = H2Connector.getInstance();
		try {

			connector.connect();

			log.info("Establish connection successful");

		} catch (SQLException e) {

			log.error("Establish connection failed", e);
			System.exit(1);

		}

		// change??
		if (connector.isNewDatabase()) {
			File versionFile = new File("version.txt");
			if (versionFile.exists()) {
				versionFile.delete();
			}
		}

		DatabaseUpdater askimedUpdater = new DatabaseUpdater(connector,
				"version.txt", "/updates.sql", VERSION, false);
		if (askimedUpdater.needUpdate()) {
			log.info("Askimed Database needs update");
			if (!askimedUpdater.update()) {
				log.error("Updating Askimed database failed.");
				return;
			}
		} else {
			log.info("Database is uptodate");
		}

		// CacheDao dao3 = new CacheDao();
		// dao3.clear();

		if (line.hasOption("add-user")) {

			String username = line.getOptionValues("add-user")[0];
			String password = line.getOptionValues("add-user")[1];
			String bucket = line.getOptionValue("bucket", null);

			// insert user
			UserDao dao = new UserDao();
			User user = dao.findByUsername(username);
			if (user == null) {
				user = new User();
				user.setUsername(username);
				if (!line.hasOption("md5")) {
					password = HashUtil.getMD5(password);
				}
				user.setPassword(password);

				if (line.hasOption("admin")) {
					user.setRole("Admin");
				}

				if (bucket != null) {
					user.setExportToS3(true);
					user.setS3Bucket(bucket);
				}
				dao.insert(user);
				log.info("User " + username + " created.");
				System.out.println("User " + username
						+ " created successfully.");
			} else {
				log.info("User " + username + " already exists.");
				System.out.println("User " + username + " already exists.");
			}

			try {
				H2Connector.getInstance().disconnect();
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.exit(0);
		}

		try {

			// init config

			if (!new File("config/settings.yaml").exists()) {
				H2Connector.getInstance().disconnect();
				log.error("Config file not found (config/settings.yaml).");
				System.exit(1);
			}

			Settings settings = Settings.getInstance();
			settings.load("config/settings.yaml");
			// reload!
			settings = Settings.getInstance();
			if (!settings.testPaths()) {

				H2Connector.getInstance().disconnect();

				System.exit(1);
			}

			// create directories
			FileUtil.createDirectory(settings.getTempPath());

			new Thread(WorkflowEngine.getInstance()).start();

			log.info("Start CronJobScheduler...");
			CronJobScheduler scheduler = new CronJobScheduler();
			scheduler.start(settings.isAutoRetire(),
					settings.isWriteStatistics());

			int port = Integer.parseInt(line.getOptionValue("port", "8082"));

			LocalReference webroot = new LocalReference("clap://thread/webapp");
			LocalReference webroot2 = new LocalReference("clap://thread/webapp");

			new WebServer(webroot, webroot2, port).start();

		} catch (Exception e) {

			log.error("Can't launch the web server.\nAn unexpected "
					+ "exception occured:", e);

			try {

				H2Connector.getInstance().disconnect();

			} catch (SQLException e1) {

				log.error("An unexpected " + "exception occured:", e);

			}

			System.exit(1);

		}
	}
}
