package cloudgene.mapred.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.ClusterStatus;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.GitHubUtil.Repository;
import cloudgene.mapred.wdl.WdlApp;
import genepi.hadoop.HadoopUtil;
import genepi.io.FileUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

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

	private Map<String, String> colors;

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

	private String googleAnalytics = "";

	protected Config config;

	public Settings() {

		apps = new Vector<Application>();
		reloadApplications();

		// read default settings from env variables when set
		String name = System.getenv().get("CLOUDGENE_SERVICE_NAME");
		if (name != null) {
			this.name = name;
		}

		MenuItem helpMenuItem = new MenuItem();
		helpMenuItem.setId("help");
		helpMenuItem.setName("Help");
		String helpLink = System.getenv().get("CLOUDGENE_HELP_PAGE");
		if (helpLink != null) {
			helpMenuItem.setLink(helpLink);
		} else {
			helpMenuItem.setLink("http://docs.cloudgene.io");
		}
		navigation.add(helpMenuItem);

		database = new HashMap<String, String>();
		initDefaultDatabase(database, "data/mapred");

		colors = getDefaultColors();

	}

	public Settings(Config config) {

		this();
		this.config = config;

		// workspace in config has higher priority
		if (config.getWorkspace() != null) {
			setLocalWorkspace(config.getWorkspace());
		}

		// database in config has higher priority
		if (config.getDatabase() != null) {
			Map<String, String> database = getDatabase();
			if (database != null) {
				if (database.get("driver") != null && database.get("driver").equals("h2")) {
					database.put("database", config.getDatabase());
					// check file and create parent folders
					File databaseFile = new File(config.getDatabase());
					if (!databaseFile.exists()) {
						databaseFile.getParentFile().mkdirs();
					}
				}
			} else {
				database = new HashMap<String, String>();
				initDefaultDatabase(database, config.getDatabase());
			}
		}

	}

	public static Settings load(Config config) throws FileNotFoundException, YamlException {

		YamlConfig yamlConfig = new YamlConfig();
		yamlConfig.setPropertyElementType(Settings.class, "apps", Application.class);
		YamlReader reader = new YamlReader(new FileReader(config.getSettings()), yamlConfig);

		Settings settings = reader.read(Settings.class);
		settings.enable(Technology.HADOOP_CLUSTER);

		log.info("Auto retire: " + settings.isAutoRetire());
		log.info("Retire jobs after " + settings.retireAfter + " days.");
		log.info("Notify user after " + settings.notificationAfter + " days.");
		log.info("Write statistics: " + settings.writeStatistics);

		if (settings.cluster != null) {
			String conf = settings.cluster.get("conf");
			String username = settings.cluster.get("user");
			String name = settings.cluster.get("name");
			if (conf != null) {
				log.info("Use Haddop configuration folder '" + conf + "'"
						+ (username != null ? " with username " + username : ""));
				HadoopCluster.setConfPath(name, conf, username);
			}
		}

		settings.config = config;

		// workspace in config has higher priority
		if (config.getWorkspace() != null) {
			settings.setLocalWorkspace(config.getWorkspace());
		}

		// database in config has higher priority
		if (config.getDatabase() != null) {
			Map<String, String> database = settings.getDatabase();
			if (database != null) {
				if (database.get("driver") != null && database.get("driver").equals("h2")) {
					database.put("database", config.getDatabase());
					// check file and create parent folders
					File databaseFile = new File(config.getDatabase());
					if (!databaseFile.exists()) {
						databaseFile.getParentFile().mkdirs();
					}
				}
			} else {
				database = new HashMap<String, String>();
				initDefaultDatabase(database, config.getDatabase());
			}
		}

		return settings;

	}

	public static void initDefaultDatabase(Map<String, String> database, String defaultSchema) {
		database.put("driver", "h2");
		database.put("database", defaultSchema);
		database.put("user", "mapred");
		database.put("password", "mapred");
	}

	public static Map<String, String> getDefaultColors() {
		Map<String, String> colors = new HashMap<String, String>();
		colors.put("background", "#343a40");
		colors.put("foreground", "navbar-dark");
		return colors;
	}

	public void save() {
		try {

			File file = new File(config.getSettings());
			if (!file.exists()) {
				file.getParentFile().mkdirs();
			}

			YamlWriter writer = new YamlWriter(new FileWriter(config.getSettings()));
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
				app.loadWdlApp();
				WdlApp wdlApp = app.getWdlApp();
				// update wdl id with id from application
				if (wdlApp != null) {
					wdlApp.setId(app.getId());
				}
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
							listApps.add(app);
						}
					} else {
						WdlApp app = application.getWdlApp();
						listApps.add(app);
					}
				}

			}

		}

		Collections.sort(listApps);
		return listApps;

	}

	public void deleteApplication(Application application) throws IOException {

		// delete application in app folder
		String id = application.getId();
		// String appPath = FileUtil.path(config.getApps(), id);
		// FileUtil.deleteDirectory(appPath);

		// remove from app list
		apps.remove(application);
		reloadApplications();

	}

	public void deleteApplicationById(String id) throws IOException {
		for (Application app : new Vector<Application>(getApps())) {
			if (app.getId().startsWith(id)) {
				deleteApplication(app);
			}
		}
	}

	public List<Application> installApplicationFromUrl(String id, String url) throws IOException {
		// download file from url
		if (url.endsWith(".zip")) {
			String zipFilename = FileUtil.path(getTempPath(), "download.zip");
			FileUtils.copyURLToFile(new URL(url), new File(zipFilename));
			return installApplicationFromZipFile(id, zipFilename);
		} else {
			try {
				String appPath = FileUtil.path(config.getApps(), id);
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

		String appPath = FileUtil.path(config.getApps(), id);
		FileUtil.deleteDirectory(appPath);
		FileUtil.createDirectory(appPath);
		try {
			ZipFile file = new ZipFile(zipFilename);
			file.extractAll(appPath);
		} catch (ZipException e) {
			throw new IOException(e);
		}

		return installApplicationFromDirectory(id, appPath);

	}

	public List<Application> installApplicationFromGitHub(String id, Repository repository, boolean update)
			throws MalformedURLException, IOException {

		List<Application> applications = new Vector<Application>();

		if (getApp(id) != null) {
			if (update) {
				System.out.println("Updating application " + id + "...");
				deleteApplicationById(id);
			} else {
				return applications;
			}
		} else {
			System.out.println("Installing application " + id + "...");
		}

		String url = GitHubUtil.buildUrlFromRepository(repository);
		String zipFilename = FileUtil.path(getTempPath(), "github.zip");
		FileUtils.copyURLToFile(new URL(url), new File(zipFilename));

		if (repository.getDirectory() != null) {
			// extract only sub dir
			applications = installApplicationFromZipFile(id, zipFilename, "^.*/" + repository.getDirectory() + ".*");
		} else {
			applications = installApplicationFromZipFile(id, zipFilename);
		}

		return applications;

	}

	public List<Application> installApplicationFromZipFile(String id, String zipFilename, String subFolder)
			throws IOException {

		String appPath = FileUtil.path(config.getApps(), id);
		FileUtil.deleteDirectory(appPath);
		FileUtil.createDirectory(appPath);
		try {
			ZipFile file = new ZipFile(zipFilename);
			for (Object header : file.getFileHeaders()) {
				FileHeader fileHeader = (FileHeader) header;
				String name = fileHeader.getFileName();
				if (name.matches(subFolder)) {
					file.extractFile(fileHeader, appPath);
				}
			}
		} catch (ZipException e) {
			throw new IOException(e);
		}

		return installApplicationFromDirectory(id, appPath);

	}

	public List<Application> installApplicationFromDirectory(String id, String path) throws IOException {
		return installApplicationFromDirectory(id, path, false);
	}

	public List<Application> installApplicationFromDirectory(String id, String path, boolean multiple)
			throws IOException {
		// find all cloudgene workflows (use filename as id)
		String[] files = FileUtil.getFiles(path, "*.yaml");

		List<Application> installed = new Vector<Application>();

		for (String filename : files) {
			String newId = id;
			if (multiple) {
				newId = id + "-" + FileUtil.getFilename(filename).replaceAll(".yaml", "");
			}
			Application application = installApplicationFromYaml(newId, filename);
			if (application != null) {
				installed.add(application);
				multiple = true;
			}
		}

		// search in subfolders
		for (String directory : getDirectories(path)) {
			List<Application> installedSubFolder = installApplicationFromDirectory(id, directory, multiple);
			if (installedSubFolder.size() > 0) {
				multiple = true;
				installed.addAll(installedSubFolder);
			}
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
		try {
			application.loadWdlApp();
		} catch (IOException e) {
			System.out.println("Ignore file " + filename + ". Not a valid cloudgene.yaml file.");
			return null;
		}
		System.out.println("Process file " + filename + "....");
		apps.add(application);
		WdlApp wdlApp = application.getWdlApp();
		if (wdlApp != null) {
			wdlApp.setId(id);
		}
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
		log.info("Enable technology " + technology);
		technologies.add(technology);
	}

	public void disable(Technology technology) {
		log.info("Disable technology " + technology);
		technologies.remove(technology);
	}

	public boolean isEnable(Technology technology) {
		return technologies.contains(technology);
	}

	public void checkTechnologies() {
		// check cluster status
		ClusterStatus details = HadoopUtil.getInstance().getClusterDetails();
		if (details != null) {
			int nodes = details.getActiveTrackerNames().size();
			if (nodes == 0) {
				disable(Technology.HADOOP_CLUSTER);
			} else {
				enable(Technology.HADOOP_CLUSTER);
			}
		} else {
			disable(Technology.HADOOP_CLUSTER);
		}

		if (!RBinary.isInstalled()) {
			disable(Technology.R);
			disable(Technology.R_MARKDOWN);
		} else {
			enable(Technology.R);
			if (!RBinary.isMarkdownInstalled()) {
				disable(Technology.R_MARKDOWN);
			} else {
				enable(Technology.R_MARKDOWN);
			}
		}

		try {
			DockerClient docker = DefaultDockerClient.fromEnv().build();
			docker.info();
			docker.close();
			enable(Technology.DOCKER);
		} catch (DockerException | DockerCertificateException | InterruptedException e1) {
			disable(Technology.DOCKER);
		}
	}

	public void setThreadsSetupQueue(int threadsSetupQueue) {
		this.threadsSetupQueue = threadsSetupQueue;
	}

	public int getThreadsSetupQueue() {
		return threadsSetupQueue;
	}

	public int getAutoRetireInterval() {
		return autoRetireInterval;
	}

	public void setAutoRetireInterval(int autoRetireInterval) {
		this.autoRetireInterval = autoRetireInterval;
	}

	public Map<String, String> getColors() {
		return colors;
	}

	public void setColors(Map<String, String> colors) {
		this.colors = colors;
	}

	public void setGoogleAnalytics(String googleAnalytics) {
		this.googleAnalytics = googleAnalytics;
	}

	public String getGoogleAnalytics() {
		return googleAnalytics;
	}

}