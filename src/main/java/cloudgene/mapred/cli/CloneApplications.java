package cloudgene.mapred.cli;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import com.esotericsoftware.yamlbeans.YamlReader;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.util.GitHubUtil;
import cloudgene.mapred.util.GitHubUtil.Repository;
import genepi.io.FileUtil;

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
		}

		String folderRepo = new File(repo).getParentFile().getAbsolutePath();

		try {
			YamlReader reader = new YamlReader(new FileReader(repo));
			while (true) {
				Map entry = reader.read(Map.class);
				if (entry == null) {
					break;
				}
				String id = entry.get("id").toString();
				String url = entry.get("url").toString();

				if (repository.getById(id) == null) {

					List<Application> installed = new Vector<Application>();

					System.out.println("Installing application " + id + "...");

					try {

						if (url.startsWith("http://") || url.startsWith("https://")) {
							installed = repository.installFromUrl(id, url);
						} else if (url.startsWith("github://")) {
							String shorthand = url.replaceAll("github://", "");
							Repository repository = GitHubUtil.parseShorthand(shorthand);
							if (repository == null) {
								printlnInRed("[ERROR] " + shorthand + " is not a valid GitHub repo.");
								return 1;

							}
							String newId = repository.getUser() + "-" + repository.getRepo();
							if (repository.getDirectory() != null) {
								newId += "-" + repository.getDirectory();
							}
							installed = this.repository.installFromGitHub(newId, repository, false);
						} else {

							if (!url.startsWith("/")) {
								url = FileUtil.path(folderRepo, url);
							}

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
						} else {
							printlnInRed("[ERROR] No valid Application found in repo '" + url + "'\n");
							return 1;
						}

					} catch (Exception e) {

						printlnInRed("[ERROR] Application not installed:" + e.toString() + "\n");

					}

				} else {
					printlnInGreen("[OK] Application " + id + " is already installed.");

				}
			}
			reader.close();
		} catch (Exception e) {
			printlnInRed("[ERROR] Error reading file '" + repo + "':" + e.toString() + "\n");
		}
		return 0;

	}
}