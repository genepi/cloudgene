package cloudgene.mapred;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cloudgene.mapred.api.v2.jobs.CancelJobTest;
import cloudgene.mapred.api.v2.jobs.DeleteJobTest;
import cloudgene.mapred.api.v2.jobs.DownloadResultsTest;
import cloudgene.mapred.api.v2.jobs.RestartJobTest;
import cloudgene.mapred.api.v2.jobs.ShareResultsTest;
import cloudgene.mapred.api.v2.jobs.SubmitJobTest;
import cloudgene.mapred.jobs.WorkflowEngineTest;

@RunWith(Suite.class)
@SuiteClasses({ SubmitJobTest.class, WorkflowEngineTest.class,
		RestartJobTest.class, CancelJobTest.class, DeleteJobTest.class,
		DownloadResultsTest.class, ShareResultsTest.class })
public class AllTests {

}
