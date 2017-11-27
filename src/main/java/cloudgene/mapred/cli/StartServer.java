package cloudgene.mapred.cli;

import cloudgene.mapred.Main;

public class StartServer extends BaseTool {

	private String cmd = "cloudgene";

	public StartServer(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {

	}

	@Override
	public int run() {

		Main main = new Main();
		try {
			main.runCloudgene(args);
			while (true) {
				Thread.sleep(5000000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
}