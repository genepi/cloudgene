package cloudgene.mapred.util;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

public class Settings {

	private String hadoopPath = "";

	private String pigPath = "/home/hadoop/pig-0.10.0/";

	private String rPath = "/usr/";

	private String appsPath = "../cloudgene.tools";

	private String app = null;

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

	public static int RETIRED_AFTER_SECS = 7 * 24 * 60 * 60;

	public boolean isRemoveHdfsWorkspace() {
		return removeHdfsWorkspace;
	}

	public void setRemoveHdfsWorkspace(boolean removeHdfsWorkspace) {
		this.removeHdfsWorkspace = removeHdfsWorkspace;
	}

	private static Settings instance = null;

	private static final Log log = LogFactory.getLog(Settings.class);

	private boolean streaming = true;

	private boolean removeHdfsWorkspace = false;

	private Settings() {

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

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
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

	public boolean testPaths() {

		if (!new File(appsPath).exists()) {

			if (!new File(app).exists()) {

				log.error("appsPath '" + app + "' does not exist.");

				return false;
			} else {

				log.info("Using application " + app);

			}
		}

		String hadoop = FileUtil.path(hadoopPath, "bin", "hadoop");

		if (!new File(hadoop).exists()) {

			log.error("hadoop '" + hadoop + "' does not exist.");

			return false;

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
}
