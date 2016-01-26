package cloudgene.mapred;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cloudgene.mapred.api.v2.jobs.SubmitJobTest;
import cloudgene.mapred.jobs.WorkflowEngineTest;

@RunWith(Suite.class)
@SuiteClasses({ SubmitJobTest.class, WorkflowEngineTest.class })
public class AllTests {

}
