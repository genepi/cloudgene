package cloudgene.mapred.cli;

import java.util.List;
import java.util.Vector;

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

		if (args.length != 2) {
			System.out.println("Usage: " + cmd + "install <name> <filename|url> ");
			System.out.println();
			System.exit(1);
		}

		String id = args[0];
		String url = args[1];

		try {

			List<Application> installed = new Vector<Application>();

			if (repository.getById(id) != null) {
				printlnInRed("[ERROR] An application with id '" + id + "' is already installed.\n");
				return 1;
			}

			System.out.println("Installing application " + id + "...");

			if (url.startsWith("http://") || url.startsWith("https://")) {
				installed = repository.installFromUrl(id, url);
			} else {
				if (url.endsWith(".zip")) {
					installed = repository.installFromZipFile(id, url);
				} else if (url.endsWith(".yaml")) {
					Application application = repository.installFromYaml(id, url);
					if (application != null) {
						installed.add(application);
					}
				} else {
					installed = repository.installFromDirectory(id, url);
				}
			}

			if (installed.size() > 0) {
				settings.save();
				printlnInGreen("[OK] " + installed.size() + " Applications installed: \n");
				ListApplications.printApplicationList(installed);
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