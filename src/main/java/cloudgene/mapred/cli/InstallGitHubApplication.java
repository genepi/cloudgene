package cloudgene.mapred.cli;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.util.GitHubUtil;
import cloudgene.mapred.util.GitHubUtil.Repository;

public class InstallGitHubApplication extends BaseTool {

	private String cmd = "cloudgene";

	public InstallGitHubApplication(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {

	}

	@Override
	public int run() {
		return 0;
	}

	@Override
	public int start() {

		// call init manualy
		init();

		if (args.length < 1) {
			System.out.println("Usage: " + cmd + " gh <GitHub repo>");
			System.out.println();
			System.exit(1);
		}

		String repo = args[0];
		repo = repo.replaceAll("github://", "");

		// create the command line parser

		try {

			Repository repository = GitHubUtil.parseShorthand(repo);
			if (repository == null) {
				printlnInRed("[ERROR] " + repo + " is not a valid GitHub repo.\n");
				return 1;
			}

			Application application = this.repository.installFromGitHub(repository);

			if (application != null) {
				settings.save();
				printlnInGreen("[OK] Application installed: \n");
				//ListApplications.printApplicationList(applications);(installed);
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

}