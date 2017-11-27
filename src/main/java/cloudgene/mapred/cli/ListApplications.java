package cloudgene.mapred.cli;

import cloudgene.mapred.util.Application;

public class ListApplications extends BaseTool {

	public ListApplications(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {

	}

	@Override
	public int run() {

		System.out.format("%-35s%-20s%-20s%-60s\n", "APPLICATION", "VERSION", "STATUS", "FILENAME");

		for (Application app : settings.getApps()) {
			System.out.format("%-35s%-20s%-20s%-60s\n", app.getId(), app.getWdlApp().getVersion(),
					app.hasSyntaxError() ? "Parsing error" : "OK", app.getFilename());
		}

		System.out.println();

		return 0;
	}

}