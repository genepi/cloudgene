package cloudgene.mapred.cli;

import java.util.List;

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

		ListApplications.printApplicationList(settings.getApps());

		return 0;
	}
	
	public static void printApplicationList(List<Application> applications){
		System.out.format("%-35s%-20s%-20s%-60s\n", "APPLICATION", "VERSION", "STATUS", "FILENAME");

		for (Application app : applications) {
			System.out.format("%-35s%-20s%-20s%-60s\n", app.getId(), app.getWdlApp().getVersion(),
					app.hasSyntaxError() ? "Parsing error" : "OK", app.getFilename());
		}

		System.out.println();
	}

}