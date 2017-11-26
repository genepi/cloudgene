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
				System.out.println("installation okey");
				settings.save();
				return 0;
			} else {
				System.out.println("installation error");
				return 1;
			}

		} catch (Exception e) {

			System.out.println("installation error");
			e.printStackTrace();
			return 1;

		}
	}
}