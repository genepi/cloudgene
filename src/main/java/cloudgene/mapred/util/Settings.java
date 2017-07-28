package cloudgene.mapred.util;

import genepi.hadoop.HadoopUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

public class Settings {

	private String hadoopPath = "/usr/bin/hadoop";

	private String pigPath = "/usr/";

	private String sparkPath = "/usr/bin/spark-submit";

	private String rPath = "/usr/";

	private String appsPath = "../cloudgene.tools";

	private String outputPath = "output";

	private String tempPath = "tmp";

	private String localWorkspace = "workspace";

	private String hdfsWorkspace = "cloudgene";

	private String streamingJar = "";

	private String version;

	private String name = "Cloudgene";

	private String secretKey = "default-key-change-me";

	private Map<String, String> mail;

	private Map<String, String> database;

	private List<Application> apps;

	private int retireAfter = 6;

	private int notificationAfter = 4;

	private int threadsQueue = 5;

	private int maxRunningJobs = 20;

	private int maxRunningJobsPerUser = 2;

	private boolean autoRetire = false;

	private boolean streaming = true;

	private boolean removeHdfsWorkspace = false;

	private static final Log log = LogFactory.getLog(Settings.class);

	private boolean writeStatistics = true;

	private boolean https = false;

	private String httpsKeystore = "";

	private String httpsPassword = "";

	private boolean maintenance = false;

	private String adminMail = "lukas.forer@i-med.ac.at";

	private Map<String, Application> indexApps;

	private String urlPrefix = "";

	private List<MenuItem> navigation = new Vector<MenuItem>();

	public Settings() {

		apps = new Vector<Application>();
		apps.add(new Application("hello", "admin", "sample/cloudgene.yaml"));
		apps.add(new Application("hello", "public", "sample/cloudgene-public.yaml"));

		indexApps = new HashMap<String, Application>();
		for (Application app : apps) {
			indexApps.put(app.getId(), app);
		}

		mail = new HashMap<String, String>();
		mail.put("smtp", "localhost");
		mail.put("port", "25");
		mail.put("user", "");
		mail.put("password", "");
		mail.put("name", "noreply@cloudgene");

		MenuItem helpMenuItem = new MenuItem();
		helpMenuItem.setId("help");
		helpMenuItem.setName("Help");
		helpMenuItem.setLink("#!pages/help");
		navigation.add(helpMenuItem);

		database = new HashMap<String, String>();
		database.put("driver", "h2");
		database.put("database", "data/mapred");
		database.put("user", "mapred");
		database.put("password", "mapred");

	}

	public static Settings load(String filename) throws FileNotFoundException, YamlException {

		YamlConfig config = new YamlConfig();
		config.setPropertyElementType(Settings.class, "apps", Application.class);

		YamlReader reader = new YamlReader(new FileReader(filename), config);

		Settings settings = reader.read(Settings.class);

		// auto-search

		if (settings.streamingJar.isEmpty() || !(new File(settings.streamingJar).exists())) {

			String version = HadoopUtil.getInstance().getVersion();
			String jar = "hadoop-streaming-" + version + ".jar";
			settings.streamingJar = FileUtil.path(settings.hadoopPath, "contrib", "streaming", jar);

			if (new File(settings.streamingJar).exists()) {

				log.info("Found streamingJar at " + settings.streamingJar + "");
				settings.streaming = true;

			} else {

				log.warn(
						"Streaming Jar could not be found automatically. Please specify it in config/settings.yaml. Streaming mode is disabled.");
				settings.streaming = false;
			}

		}

		log.info("Auto retire: " + settings.isAutoRetire());
		log.info("Retire jobs after " + settings.retireAfter + " days.");
		log.info("Notify user after " + settings.notificationAfter + " days.");
		log.info("Write statistics: " + settings.writeStatistics);

		return settings;

	}

	public String getHadoopPath() {
		return hadoopPath;
	}

	public void setHadoopPath(String hadoopPath) {
		this.hadoopPath = hadoopPath;
	}

	public void setPigPath(String pigPath) {
		this.pigPath = pigPath;
	}

	public String getPigPath() {
		return pigPath;
	}

	/*
	 * public String getAppsPath() { return appsPath; }
	 * 
	 * public void setAppsPath(String appsPath) { this.appsPath = appsPath; }
	 */

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getTempPath() {
		return tempPath;
	}

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	public String getLocalWorkspace() {
		return localWorkspace;
	}

	public void setLocalWorkspace(String localWorkspace) {
		this.localWorkspace = localWorkspace;
	}

	public String getHdfsWorkspace() {
		return hdfsWorkspace;
	}

	public void setHdfsWorkspace(String hdfsWorkspace) {
		this.hdfsWorkspace = hdfsWorkspace;
	}

	public String getStreamingJar() {
		return streamingJar;
	}

	public void setStreamingJar(String streamingJar) {
		this.streamingJar = streamingJar;
	}

	public boolean isStreaming() {
		return streaming;
	}

	public void setStreaming(boolean streaming) {
		this.streaming = streaming;
	}

	public String getRPath() {
		return rPath;
	}

	public void setRPath(String rPath) {
		this.rPath = rPath;
	}

