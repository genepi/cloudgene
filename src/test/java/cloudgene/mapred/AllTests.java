package cloudgene.mapred;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import cloudgene.mapred.api.v2.jobs.CancelJobTest;
import cloudgene.mapred.api.v2.jobs.DeleteJobTest;
import cloudgene.mapred.api.v2.jobs.DownloadResultsTest;
import cloudgene.mapred.api.v2.jobs.GetJobsTest;
import cloudgene.mapred.api.v2.jobs.GetLogsTest;
import cloudgene.mapred.api.v2.jobs.RestartJobTest;
import cloudgene.mapred.api.v2.jobs.ShareResultsTest;
import cloudgene.mapred.api.v2.jobs.SubmitJobTest;
import cloudgene.mapred.api.v2.users.RegisterUserTest;
import cloudgene.mapred.api.v2.users.UserProfileTest;
import cloudgene.mapred.core.UserTest;
import cloudgene.mapred.database.JobDaoTest;
import cloudgene.mapred.jobs.PriorityThreadPoolExecutorTest;
import cloudgene.mapred.jobs.WorkflowEngineTest;
import cloudgene.mapred.jobs.WrongWorkspaceTest;
import cloudgene.mapred.steps.TestCommand;

@RunWith(Suite.class)
@SuiteClasses({ SubmitJobTest.class, WorkflowEngineTest.class, RestartJobTest.class, CancelJobTest.class,
		DeleteJobTest.class, DownloadResultsTest.class, ShareResultsTest.class, GetLogsTest.class, GetJobsTest.class,
		TestCommand.class, JobDaoTest.class, PriorityThreadPoolExecutorTest.class, WrongWorkspaceTest.class,
		UserTest.class, UserProfileTest.class, RegisterUserTest.class })
public class AllTests {

}
