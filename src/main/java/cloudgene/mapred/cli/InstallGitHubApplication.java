package cloudgene.mapred.cli;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import cloudgene.mapred.util.Application;
import genepi.io.FileUtil;

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

		if (args.length != 2) {
			System.out.println("Usage: " + cmd + "gh <name> <GitHub repo> ");
			System.out.println();
			System.exit(1);
		}

		String id = args[0];
		String repo = args[1];

		//TODO: check repo structure and support tags, commits
		
		try {

			List<Application> applications = new Vector<Application>();

			if (settings.getApp(id) != null) {
				printlnInRed("[ERROR] An application with id '" + id + "' is already installed.\n");
				return 1;
			}

			System.out.println("Installing application " + id + "...");

			String url = "https://api.github.com/repos/" + repo + "/zipball/master";

			String zipFilename = FileUtil.path(settings.getTempPath(), "github.zip");
			FileUtils.copyURLToFile(new URL(url), new File(zipFilename));

			applications = getSettings().installApplicationFromZipFile(id, zipFilename);

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