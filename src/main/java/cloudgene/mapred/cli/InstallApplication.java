package cloudgene.mapred.cli;

import cloudgene.mapred.apps.Application;

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
			System.out.println("Usage: " + cmd + "install <filename|url> ");
			System.out.println();
			System.exit(1);
		}

		String url = args[0];

		try {

			Application application = null;

			System.out.println("Installing application " + url + "...");

			if (url.startsWith("http://") || url.startsWith("https://")) {
				application = repository.installFromUrl(url);
			} else {
				if (url.endsWith(".zip")) {
					application = repository.installFromZipFile(url);
				} else if (url.endsWith(".yaml")) {
					application = repository.installFromYaml(url, false);
				} else {
					application = repository.installFromDirectory(url, false);
				}
			}

			if (application != null) {
				settings.save();
				printlnInGreen("[OK] Application installed: \n");
				// ListApplications.printApplicationList(applications);(installed);
				return 0;
			} else {
				printlnInRed("[ERROR] No valid Application found.\n");
				return 1;
			}

		} catch (Exception e) {

			printlnInRed("[ERROR] Application not installed:" + e.toString() + "\n");
			return 1;

		}
	}
}