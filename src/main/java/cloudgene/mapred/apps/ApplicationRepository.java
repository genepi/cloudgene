package cloudgene.mapred.apps;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.util.DatabaseUpdater;
import cloudgene.mapred.util.GitHubException;
import cloudgene.mapred.util.GitHubUtil;
import cloudgene.mapred.util.GitHubUtil.Repository;
import cloudgene.mapred.wdl.WdlApp;
import genepi.hadoop.S3Util;
import genepi.io.FileUtil;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class ApplicationRepository {

	private List<Application> apps;

	private Map<String, Application> indexApps;

	private String appsFolder = "apps";

	private static final Log log = LogFactory.getLog(ApplicationRepository.class);

	public ApplicationRepository() {
		apps = new Vector<Application>();
		reload();
	}

	public void setAppsFolder(String appsFolder) {
		this.appsFolder = appsFolder;
	}

	public List<Application> getAll() {
		return apps;
	}

	public void setApps(List<Application> apps) {
		this.apps = apps;
		reload();
	}

	public void reload() {
		indexApps = new HashMap<String, Application>();
		log.info("Reload applications...");
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
		log.info("Loaded " + apps.size() + " applications.");
	}

	public Application getByIdAndUser(String id, User user) {

		Application app = getById(id);

		if (app != null && app.isEnabled() && app.isLoaded() && !app.hasSyntaxError()) {

			if (user == null) {
				if (app.hasPermission(("public"))) {
					if (app.getWdlApp().getWorkflow() != null) {
						return app;
					} else {
						return app;
					}
				} else {
					return null;
				}
			}

			if (user.isAdmin() || user.hasRole(app.getPermissions())
					|| app.hasPermission("public")) {
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

	public Application getById(String id) {

		Application app = indexApps.get(id);

		if (app == null) {
			// try without version

			List<Application> versions = new Vector<Application>();

			for (String idd : indexApps.keySet()) {
				String tiles[] = idd.split("@");
				if (tiles.length == 2) {
					if (id.equals(tiles[0])) {
						versions.add(indexApps.get(idd));
					}
				}
			}

			if (!versions.isEmpty()) {
				// find latest

				Application latest = versions.get(0);

				for (int i = 1; i < versions.size(); i++) {

					String latestVersion = latest.getWdlApp().getVersion();
					String version = versions.get(i).getWdlApp().getVersion();

					if (DatabaseUpdater.compareVersion(version, latestVersion) == 1) {
						latest = versions.get(i);
					}

				}

				return latest;

			} else {
				return null;
			}

		}

		return app;

	}

	public List<WdlApp> getAllByUser(User user) {
		return getAllByUser(user, true);
	}

	public List<WdlApp> getAllByUser(User user, boolean appsOnly) {

		List<WdlApp> listApps = new Vector<WdlApp>();

		for (Application application : getAll()) {

			boolean using = true;

			if (user == null) {
				if (application.hasPermission("public")) {
					using = true;
				} else {
					using = false;
				}
			} else {

				if (!user.isAdmin() && !application.hasPermission("public")) {

					if (!user.hasRole(application.getPermissions())) {
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

	public void remove(Application application) throws IOException {
		log.info("Remove application " + application.getId());
		// delete application in app folder
		// TODO: add some check to avoid deleting whole hdd. e.g. delete only if its in
		// app folders...
		// FileUtil.deleteDirectory(application.getWdlApp().getPath());
		// remove from app list
		apps.remove(application);
		reload();

	}

	public Application install(String url) throws IOException, GitHubException {

		Application application = null;

		if (url.startsWith("http://") || url.startsWith("https://")) {
			application = installFromUrl(url);
		} else if (url.startsWith("s3://")) {
			application = installFromS3(url);
		} else if (url.startsWith("github://")) {

			String repo = url.replace("github://", "");

			Repository repository = GitHubUtil.parseShorthand(repo);
			if (repository == null) {
				throw new GitHubException(repo + " is not a valid GitHub repo.");
			}

			application = installFromGitHub(repository);

		} else {

			if (new File(url).exists()) {

				if (url.endsWith(".zip")) {
					application = installFromZipFile(url);
				} else if (url.endsWith(".yaml")) {
					application = installFromYaml(url, false);
				} else if (url.endsWith(".yml")) {
					application = installFromYaml(url, false);
				} else {
					application = installFromDirectory(url, false);
				}

			} else {
				String repo = url.replace("github://", "");

				Repository repository = GitHubUtil.parseShorthand(repo);
				if (repository == null) {
					throw new GitHubException(repo + " is not a valid GitHub repo.");
				}

				application = installFromGitHub(repository);

			}
		}

		return application;

	}

	public void removeById(String id) throws IOException {
		for (Application app : new Vector<Application>(getAll())) {
			if (app.getId().startsWith(id)) {
				remove(app);
			}
		}
	}

	public Application installFromUrl(String url) throws IOException {
		// download file from url
		if (url.endsWith(".zip")) {
			File zipFile = new File(FileUtil.path(appsFolder, "archive.zip"));
			FileUtils.copyURLToFile(new URL(url), zipFile);
			Application application = installFromZipFile(zipFile.getAbsolutePath());
			zipFile.delete();
			return application;
		}
		return null;

	}

	public Application installFromS3(String url) throws IOException {
		// download file from s3 bucket
		if (url.endsWith(".zip")) {
			File zipFile = new File(FileUtil.path(appsFolder, "archive.zip"));
			S3Util.copyToFile(url, zipFile);
			Application application = installFromZipFile(zipFile.getAbsolutePath());
			zipFile.delete();
			return application;
		} else {

			String appPath = FileUtil.path(appsFolder, "s3-download");
			FileUtil.deleteDirectory(appPath);
			FileUtil.createDirectory(appPath);

			String baseKey = S3Util.getKey(url);

			ObjectListing listing = S3Util.listObjects(url);

			for (S3ObjectSummary summary : listing.getObjectSummaries()) {

				String bucket = summary.getBucketName();
				String key = summary.getKey();

				if (summary.getKey().endsWith("/")) {
					System.out.println("Found folder" + bucket + "/" + key);
					String relativeKey = summary.getKey().replaceAll(baseKey, "");
					String target = FileUtil.path(appPath, relativeKey);
					FileUtil.createDirectory(target);
				}

			}

			for (S3ObjectSummary summary : listing.getObjectSummaries()) {

				String bucket = summary.getBucketName();
				String key = summary.getKey();

				if (!summary.getKey().endsWith("/")) {
					System.out.println("Found file" + bucket + "/" + key);
					String relativeKey = summary.getKey().replaceAll(baseKey, "");
					String target = FileUtil.path(appPath, relativeKey);
					File file = new File(target);
					// create parent folder
					File parent = file.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					System.out.println("Copy file from " + bucket + "/" + key + " to " + target);
					S3Util.copyToFile(bucket, key, file);
				}
			}

			try {
				Application application = installFromDirectory(appPath, true);
				return application;
			} finally {
				FileUtil.deleteDirectory(appPath);
			}

		}
	}

	public Application installFromGitHub(Repository repository) throws MalformedURLException, IOException {

		String url = GitHubUtil.buildUrlFromRepository(repository);
		File zipFile = new File(FileUtil.path(appsFolder, "archive.zip"));
		FileUtils.copyURLToFile(new URL(url), zipFile);

		String zipFilename = zipFile.getAbsolutePath();
		if (repository.getDirectory() != null) {
			// extract only sub dir
			Application application = installFromZipFile(zipFilename, "^.*/" + repository.getDirectory() + ".*");
			zipFile.delete();
			return application;

		} else {
			Application application = installFromZipFile(zipFilename);
			zipFile.delete();
			return application;
		}

	}

	public Application installFromZipFile(String zipFilename) throws IOException {

		// extract in apps folder

		String appPath = FileUtil.path(appsFolder, "archive");
		FileUtil.deleteDirectory(appPath);
		FileUtil.createDirectory(appPath);
		try {
			ZipFile file = new ZipFile(zipFilename);
			file.extractAll(appPath);
		} catch (ZipException e) {
			throw new IOException(e);
		}

		try {
			Application application = installFromDirectory(appPath, true);
			return application;
		} finally {
			FileUtil.deleteDirectory(appPath);
		}

	}

	public Application installFromZipFile(String zipFilename, String subFolder) throws IOException {

		String appPath = FileUtil.path(appsFolder, "archive");
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

		try {
			Application application = installFromDirectory(appPath, true);
			return application;
		} finally {
			FileUtil.deleteDirectory(appPath);
		}

	}

	public Application installFromDirectory(String path, boolean moveToApps) throws IOException {

		String cloudgeneFilename = FileUtil.path(path, "cloudgene.yaml");
		if (new File(cloudgeneFilename).exists()) {
			Application application = installFromYaml(cloudgeneFilename, moveToApps);
			if (application != null) {
				return application;
			}
		}

		cloudgeneFilename = FileUtil.path(path, "cloudgene.yml");
		if (new File(cloudgeneFilename).exists()) {
			Application application = installFromYaml(cloudgeneFilename, moveToApps);
			if (application != null) {
				return application;
			}
		}

		// find all cloudgene workflows (use filename as id)
		String[] files = FileUtil.getFiles(path, "*.yaml");

		for (String filename : files) {
			Application application = installFromYaml(filename, moveToApps);
			if (application != null) {
				return application;
			}
		}

		// search in subfolders
		for (String directory : getDirectories(path)) {
			Application application = installFromDirectory(directory, moveToApps);
			if (application != null) {
				return application;
			}
		}
		return null;

	}

	public Application installFromYaml(String filename, boolean moveToApps) throws IOException {

		Application application = new Application();
		application.setFilename(filename);
		application.setPermission("user");
		try {
			application.loadWdlApp();
		} catch (IOException e) {
			log.warn("Ignore file " + filename + ". Not a valid cloudgene file.", e);
			return null;
		}

		String id = application.getWdlApp().getId() + "@" + application.getWdlApp().getVersion();

		// application with same version is already installed.
		if (indexApps.get(id) != null) {
			throw new IOException("Application " + id + " is already installed");
		}

		// check if its an update an remove old version
		Application installedApplication = null;
		for (String installedId : indexApps.keySet()) {
			String name = installedId.split(":")[0];
			if (name.equals(application.getWdlApp().getId())) {
				installedApplication = indexApps.remove(installedId);
			}
		}

		if (installedApplication != null) {
			log.info("Update application from " + installedApplication.getId() + " to " + id);
			remove(installedApplication);
		}

		if (moveToApps) {

			File file = new File(filename);
			File folder = file.getParentFile();

			String targetPath = FileUtil.path(appsFolder, application.getWdlApp().getId(),
					application.getWdlApp().getVersion());

			FileUtil.createDirectory(targetPath);
			File target = new File(targetPath);

			// copy to apps and update filename
			FileUtils.copyDirectory(folder, target);
			application.setFilename(FileUtil.path(targetPath, file.getName()));

			// delete older directory
			FileUtil.deleteDirectory(folder);

			try {
				application.loadWdlApp();
			} catch (IOException e) {
				log.warn("Ignore file " + filename + ". Not a valid cloudgene file.", e);
				return null;
			}

		}

		application.setId(id);
		apps.add(application);

		WdlApp wdlApp = application.getWdlApp();
		// TODO: check if it is needed!
		if (wdlApp != null) {
			wdlApp.setId(id);
		}

		indexApps.put(application.getId(), application);

		return application;

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

	public String getConfigDirectory(String id) {
		Application app = getById(id);
		return getConfigDirectory(app.getWdlApp());
	}

	public String getConfigDirectory(WdlApp app) {
		return FileUtil.path(appsFolder, app.getId().split("@")[0]);
	}

	public Map<String, String> getConfig(WdlApp app) {

		String appFolder = getConfigDirectory(app);

		Map<String, String> config = new HashMap<String, String>();

		String nextflowConfig = FileUtil.path(appFolder, "nextflow.config");
		if (new File(nextflowConfig).exists()) {
			String content = FileUtil.readFileAsString(nextflowConfig);
			config.put("nextflow.config", content);
		}

		String nextflowProfile = FileUtil.path(appFolder, "nextflow.profile");
		if (new File(nextflowProfile).exists()) {
			String content = FileUtil.readFileAsString(nextflowProfile);
			config.put("nextflow.profile", content);
		}

		String nextflowWork = FileUtil.path(appFolder, "nextflow.work");
		if (new File(nextflowWork).exists()) {
			String content = FileUtil.readFileAsString(nextflowWork);
			config.put("nextflow.work", content);
		}

		return config;

	}

	public void updateConfig(WdlApp app, Map<String, String> config) {

		String appFolder = getConfigDirectory(app);
		FileUtil.createDirectory(appFolder);

		String nextflowConfig = FileUtil.path(appFolder, "nextflow.config");
		String content = config.get("nextflow.config");
		StringBuffer contentNextflowConfig = new StringBuffer(content == null ? "" : content);
		FileUtil.writeStringBufferToFile(nextflowConfig, contentNextflowConfig);

		String nextflowProfile = FileUtil.path(appFolder, "nextflow.profile");
		content = config.get("nextflow.profile");
		StringBuffer contentNextflowProfile = new StringBuffer(content == null ? "" : content);
		FileUtil.writeStringBufferToFile(nextflowProfile, contentNextflowProfile);

		String nextflowWork = FileUtil.path(appFolder, "nextflow.work");
		content = config.get("nextflow.work");
		StringBuffer contentNextflowWork = new StringBuffer(content == null ? "" : content);
		FileUtil.writeStringBufferToFile(nextflowWork, contentNextflowWork);

	}

}