	public boolean isRemoveHdfsWorkspace() {
		return removeHdfsWorkspace;
	}

	public void setRemoveHdfsWorkspace(boolean removeHdfsWorkspace) {
		this.removeHdfsWorkspace = removeHdfsWorkspace;
	}

	public boolean testPaths() {

		if (!new File(appsPath).exists()) {

			for (Application app : apps) {

				if (!new File(app.getFilename()).exists()) {

					log.error("file '" + app.getFilename() + "' does not exist.");

					return false;
				} else {

					log.info("using application " + app.getId());

				}

			}

		}

		String hadoop = FileUtil.path(hadoopPath, "bin", "hadoop");

		if (!new File(hadoop).exists()) {

			log.warn("hadoop '" + hadoop + "' does not exist. please change it.");

			// return false;

		}
		/*
		 * if (!new File(streamingJar).exists()) {
		 * 
		 * log.error("streamingJar '" + streamingJar + "' does not exist.");
		 * 
		 * return false;
		 * 
		 * }
		 */

		return true;

	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, String> getMail() {
		return mail;
	}

	public void setMail(Map<String, String> mail) {
		this.mail = mail;
	}

	public List<Application> getApps() {
		return apps;
	}

	public void setApps(List<Application> apps) {
		this.apps = apps;
		indexApps = new HashMap<String, Application>();
		for (Application app : apps) {
			log.info("Register application " + app.getId());
			indexApps.put(app.getId(), app);
		}
	}

	public String getApp(User user, String id) {

		Application app = indexApps.get(id);

		if (app != null) {

			if (user == null) {
				if (app.getPermission().toLowerCase().equals("public")) {
					System.out.println("App: " + app.getFilename());
					return app.getFilename();
				} else {
					return null;
				}
			}

			if (user.isAdmin() || app.getPermission().toLowerCase().equals(user.getRole().toLowerCase())
					|| app.getPermission().toLowerCase().equals("public")) {

				return app.getFilename();
			} else {
				return null;
			}

		} else {

			if (user != null) {

				String filename = FileUtil.path("apps", id.replaceAll("~", "/") + ".yaml");

				return filename;

			} else {
				return null;
			}

		}

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTempFilename(String filename) {
		String path = getTempPath();
		String name = FileUtil.getFilename(filename);
		return FileUtil.path(path, name);
	}

	public void setNotificationAfter(int notificationAfter) {
		this.notificationAfter = notificationAfter;
	}

	public int getNotificationAfter() {
		return notificationAfter;
	}

	public int getNotificationAfterInSec() {
		return notificationAfter * 24 * 60 * 60;
	}

	public void setRetireAfter(int retireAfter) {
		this.retireAfter = retireAfter;
	}

	public int getRetireAfter() {
		return retireAfter;
	}

	public int getRetireAfterInSec() {
		return retireAfter * 24 * 60 * 60;
	}

	public void setAutoRetire(boolean autoRetire) {
		this.autoRetire = autoRetire;
	}

	public boolean isAutoRetire() {
		return autoRetire;
	}

	public void setWriteStatistics(boolean writeStatistics) {
		this.writeStatistics = writeStatistics;
	}

	public boolean isWriteStatistics() {
		return writeStatistics;
	}

	public void setHttps(boolean https) {
		this.https = https;
	}

	public boolean isHttps() {
		return https;
	}

	public void setHttpsKeystore(String httpsKeystore) {
		this.httpsKeystore = httpsKeystore;
	}

	public String getHttpsKeystore() {
		return httpsKeystore;
	}

	public void setHttpsPassword(String httpsPassword) {
		this.httpsPassword = httpsPassword;
	}

	public String getHttpsPassword() {
		return httpsPassword;
	}

	public void setMaintenance(boolean maintenance) {
		this.maintenance = maintenance;
	}

	public boolean isMaintenance() {
		return maintenance;
	}

	public void setAdminMail(String adminMail) {
		this.adminMail = adminMail;
	}

	public String getAdminMail() {
		return adminMail;
	}
	
	public String getSparkPath() {
		return sparkPath;
	}

	public void setSparkPath(String sparkPath) {
		this.sparkPath = sparkPath;
	}

	public void setThreadsQueue(int threadsQueue) {
		this.threadsQueue = threadsQueue;
	}

	public int getThreadsQueue() {
		return threadsQueue;
	}

	public int getMaxRunningJobs() {
		return maxRunningJobs;
	}

	public void setMaxRunningJobs(int maxRunningJobs) {
		this.maxRunningJobs = maxRunningJobs;
	}

	public int getMaxRunningJobsPerUser() {
		return maxRunningJobsPerUser;
	}

	public void setMaxRunningJobsPerUser(int maxRunningJobsPerUser) {
		this.maxRunningJobsPerUser = maxRunningJobsPerUser;
	}

	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}

	public String getUrlPrefix() {
		return urlPrefix;
	}

	public void setDatabase(Map<String, String> database) {
		this.database = database;
	}

	public Map<String, String> getDatabase() {
		return database;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void setNavigation(List<MenuItem> navigation) {
		this.navigation = navigation;
	}

	public List<MenuItem> getNavigation() {
		return navigation;
	}

}
