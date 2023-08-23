package cloudgene.mapred.cli;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.esotericsoftware.yamlbeans.YamlReader;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.util.S3Util;

public class CloneApplications extends BaseTool {

	private String cmd = "cloudgene";

	public CloneApplications(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {

	}

	@Override
	public int run() {

		if (args.length != 1) {
			System.out.println("Usage: " + cmd + "clone <filename|url> ");
			System.out.println();
			System.exit(1);
		}

		String repo = args[0];

		String tmpFilename = "repo.yaml";
		if (repo.startsWith("http://") || repo.startsWith("https://")) {
			try {
				FileUtils.copyURLToFile(new URL(repo), new File(tmpFilename));
				repo = tmpFilename;
			} catch (Exception e) {
				System.out.println("Error during download repository from " + repo);
				e.printStackTrace();
				return 1;
			}
		} else if (repo.startsWith("s3://")) {
			try {
				S3Util.copyToFile(repo, new File(tmpFilename));
				repo = tmpFilename;
			} catch (Exception e) {
				System.out.println("Error during download repository from " + repo);
				e.printStackTrace();
				return 1;
			}
		}

		try {
			YamlReader reader = new YamlReader(new FileReader(repo));
			while (true) {
				Map entry = reader.read(Map.class);
				if (entry == null) {
					break;
				}
				String url = entry.get("url").toString();

				Application application = null;

				System.out.println("Installing application " + url + "...");
				
				try {

					application = repository.install(url);

					if (application != null) {
						settings.save();
						printlnInGreen("[OK] Application installed: \n");
					} else {
						printlnInRed("[ERROR] No valid Application found in repo '" + url + "'\n");
						return 1;
					}

				} catch (Exception e) {

					printlnInRed("[ERROR] Application not installed:" + e.toString() + "\n");

				}

			}
			reader.close();
		} catch (Exception e) {
			printlnInRed("[ERROR] Error reading file '" + repo + "':" + e.toString() + "\n");
		}
		return 0;

	}
}