package cloudgene.mapred.cli;

import org.junit.jupiter.api.Test;

public class ShowPluginsTest {

	public static String TEST_SETTINGS = null;

	@Test
	public void testShowPlugins() {
		String[] args = {};
		ShowPlugins cmd = new ShowPlugins(args);
		int result = cmd.start();
	}

}
