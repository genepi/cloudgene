package cloudgene.mapred.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import cloudgene.mapred.core.User;
import cloudgene.mapred.wdl.WdlApp;
import genepi.io.FileUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Settings {

	private String hadoopPath = "/usr/bin/hadoop";

	private String pigPath = "/usr/";

	private String sparkPath = "/usr/bin/spark-submit";

	private String tempPath = "tmp";

	private String localWorkspace = "workspace";

	private String hdfsWorkspace = "cloudgene/data";

	private String hdfsAppWorkspace = "cloudgene/apps";

	private String streamingJar = "";

	private String version;

	private String name = "Cloudgene";

	private String secretKey = "default-key-change-me";

	private Map<String, String> mail;

	private Map<String, String> database;

	private Map<String, String> cluster;

	private List<Application> apps;

	private int autoRetireInterval = 5;

	private int retireAfter = 6;

	private int notificationAfter = 4;

	private int threadsSetupQueue = 5;

	private int threadsQueue = 5;

	private int maxRunningJobsPerUser = 2;

	private boolean autoRetire = false;

	private boolean streaming = true;

	private boolean removeHdfsWorkspace = true;

	private static final Log log = LogFactory.getLog(Settings.class);

	private boolean writeStatistics = true;

	private boolean https = false;

	private String httpsKeystore = "";

	private String httpsPassword = "";

	private boolean maintenance = false;

	private String adminMail = null;

	private String slack = null;

	private Map<String, Application> indexApps;

	private String urlPrefix = "";

	private List<MenuItem> navigation = new Vector<MenuItem>();

	private boolean secureCookie = false;

	private int uploadLimit = -1;

	private Set<Technology> technologies = new HashSet<Technology>();

	public Settings() {

		apps = new Vector<Application>();
		reloadApplications();

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

		// enable all technologies
		for (Technology technology : Technology.values()) {
			enable(technology);
		}

	}

	public static Settings load(String filename) throws FileNotFoundException, YamlException {

		YamlConfig config = new YamlConfig();
		config.setPropertyElementType(Settings.class, "apps", Application.class);
		YamlReader reader = new YamlReader(new FileReader(filename), config);

		Settings settings = reader.read(Settings.class);
		settings.enable(Technology.HADOOP_CLUSTER);

		log.info("Auto retire: " + settings.isAutoRetire());
		log.info("Retire jobs after " + settings.retireAfter + " days.");
		log.info("Notify user after " + settings.notificationAfter + " days.");
		log.info("Write statistics: " + settings.writeStatistics);

		if (settings.cluster != null) {
			String host = settings.cluster.get("host");
			String username = settings.cluster.get("username");
			log.info("Use external Haddop cluster running on " + host + " with username " + username);
			HadoopCluster.init(host, username);
		}

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

	public String getHdfsAppWorkspace() {
		return hdfsAppWorkspace;
	}

	public void setHdfsAppWorkspace(String hdfsAppWorkspace) {
		this.hdfsAppWorkspace = hdfsAppWorkspace;
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

	public Map<String, String> getCluster() {
		return cluster;
	}

	public void setCluster(Map<String, String> cluster) {
		this.cluster = cluster;
	}

	public String getSlack() {
		return slack;
	}

	public void setSlack(String slack) {
		this.slack = slack;
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
					if (app.getWdlApp().getWorkflow() != null) {
						return app;
					} else {
						return app;
					}
				} else {
					return null;
				}
			}

			if (user.isAdmin() || user.hasRole(app.getPermission())
					|| app.getPermission().toLowerCase().equals("public")) {
				if (app.getWdlApp().getWorkflow() != null) {
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

	public List<WdlApp> getAppsByUser(User user) {
		return getAppsByUser(user, true);
	}

	public List<WdlApp> getAppsByUser(User user, boolean appsOnly) {

		List<WdlApp> listApps = new Vector<WdlApp>();

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

					if (!user.hasRole(application.getPermission())) {
						using = false;
					}
				}
			}

			if (using) {

				if (application.isEnabled() && application.isLoaded() && !application.hasSyntaxError()) {
					if (appsOnly) {
						if (application.getWdlApp().getWorkflow() != null) {
							WdlApp app = application.getWdlApp();
							app.setId(application.getId());
							listApps.add(app);
						}
					} else {
						WdlApp app = application.getWdlApp();
						app.setId(application.getId());
						listApps.add(app);
					}
				}

			}

		}

		return listApps;

	}

	public void deleteApplication(Application application) throws IOException {

		// download
		String id = application.getId();
		// String appPath = FileUtil.path("apps", id);
		// FileUtil.deleteDirectory(appPath);
		apps.remove(application);
		reloadApplications();

	}

	public List<Application> installApplicationFromUrl(String id, String url) throws IOException {
		// download file from url
		if (url.endsWith(".zip")) {
			String zipFilename = FileUtil.path(getTempPath(), "download.zip");
			FileUtils.copyURLToFile(new URL(url), new File(zipFilename));
			return installApplicationFromZipFile(id, zipFilename);
		} else {
			try {
				String appPath = FileUtil.path("apps", id);
				FileUtil.createDirectory("apps");
				FileUtil.createDirectory(appPath);

				String yamlFilename = FileUtil.path(appPath, "cloudgene.yaml");

				FileUtils.copyURLToFile(new URL(url), new File(yamlFilename));
				Application application = installApplicationFromYaml(id, yamlFilename);

				List<Application> installed = new Vector<Application>();
				if (application != null) {
					installed.add(application);
				}

				return installed;

			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}

	}

	public List<Application> installApplicationFromZipFile(String id, String zipFilename) throws IOException {

		// extract in apps folder
		String appPath = FileUtil.path("apps", id);

		FileUtil.createDirectory("apps");
		FileUtil.createDirectory(appPath);
		ZipFile file;
		try {
			file = new ZipFile(zipFilename);
			file.extractAll(appPath);
		} catch (ZipException e) {
			throw new IOException(e);
		}

		return installApplicationFromDirectory(id, appPath);

	}

	public List<Application> installApplicationFromDirectory(String id, String path) throws IOException {
		// find all cloudgene workflows (use filename as id)
		System.out.println("Search in folder " + path);
		String[] files = FileUtil.getFiles(path, "*.yaml");

		List<Application> installed = new Vector<Application>();

		for (String filename : files) {
			String newId = id;
			if (files.length > 0) {
				newId = id + "-" + FileUtil.getFilename(filename).replaceAll(".yaml", "");
			}
			Application application = installApplicationFromYaml(newId, filename);
			if (application != null) {
				installed.add(application);
			}
		}

		// search in subfolders
		for (String directory : getDirectories(path)) {
			installed.addAll(installApplicationFromDirectory(id, directory));
		}
		return installed;

	}

	public Application installApplicationFromYaml(String id, String filename) throws IOException {

		if (indexApps.get(id) != null) {
			throw new IOException("Application " + id + " is already installed");
		}

		Application application = new Application();
		application.setId(id);
		application.setFilename(filename);
		application.setPermission("user");
		application.loadWorkflow();

		apps.add(application);
		indexApps.put(application.getId(), application);

		return application;

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

	public int getUploadLimit() {
		return uploadLimit;
	}

	public void setUploadLimit(int uploadLimit) {
		this.uploadLimit = uploadLimit;
	}

	private String[] getDirectories(String path) {
		File dir = new File(path);
		File[] files = dir.listFiles();

		int count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				count++;
			}
		}

		String[] names = new String[count];

		count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				names[count] = file.getAbsolutePath();
				count++;
			}
		}

		return names;
	}

	public void enable(Technology technology) {
		technologies.add(technology);
	}

	public void disable(Technology technology) {
		technologies.remove(technology);
	}

	public boolean isEnable(Technology technology) {
		return technologies.contains(technology);
	}

	public void setThreadsSetupQueue(int threadsSetupQueue) {
		this.threadsSetupQueue = threadsSetupQueue;
	}

	public int getThreadsSetupQueue() {
		return threadsSetupQueue;
	}

	public HashMap<String, String> getEnvironment(WdlApp application) {
		HashMap<String, String> environment = new HashMap<String, String>();
		String hdfsFolder = FileUtil.path(hdfsAppWorkspace, application.getId(), application.getVersion());
		String localFolder = application.getPath();
		environment.put("hdfs_app_folder", hdfsFolder);
		environment.put("local_app_folder", localFolder);
		return environment;
	}

	public int getAutoRetireInterval() {
		return autoRetireInterval;
	}

	public void setAutoRetireInterval(int autoRetireInterval) {
		this.autoRetireInterval = autoRetireInterval;
	}

}