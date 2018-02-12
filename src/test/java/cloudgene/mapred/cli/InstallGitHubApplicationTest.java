package cloudgene.mapred.cli;

import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;
import junit.framework.TestCase;

public class InstallGitHubApplicationTest extends TestCase {

	public void testInstallFromRepository() {
		String[] args = { "genepi/cloudgene-examples" };
		InstallGitHubApplication cmd = new InstallGitHubApplication(args) {
			@Override
			public void init() {
				Config config = new Config();
				config.setApps("test-github");
				FileUtil.deleteDirectory("test-github");
				settings = new Settings(config);
			}
		};
		int result = cmd.start();
		assertEquals(0, result);
	}

	public void testInstallFromRepositoryWithId() {
		String[] args = { "genepi/cloudgene-examples", "--name", "hello" };
		InstallGitHubApplication cmd2 = new InstallGitHubApplication(args) {
			@Override
			public void init() {
				Config config = new Config();
				config.setApps("test-github");
				FileUtil.deleteDirectory("test-github");
				settings = new Settings(config);
			}
		};
		int result = cmd2.start();
		assertEquals(0, result);

		args = new String[] { "genepi/cloudgene-examples", "--name", "hello" };
		InstallGitHubApplication cmd = new InstallGitHubApplication(args) {
			@Override
			public void init() {
				// reuse setttings
				settings = cmd2.settings;
			}
		};
		result = cmd.start();
		assertEquals(1, result);

		args = new String[] { "genepi/cloudgene-examples", "--name", "hello", "--update" };
		cmd = new InstallGitHubApplication(args) {
			@Override
			public void init() {
				// reuse setttings
				settings = cmd2.settings;
			}
		};
		result = cmd.start();
		assertEquals(0, result);

	}

	public void testInstallFromRepositoryDirectory() {
		String[] args = { "genepi/cloudgene-examples/fastqc" };
		InstallGitHubApplication cmd = new InstallGitHubApplication(args) {
			@Override
			public void init() {
				Config config = new Config();
				config.setApps("test-github");
				FileUtil.deleteDirectory("test-github");
				settings = new Settings(config);
			}
		};
		int result = cmd.start();

		assertEquals(0, result);

		args = new String[] { "genepi/cloudgene-examples/vcf-tools" };
		InstallGitHubApplication cmd2 = new InstallGitHubApplication(args) {
			@Override
			public void init() {
				// reuse setttings
				settings = cmd.settings;
			}
		};
		result = cmd2.start();

		assertEquals(0, result);

	}

	public void testInstallHello() {
		String[] args = { "lukfor/hello-cloudgene@latest" };
		InstallGitHubApplication cmd = new InstallGitHubApplication(args) {
			@Override
			public void init() {
				Config config = new Config();
				config.setApps("test-github");
				FileUtil.deleteDirectory("test-github");
				settings = new Settings(config);
			}
		};
		int result = cmd.start();

		assertEquals(0, result);
	}

	public void testInstallMTDNA() {
		String[] args = { "seppinho/mtdna-server-workflow@latest" };
		InstallGitHubApplication cmd = new InstallGitHubApplication(args) {
			@Override
			public void init() {
				Config config = new Config();
				config.setApps("test-github");
				FileUtil.deleteDirectory("test-github");
				settings = new Settings(config);
			}
		};
		int result = cmd.start();

		assertEquals(0, result);
	}

}
