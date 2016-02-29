package cloudgene.mapred.steps;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import junit.framework.TestCase;
import cloudgene.mapred.util.junit.TestCluster;

public class SimpleMRTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		TestCluster.getInstance().start();
	}

	public void testMRJob() throws Exception {

		// put intput
		FileUtil.writeStringBufferToFile("input.txt", new StringBuffer(
				"lukas lukas ist super"));
		HdfsUtil.put("input.txt", "input");

		// start wordcount job
		WordCountSimple.main(new String[] { "input", "output" });

		// get output
		HdfsUtil.merge("output.txt", "output", false);
		String content = FileUtil.readFileAsString("output.txt");

		// check output
		assertTrue(content.contains("lukas\t2"));
		assertTrue(content.contains("ist\t1"));
		assertTrue(content.contains("super\t1"));

	}

}
