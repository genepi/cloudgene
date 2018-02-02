package cloudgene.mapred.cli;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import cloudgene.mapred.util.Application;
import genepi.io.FileUtil;

public class InstallGitHubApplication extends BaseTool {

	public static class Repository {
		private String user;

		private String repo;

		private String tag;

		private String directory;

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getRepo() {
			return repo;
		}

		public void setRepo(String repo) {
			this.repo = repo;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public String getDirectory() {
			return directory;
		}

		public void setDirectory(String directory) {
			this.directory = directory;
		}

	}

	private String cmd = "cloudgene";

	public InstallGitHubApplication(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {

	}

	@Override
	public int run() {

		if (args.length != 2) {
			System.out.println("Usage: " + cmd + "gh <name> <GitHub repo> ");
			System.out.println();
			System.exit(1);
		}

		String id = args[0];
		String repo = args[1];

		// TODO: check repo structure and support tags, commits

		try {

			List<Application> applications = new Vector<Application>();

			if (settings.getApp(id) != null) {
				printlnInRed("[ERROR] An application with id '" + id + "' is already installed.\n");
				return 1;
			}

			System.out.println("Installing application " + id + "...");

			Repository repository = InstallGitHubApplication.parseShorthand(repo);
			if (repository == null) {
				printlnInRed("[ERROR] " + repo + " is not a valid GitHub repo.\n");
				return 1;
			}

			String url = InstallGitHubApplication.buildUrlFromRepository(repository);
			String zipFilename = FileUtil.path(settings.getTempPath(), "github.zip");
			FileUtils.copyURLToFile(new URL(url), new File(zipFilename));

			if (repository.getDirectory() != null) {
				//extract only sub dir
				applications = getSettings().installApplicationFromZipFile(id, zipFilename,
						"^.*/" + repository.getDirectory()+".*");
			} else {
				applications = getSettings().installApplicationFromZipFile(id, zipFilename);
			}

			if (applications.size() > 0) {
				settings.save();
				printlnInGreen("[OK] " + applications.size() + " Applications installed: \n");
				ListApplications.printApplicationList(applications);
				return 0;
			} else {
				printlnInRed("[ERROR] No valid Application found.\n");
				return 1;
			}

		} catch (Exception e) {
			e.printStackTrace();
			printlnInRed("[ERROR] Application not installed:" + e.toString() + "\n");
			return 1;

		}
	}

	public static Repository parseShorthand(String shorthand) {
		Repository repo = new Repository();
		// username/repo[/subdir][@ref]
		String[] tiles2 = shorthand.split("@");

		String[] tiles = tiles2[0].split("/", 3);
		if (tiles.length < 2) {
			return null;
		}
		repo.setUser(tiles[0]);
		repo.setRepo(tiles[1]);
		if (tiles.length > 2) {
			repo.setDirectory(tiles[2]);
		}

		if (tiles2.length == 2) {
			repo.setTag(tiles2[1]);
		} else if (tiles2.length > 2) {
			return null;
		}

		return repo;
	}

	public static String buildUrlFromRepository(Repository repo) {

		String url = "https://api.github.com/repos/" + repo.getUser() + "/" + repo.getRepo() + "/zipball";
		if (repo.getTag() != null) {
			url += "/" + repo.getTag();
		}
		return url;
	}
}