package cloudgene.mapred.cli;

import java.io.File;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.util.GitHubUtil;
import cloudgene.mapred.util.GitHubUtil.Repository;

public class InstallApplication extends BaseTool {

	private String cmd = "cloudgene";

	public InstallApplication(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {

	}

	@Override
	public int run() {

		if (args.length != 1) {
			System.out.println("Usage: " + cmd + "install <filename|url|github> ");
			System.out.println();
			System.exit(1);
		}

		String url = args[0];

		try {

			Application application = null;

			System.out.println("Installing application " + url + "...");

			if (url.startsWith("http://") || url.startsWith("https://")) {
				application = repository.installFromUrl(url);
			} else if (url.startsWith("github://")) {

				String repo = url.replace("github://", "");

				Repository repository = GitHubUtil.parseShorthand(repo);
				if (repository == null) {
					printlnInRed("[ERROR] " + repo + " is not a valid GitHub repo.\n");
					return 1;
				}

				application = this.repository.installFromGitHub(repository);

			} else {

				if (new File(url).exists()) {

					if (url.endsWith(".zip")) {
						application = repository.installFromZipFile(url);
					} else if (url.endsWith(".yaml")) {
						application = repository.installFromYaml(url, false);
					} else {
						application = repository.installFromDirectory(url, false);
					}
					
				} else {
					String repo = url.replace("github://", "");

					Repository repository = GitHubUtil.parseShorthand(repo);
					if (repository == null) {
						printlnInRed("[ERROR] " + repo + " is not a valid GitHub repo.\n");
						return 1;
					}

					application = this.repository.installFromGitHub(repository);

				}
			}

			if (application != null) {
				settings.save();
				printlnInGreen("[OK] Application '" + application.getWdlApp().getName() + "' installed.");
				System.out.println("");
				System.out.println("The application can be started with:\n");
				System.out.println("cloudgene run " + application.getId() + "");
				System.out.println();
				return 0;
			} else {
				printlnInRed("[ERROR] No valid application found.\n");
				return 1;
			}

		} catch (Exception e) {
			printlnInRed("[ERROR] Application not installed:" + e.toString() + "\n");
			return 1;

		}
	}
}