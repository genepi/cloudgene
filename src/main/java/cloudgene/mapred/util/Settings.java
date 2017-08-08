package cloudgene.mapred.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import cloudgene.mapred.core.User;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlHeader;
import genepi.hadoop.HadoopUtil;
import genepi.io.FileUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Settings {

	private String hadoopPath = "/usr/bin/hadoop";

	private String pigPath = "/usr/";

	private String sparkPath = "/usr/bin/spark-submit";

	private String rPath = "/usr/";

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

	private boolean secureCookie = false;

	public Settings() {

		apps = new Vector<Application>();
		apps.add(new Application("hello", "admin", "sample/cloudgene.yaml"));
		apps.add(new Application("hello", "public", "sample/cloudgene-public.yaml"));

		reloadApplications();

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

	public void save() {
		try {

			FileUtil.createDirectory("config");
			YamlWriter writer = new YamlWriter(new FileWriter(FileUtil.path("config", "settings.yaml")));
			writer.write(this);
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

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
		reloadApplications();
	}

	public void reloadApplications() {
		indexApps = new HashMap<String, Application>();
		for (Application app : apps) {
			log.info("Register application " + app.getId());

			// load application
			try {
				log.info("Load workflow file " + app.getFilename());
				app.loadWorkflow();
			} catch (IOException e) {
				log.error("Application " + app.getId() + " has syntax errors.", e);
			}
			indexApps.put(app.getId(), app);

		}
	}

	public Application getAppByIdAndUser(String id, User user) {

		Application app = getApp(id);

		if (app != null && app.isEnabled() && app.isLoaded() && !app.hasSyntaxError()) {

			if (user == null) {
				if (app.getPermission().toLowerCase().equals("public")) {
					if (app.getWorkflow().getMapred() != null) {
						return app;
					} else {
						return app;
					}
				} else {
					return null;
				}
			}

			if (user.isAdmin() || app.getPermission().toLowerCase().equals(user.getRole().toLowerCase())
					|| app.getPermission().toLowerCase().equals("public")) {
				if (app.getWorkflow().getMapred() != null) {
					return app;
				} else {
					return app;
				}

			} else {
				return null;
			}

		}

		return null;

	}

	public Application getApp(String id) {

		Application app = indexApps.get(id);
		return app;

	}
	
	public List<WdlHeader> getAppsByUser(User user) {

		List<WdlHeader> listApps = new Vector<WdlHeader>();

		for (Application application : getApps()) {

			boolean using = true;

			if (user == null) {
				if (application.getPermission().toLowerCase().equals("public")) {
					using = true;
				} else {
					using = false;
				}
			} else {

				if (!user.isAdmin() && !application.getPermission().toLowerCase().equals("public")) {

					if (!application.getPermission().toLowerCase().equals(user.getRole().toLowerCase())) {
						using = false;
					}
				}
			}

			if (using) {

				if (application.isEnabled() && application.isLoaded() && !application.hasSyntaxError()) {
					if (application.getWorkflow().getMapred() != null) {
						WdlApp app = application.getWorkflow();
						WdlHeader meta = (WdlHeader) app;
						app.setId(application.getId());
						listApps.add(meta);
					}
				}

			}

		}

		return listApps;

	}

	public void deleteApplication(Application application) throws IOException {

		// execute install steps
		if (application.getWorkflow().getDeinstallation() != null) {
			application.getWorkflow().deinstall();
		}
		
		// download
		String appPath = FileUtil.path("apps", "my_app");
		FileUtil.deleteDirectory(appPath);
		apps.remove(application);
		reloadApplications();

	}

	public Application installApplicationFromUrl(String url) throws IOException {

		// download
		ClientResource pull = new ClientResource(url);

		Representation response = null;
		try {
			response = pull.get(MediaType.APPLICATION_ZIP);
		} catch (ResourceException e) {
			throw new IOException(e);
		}

		if (pull.getStatus().getCode() == 200) {
			String zipFilename = FileUtil.path(getTempPath(), "download.zip");
			response.write(new FileOutputStream(zipFilename));

			return installApplicationFromZipFile(zipFilename);

		} else {
			throw new IOException("Zip file couldn't be downloaded.");
		}

	}

	public Application installApplicationFromZipFile(String zipFilename) throws IOException {

		// extract in apps folder

		String appPath = FileUtil.path("apps", "my_app");

		FileUtil.createDirectory("apps");
		FileUtil.createDirectory(appPath);
		ZipFile file;
		try {
			file = new ZipFile(zipFilename);
			file.extractAll(appPath);
		} catch (ZipException e) {
			throw new IOException(e);
		}

		return installApplicationFromDirectory(appPath);

	}

	public Application installApplicationFromDirectory(String path) throws IOException {

		// find all cloudgene workflows (use filename as id)

		String[] files = FileUtil.getFiles(path, "*.yaml");
		for (String filename : files) {

			Application application = new Application();
			application.setId(FileUtil.getFilename(filename).replaceAll(".yaml", ""));
			application.setFilename(filename);
			application.setPermission("user");
			application.loadWorkflow();

			// check requirements

			// execute install steps
			if (application.getWorkflow().getInstallation() != null) {
				application.getWorkflow().install();
			}

			apps.add(application);
			indexApps.put(application.getId(), application);
			
			return application;
		}
		
		return null;

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

	public boolean isSecureCookie() {
		return secureCookie;
	}

	public void setSecureCookie(boolean secureCookie) {
		this.secureCookie = secureCookie;
	}

}