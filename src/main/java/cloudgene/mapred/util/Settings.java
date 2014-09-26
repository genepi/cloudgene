package cloudgene.mapred.util;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

public class Settings {

	private String hadoopPath = "/usr/";

	private String pigPath = "/home/hadoop/pig-0.10.0/";

	private String rPath = "/usr/";

	private String appsPath = "../cloudgene.tools";

	private String outputPath = "output";

	private String tempPath = "tmp";

	private String localWorkspace = "workspace";

	private String hdfsWorkspace = "cloudgene";

	private String streamingJar = "";

	private String cloudUser;

	private String mapRed;

	private String cloudFolder;

	private String version;

	private String name = "Cloudgene";

	private Map<String, String> mail;

	private Map<String, String> apps;

	private int retireAfter = 6;

	private int notificationAfter = 4;

	private boolean autoRetire = false;

	private boolean streaming = true;

	private boolean removeHdfsWorkspace = false;

	private static Settings instance = null;

	private static final Log log = LogFactory.getLog(Settings.class);

	private boolean writeStatistics = true;

	private boolean https = false;

	private String httpsKeystore = "";

	private String httpsPassword = "";

	private boolean maintenance = false;

	private String adminMail = "lukas.forer@i-med.ac.at";

	private Map<String, String> cacheTemplates;

	private Settings() {
		apps = new HashMap<String, String>();
		apps.put("user", "sample/cloudgene.yaml");
		apps.put("admin", "sample/cloudgene.yaml");

		mail = new HashMap<String, String>();
		mail.put("smtp", "localhost");
		mail.put("port", "25");
		mail.put("user", "");
		mail.put("password", "");
		mail.put("name", "noreply@cloudgene");

	}

	public void load(String filename) throws FileNotFoundException,
			YamlException {

		YamlReader reader = new YamlReader(new FileReader(filename));

		instance = reader.read(Settings.class);

		// auto-search

		if (instance.streamingJar.isEmpty()
				|| !(new File(instance.streamingJar).exists())) {

			String version = HadoopUtil.getInstance().getVersion();
			String jar = "hadoop-streaming-" + version + ".jar";
			instance.streamingJar = FileUtil.path(instance.hadoopPath,
					"contrib", "streaming", jar);

			if (new File(instance.streamingJar).exists()) {

				log.info("Found streamingJar at " + instance.streamingJar + "");
				instance.streaming = true;

			} else {

				log.warn("Streaming Jar could not be found automatically. Please specify it in config/settings.yaml. Streaming mode is disabled.");
				instance.streaming = false;
			}

		}

		log.info("Auto retire: " + instance.isAutoRetire());
		log.info("Retire jobs after " + instance.retireAfter + " days.");
		log.info("Notify user after " + instance.notificationAfter + " days.");
		log.info("Write statistics: " + instance.writeStatistics);

	}


	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
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

	public String getAppsPath() {
		return appsPath;
	}

	public void setAppsPath(String appsPath) {
		this.appsPath = appsPath;
	}

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

	public String getLocalWorkspace(String username) {
		return HdfsUtil.path(localWorkspace, username);
	}

	public void setLocalWorkspace(String localWorkspace) {
		this.localWorkspace = localWorkspace;
	}

	public String getHdfsWorkspace() {
		return hdfsWorkspace;
	}

	public String getHdfsWorkspace(String username) {
		return HdfsUtil.path(hdfsWorkspace, username);
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

			for (String app : apps.values()) {

				if (!new File(app).exists()) {

					log.error("file '" + app + "' does not exist.");

					return false;
				} else {

					log.info("using application " + app);

				}

			}

		}

		String hadoop = FileUtil.path(hadoopPath, "bin", "hadoop");

		if (!new File(hadoop).exists()) {

			log.warn("hadoop '" + hadoop
					+ "' does not exist. please change it.");

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

	public String getCloudUser() {
		return cloudUser;
	}

	public void setCloudUser(String cloudUser) {
		this.cloudUser = cloudUser;
	}

	public String getMapRed() {
		return mapRed;
	}

	public void setMapRed(String mapRed) {
		this.mapRed = mapRed;
	}

	public String getCloudFolder() {
		return cloudFolder;
	}

	public void setCloudFolder(String cloudFolder) {
		this.cloudFolder = cloudFolder;
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

	public Map<String, String> getApps() {
		return apps;
	}

	public void setApps(Map<String, String> apps) {
		this.apps = apps;
	}

	public String getApp(User user) {

		return apps.get(user.getRole().toLowerCase());

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTempFilename(String filename) {
		String path = Settings.getInstance().getTempPath();
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

	public void reloadTemplates() {
		TemplateDao dao = new TemplateDao();
		List<Template> templates = dao.findAll();

		cacheTemplates = new HashMap<String, String>();
		for (Template snippet : templates) {
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

	public void setAdminMail(String adminMail) {
		this.adminMail = adminMail;
	}

	public String getAdminMail() {
		return adminMail;
	}

}
