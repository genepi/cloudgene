package cloudgene.mapred.cli;

import cloudgene.mapred.util.Application;

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

		if (args.length != 2) {
			System.out.println("Usage: " + cmd + "install <name> <filename|url> ");
			System.out.println();
			System.exit(1);
		}

		String id = args[0];
		String url = args[1];

		try {

			Application application = null;

			if (settings.getApp(id) != null) {
				printlnInRed("[ERROR] An application with id '" + id + "' is already installed.\n");
				return 1;
			}

			System.out.println("Installing application " + id + "...");

			if (url.startsWith("http://") || url.startsWith("https://")) {
				application = getSettings().installApplicationFromUrl(id, url);
			} else {
				if (url.endsWith(".zip")) {
					application = getSettings().installApplicationFromZipFile(id, url);
				} else if (url.endsWith(".yaml")) {
					application = getSettings().installApplicationFromYaml(id, url);
				} else {
					application = getSettings().installApplicationFromDirectory(id, url);
				}
			}

			if (application != null) {
				settings.save();
				printlnInGreen("[OK] Application installed.\n");
				return 0;
			} else {
				printlnInRed("[ERROR] Application not installed.\n");
				return 1;
			}

		} catch (Exception e) {

			printlnInRed("[ERROR] Application not installed:" + e.toString() + "\n");

			return 1;

		}
	}
}