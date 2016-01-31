package cloudgene.mapred.steps;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.cli.TestCLI;

import junit.framework.TestCase;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.AbstractJob;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.TestCluster;
import cloudgene.mapred.util.TestServer;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class SimpleMRTest extends TestCase {

	private TestCluster cluster;

	@Override
	protected void setUp() throws Exception {
		TestCluster.getInstance().start();

	}
	
	public void testMRJob() throws Exception{
		FileUtil.writeStringBufferToFile("input.txt", new StringBuffer("lukas lukas ist super"));
		HdfsUtil.put("input.txt", "input");
		WordCountSimple.main(new String[]{"input", "output"});
		HdfsUtil.merge("output.txt", "output", false);
		String content = FileUtil.readFileAsString("output.txt");
		assertTrue(content.contains("lukas\t2"));
		assertTrue(content.contains("ist\t1"));
		assertTrue(content.contains("super\t1"));
	}
}
