package cloudgene.mapred.cli;

import cloudgene.mapred.util.Application;

public class RemoveApplication extends BaseTool {

	private String cmd = "cloudgene";
	
	public RemoveApplication(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {
		
	}
	
	@Override
	public int run() {
		if (args.length != 1) {
			System.out.println("Usage: " + cmd + "remove <name> ");
			System.out.println();
			System.exit(1);
		}

		String id = args[0];
		try {

			Application application = settings.getApp(id);
			if (application == null) {
				System.out.println("application not found.");
				return 1;
			}

			settings.deleteApplication(application);

			System.out.println("remove okey");
			settings.save();
			return 0;

		} catch (Exception e) {
			System.out.println("remove error");
			e.printStackTrace();
			return 1;
		}

	}

}