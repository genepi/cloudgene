package cloudgene.mapred.util;

import cloudgene.mapred.util.GitHubUtil.Repository;
import junit.framework.TestCase;

public class GitHubUtilTest extends TestCase {

	public void testParseShorthand() {
		// username/repo[/subdir][@ref]

		Repository repo = GitHubUtil.parseShorthand("genepi");
		assertEquals(null, repo);

		repo = GitHubUtil.parseShorthand("genepi@1.1.0");
		assertEquals(null, repo);

		repo = GitHubUtil.parseShorthand("genepi/cloudgene-examples");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals(null, repo.getDirectory());
		assertEquals(null, repo.getTag());

		repo = GitHubUtil.parseShorthand("genepi/cloudgene-examples/fastqc");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals("fastqc", repo.getDirectory());
		assertEquals(null, repo.getTag());

		repo = GitHubUtil.parseShorthand("genepi/cloudgene-examples@1.1.0");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals(null, repo.getDirectory());
		assertEquals("1.1.0", repo.getTag());

		repo = GitHubUtil.parseShorthand("genepi/cloudgene-examples/fastqc@1.1.0");
		assertEquals("genepi", repo.getUser());
		assertEquals("cloudgene-examples", repo.getRepo());
		assertEquals("fastqc", repo.getDirectory());
		assertEquals("1.1.0", repo.getTag());

		repo = GitHubUtil.parseShorthand("genepi/cloudgene-examples/ngs/fastqc@1.1.0");
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
				GitHubUtil.buildUrlFromRepository(repository));

		repository = new Repository();
		repository.setUser("genepi");
		repository.setRepo("imputationserver");
		repository.setTag("1.0.2");
		assertEquals("https://api.github.com/repos/genepi/imputationserver/zipball/1.0.2",
				GitHubUtil.buildUrlFromRepository(repository));

		repository = new Repository();
		repository.setUser("genepi");
		repository.setRepo("imputationserver");
		repository.setDirectory("subdir/subdir2");
		repository.setTag("1.0.2");
		assertEquals("https://api.github.com/repos/genepi/imputationserver/zipball/1.0.2",
				GitHubUtil.buildUrlFromRepository(repository));
	}

	public void testGetLatestReleaseFromRepository() {
		Repository repository = new Repository();
		repository.setUser("lukfor");
		repository.setRepo("hello-cloudgene");
		repository.setTag("latest");

		String latest = GitHubUtil.getLatestReleaseFromRepository(repository);
		assertEquals("1.2.0", latest);

		assertEquals("https://api.github.com/repos/lukfor/hello-cloudgene/zipball/1.2.0",
				GitHubUtil.buildUrlFromRepository(repository));
	}

}
