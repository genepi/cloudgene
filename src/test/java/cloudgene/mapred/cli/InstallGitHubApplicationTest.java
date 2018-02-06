package cloudgene.mapred.cli;

import cloudgene.mapred.cli.InstallGitHubApplication.Repository;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import genepi.io.FileUtil;
import junit.framework.TestCase;

public class InstallGitHubApplicationTest extends TestCase {

	public void testParseRepository() {
		// username/repo[/subdir][@ref]

		Repository repo = InstallGitHubApplication.parseShorthand("genepi");
		assertEquals(null, repo);

		repo = InstallGitHubApplication.parseShorthand("genepi@1.1.0");
		assertEquals(null, repo);

		repo = InstallGitHubApplication.parseShorthand("genepi/cloudgene-examples");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals(null, repo.getDirectory());
		assertEquals(null, repo.getTag());

		repo = InstallGitHubApplication.parseShorthand("genepi/cloudgene-examples/fastqc");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals("fastqc", repo.getDirectory());
		assertEquals(null, repo.getTag());

		repo = InstallGitHubApplication.parseShorthand("genepi/cloudgene-examples@1.1.0");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals(null, repo.getDirectory());
		assertEquals("1.1.0", repo.getTag());

		repo = InstallGitHubApplication.parseShorthand("genepi/cloudgene-examples/fastqc@1.1.0");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals("fastqc", repo.getDirectory());
		assertEquals("1.1.0", repo.getTag());

		repo = InstallGitHubApplication.parseShorthand("genepi/cloudgene-examples/ngs/fastqc@1.1.0");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals("ngs/fastqc", repo.getDirectory());
		assertEquals("1.1.0", repo.getTag());
	}

	public void testBuildUrlFromRepository() {
		Repository repository = new Repository();
		repository.setUser("genepi");
		repository.setRepo("cloudgene-examples");
		assertEquals("https://api.github.com/repos/genepi/cloudgene-examples/zipball",
				InstallGitHubApplication.buildUrlFromRepository(repository));

		repository = new Repository();
		repository.setUser("genepi");
		repository.setRepo("imputationserver");
		repository.setTag("1.0.2");
		assertEquals("https://api.github.com/repos/genepi/imputationserver/zipball/1.0.2",
				InstallGitHubApplication.buildUrlFromRepository(repository));

		repository = new Repository();
		repository.setUser("genepi");
		repository.setRepo("imputationserver");
		repository.setDirectory("subdir/subdir2");
		repository.setTag("1.0.2");
		assertEquals("https://api.github.com/repos/genepi/imputationserver/zipball/1.0.2",
				InstallGitHubApplication.buildUrlFromRepository(repository));
	}
	
	public void testInstallFromRepository(){
		String[] args = {"examples","genepi/cloudgene-examples"};
		InstallGitHubApplication cmd = new InstallGitHubApplication(args){
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

	
	public void testInstallFromRepositoryDirectory(){
		String[] args = {"fastqc","genepi/cloudgene-examples/fastqc"};
		InstallGitHubApplication cmd = new InstallGitHubApplication(args){
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
