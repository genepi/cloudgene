package cloudgene.mapred.cli;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.util.GitHubException;

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
			System.out.println("Usage: " + cmd + " install <filename|url|github> ");
			System.out.println();
			System.exit(1);
		}

		String url = args[0];

		try {

			System.out.println("Installing application " + url + "...");

			Application application = repository.install(url);

			if (application != null) {
				settings.save();
				printlnInGreen("[OK] Application '" + application.getWdlApp().getName() + "' installed.");
				System.out.println();
				return 0;
			} else {
				printlnInRed("[ERROR] No valid application found.\n");
				return 1;
			}
		} catch (GitHubException e) {
			printlnInRed("[ERROR] " + e.getMessage() + ".\n");
			return 1;
		} catch (Exception e) {
			printlnInRed("[ERROR] Application not installed:" + e.toString() + "\n");
			return 1;

		}
	}
}