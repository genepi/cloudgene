package cloudgene.mapred.cli;

import junit.framework.TestCase;

public class ShowPluginsTest extends TestCase {

	public static String TEST_SETTINGS = null;

	public void testShowPlugins() {
		String[] args = {};
		ShowPlugins cmd = new ShowPlugins(args);
		int result = cmd.start();
	}

}
