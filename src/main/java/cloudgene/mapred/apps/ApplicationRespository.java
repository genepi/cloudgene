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

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.GitHubUtil;
import cloudgene.mapred.util.GitHubUtil.Repository;
import cloudgene.mapred.wdl.WdlApp;
import genepi.io.FileUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class ApplicationRespository {

	private List<Application> apps;

	private Map<String, Application> indexApps;

	private String appsFolder = "apps";

	private static ApplicationRespository instance;

	private static final Log log = LogFactory.getLog(ApplicationRespository.class);

	public static ApplicationRespository getInstance() {
		if (instance == null) {
			instance = new ApplicationRespository("apps");
		}
		return instance;
	}

	private ApplicationRespository(String appsFolder) {
		this.appsFolder = appsFolder;
		apps = new Vector<Application>();
		reload();
	}

	public List<Application> getAll() {
		return apps;
	}

	public void init(List<Application> apps) {
		this.apps = apps;
		reload();
	}

	public void reload() {
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

	public Application getByIdAndUser(String id, User user) {

		Application app = getById(id);

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

	public Application getById(String id) {

		Application app = indexApps.get(id);
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

	public void remove(Application application) throws IOException {

		// delete application in app folder
		String id = application.getId();
		// String appPath = FileUtil.path(config.getApps(), id);
		// FileUtil.deleteDirectory(appPath);

		// remove from app list
		apps.remove(application);
		reload();

	}

	public void removeById(String id) throws IOException {
		for (Application app : new Vector<Application>(getAll())) {
			if (app.getId().startsWith(id)) {
				remove(app);
			}
		}
	}

	public List<Application> installFromUrl(String id, String url) throws IOException {
		// download file from url
		if (url.endsWith(".zip")) {
			File zipFile = File.createTempFile("download", ".zip");
			FileUtils.copyURLToFile(new URL(url), zipFile);
			return installFromZipFile(id, zipFile.getAbsolutePath());
		} else {
			try {
				String appPath = FileUtil.path(appsFolder, id);
				FileUtil.createDirectory(appPath);

				String yamlFilename = FileUtil.path(appPath, "cloudgene.yaml");

				FileUtils.copyURLToFile(new URL(url), new File(yamlFilename));
				Application application = installFromYaml(id, yamlFilename);

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

	public List<Application> installFromZipFile(String id, String zipFilename) throws IOException {

		// extract in apps folder

		String appPath = FileUtil.path(appsFolder, id);
		FileUtil.deleteDirectory(appPath);
		FileUtil.createDirectory(appPath);
		try {
			ZipFile file = new ZipFile(zipFilename);
			file.extractAll(appPath);
		} catch (ZipException e) {
			throw new IOException(e);
		}

		return installFromDirectory(id, appPath);

	}

	public List<Application> installFromGitHub(String id, Repository repository, boolean update)
			throws MalformedURLException, IOException {

		List<Application> applications = new Vector<Application>();

		if (getById(id) != null) {
			if (update) {
				System.out.println("Updating application " + id + "...");
				removeById(id);
			} else {
				return applications;
			}
		} else {
			System.out.println("Installing application " + id + "...");
		}

		String url = GitHubUtil.buildUrlFromRepository(repository);
		File zipFile = File.createTempFile("github", ".zip");
		FileUtils.copyURLToFile(new URL(url), zipFile);

		String zipFilename = zipFile.getAbsolutePath();
		if (repository.getDirectory() != null) {
			// extract only sub dir
			applications = installFromZipFile(id, zipFilename, "^.*/" + repository.getDirectory() + ".*");
		} else {
			applications = installFromZipFile(id, zipFilename);
		}

		return applications;

	}

	public List<Application> installFromZipFile(String id, String zipFilename, String subFolder)
			throws IOException {

		String appPath = FileUtil.path(appsFolder, id);
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

		return installFromDirectory(id, appPath);

	}

	public List<Application> installFromDirectory(String id, String path) throws IOException {
		return installFromDirectory(id, path, false);
	}

	public List<Application> installFromDirectory(String id, String path, boolean multiple)
			throws IOException {
		// find all cloudgene workflows (use filename as id)
		String[] files = FileUtil.getFiles(path, "*.yaml");

		List<Application> installed = new Vector<Application>();

		for (String filename : files) {
			String newId = id;
			if (multiple) {
				newId = id + "-" + FileUtil.getFilename(filename).replaceAll(".yaml", "");
			}
			Application application = installFromYaml(newId, filename);
			if (application != null) {
				installed.add(application);
				multiple = true;
			}
		}

		// search in subfolders
		for (String directory : getDirectories(path)) {
			List<Application> installedSubFolder = installFromDirectory(id, directory, multiple);
			if (installedSubFolder.size() > 0) {
				multiple = true;
				installed.addAll(installedSubFolder);
			}
		}
		return installed;

	}

	public Application installFromYaml(String id, String filename) throws IOException {

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
			log.warn("Ignore file " + filename + ". Not a valid cloudgene.yaml file.", e);
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
}
