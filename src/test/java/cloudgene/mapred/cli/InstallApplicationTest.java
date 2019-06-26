package cloudgene.mapred.cli;

import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;
import junit.framework.TestCase;

public class InstallApplicationTest extends TestCase {

	public static String TEST_SETTINGS = null;

	public void testInstall() {
		assertTrue(true);
	}

	public void testInstallFromRepository() {
		String[] args = { "github://genepi/cloudgene-examples" };
		InstallApplication cmd = new InstallApplication(args) {
			@Override
			public void init() {
				Config config = new Config();
				config.setSettings(TEST_SETTINGS);
				config.setApps("test-github");
				FileUtil.deleteDirectory("test-github");
				settings = new Settings(config);
				repository = settings.getApplicationRepository();
			}
		};
		int result = cmd.start();
		assertEquals(0, result);
		ApplicationRepository repository = cmd.settings.getApplicationRepository();
		assertEquals(1, repository.getAll().size());
	}

	public void testInstallFromRepositoryDirectory() {
		String[] args = { "github://genepi/cloudgene-examples/fastqc" };
		InstallApplication cmd = new InstallApplication(args) {
			@Override
			public void init() {
				Config config = new Config();
				config.setSettings(TEST_SETTINGS);
				config.setApps("test-github");
				FileUtil.deleteDirectory("test-github");
				settings = new Settings(config);
				repository = settings.getApplicationRepository();
			}
		};
		int result = cmd.start();

		assertEquals(0, result);
		ApplicationRepository repository = cmd.settings.getApplicationRepository();
		assertEquals(1, repository.getAll().size());

		args = new String[] { "github://genepi/cloudgene-examples/vcf-tools" };
		InstallApplication cmd2 = new InstallApplication(args) {
			@Override
			public void init() {
				// reuse setttings
				settings = cmd.settings;
				repository = settings.getApplicationRepository();
			}
		};
		result = cmd2.start();

		assertEquals(0, result);
		assertEquals(2, repository.getAll().size());
	}

	public void testInstallHello() {
		String[] args = { "github://lukfor/hello-cloudgene@latest" };
		InstallApplication cmd = new InstallApplication(args) {
			@Override
			public void init() {

				Config config = new Config();
				config.setSettings(TEST_SETTINGS);
				config.setApps("test-github");
				FileUtil.deleteDirectory("test-github");
				settings = new Settings(config);
				repository = settings.getApplicationRepository();
			}
		};
		int result = cmd.start();

		assertEquals(0, result);
		ApplicationRepository repository = cmd.settings.getApplicationRepository();
		assertEquals(1, repository.getAll().size());
	}

}
