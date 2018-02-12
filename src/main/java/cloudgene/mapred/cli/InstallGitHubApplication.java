package cloudgene.mapred.cli;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import cloudgene.mapred.util.Application;
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
			System.out.println("Usage: " + cmd + " gh <GitHub repo> [--name <name>] [--update]");
			System.out.println();
			System.exit(1);
		}

		String repo = args[0];

		// create the command line parser
		CommandLineParser parser = new PosixParser();
		Options options = new Options();
		Option idOption = new Option(null, "name", true, "Custom application name");
		idOption.setRequired(false);
		options.addOption(idOption);
		Option forceOption = new Option(null, "update", false, "Force application update");
		forceOption.setRequired(false);
		options.addOption(forceOption);

		// parse the command line arguments
		CommandLine line = null;
		try {

			line = parser.parse(options, args);

		} catch (Exception e) {
			printError(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Arguments:", options);
			System.out.println();
			return 1;
		}

		try {

			Repository repository = GitHubUtil.parseShorthand(repo);
			if (repository == null) {
				printlnInRed("[ERROR] " + repo + " is not a valid GitHub repo.\n");
				return 1;
			}

			// create id from github shorthand

			String id = repository.getUser() + "-" + repository.getRepo();
			if (repository.getDirectory() != null){
				id += "-" + repository.getDirectory();
			}
			if (line.hasOption("name")) {
				id = line.getOptionValue("name");
			}

			boolean update = line.hasOption("update");
			List<Application> applications = getSettings().installApplicationFromGitHub(id, repository, update);

			if (applications.size() > 0) {
				settings.save();
				printlnInGreen("[OK] " + applications.size() + " Application(s) installed: \n");
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

}