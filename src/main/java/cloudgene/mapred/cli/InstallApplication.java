package cloudgene.mapred.cli;

import java.util.List;
import java.util.Vector;

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

			List<Application> applications = new Vector<Application>();

			if (settings.getApp(id) != null) {
				printlnInRed("[ERROR] An application with id '" + id + "' is already installed.\n");
				return 1;
			}

			System.out.println("Installing application " + id + "...");

			if (url.startsWith("http://") || url.startsWith("https://")) {
				applications = getSettings().installApplicationFromUrl(id, url);
			} else {
				if (url.endsWith(".zip")) {
					applications = getSettings().installApplicationFromZipFile(id, url);
				} else if (url.endsWith(".yaml")) {
					Application application = getSettings().installApplicationFromYaml(id, url);
					if (application != null) {
						applications.add(application);
					}
				} else {
					applications = getSettings().installApplicationFromDirectory(id, url);
				}
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

			printlnInRed("[ERROR] Application not installed:" + e.toString() + "\n");
			return 1;

		}
	}
}