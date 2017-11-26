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
		for (Application app : settings.getApps()) {
			System.out.println(app.getId() + "\t\t" + app.getFilename());
		}

		return 0;
	}

}