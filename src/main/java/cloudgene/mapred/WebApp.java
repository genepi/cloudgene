package cloudgene.mapred;

import java.io.File;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

import com.sun.org.glassfish.external.statistics.annotations.Reset;

import cloudgene.mapred.representations.CustomStatusService;
import cloudgene.mapred.resources.Admin;
import cloudgene.mapred.resources.Index;
import cloudgene.mapred.resources.Start;
import cloudgene.mapred.resources.admin.BlockQueue;
import cloudgene.mapred.resources.admin.EnterMaintenance;
import cloudgene.mapred.resources.admin.ExitMaintenance;
import cloudgene.mapred.resources.admin.GetAllJobs;
import cloudgene.mapred.resources.admin.GetClusterDetails;
import cloudgene.mapred.resources.admin.GetStatistics;
import cloudgene.mapred.resources.admin.GetTemplates;
import cloudgene.mapred.resources.admin.OpenQueue;
import cloudgene.mapred.resources.admin.StartRetire;
import cloudgene.mapred.resources.admin.UpdateHtmlSnippet;
import cloudgene.mapred.resources.apps.GetApp;
import cloudgene.mapred.resources.apps.GetAppDetails;
import cloudgene.mapred.resources.apps.GetAppParams;
import cloudgene.mapred.resources.apps.GetApps;
import cloudgene.mapred.resources.apps.GetAppsFromRepository;
import cloudgene.mapred.resources.apps.InstallApp;
import cloudgene.mapred.resources.data.FileUploadRessource;
import cloudgene.mapred.resources.data.GetBucketsPrivate;
import cloudgene.mapred.resources.data.GetBucketsPublic;
import cloudgene.mapred.resources.data.GetCounter;
import cloudgene.mapred.resources.data.GetFileList;
import cloudgene.mapred.resources.data.GetFolderList;
import cloudgene.mapred.resources.data.GetFormatsList;
import cloudgene.mapred.resources.data.GetLocalFiles;
import cloudgene.mapred.resources.data.GetMyBuckets;
import cloudgene.mapred.resources.data.GetSftpFiles;
import cloudgene.mapred.resources.data.ImportFiles;
import cloudgene.mapred.resources.data.NewFolder;
import cloudgene.mapred.resources.data.RemoveFiles;
import cloudgene.mapred.resources.data.RenameFile;
import cloudgene.mapred.resources.data.ValidateImport;
import cloudgene.mapred.resources.jobs.CancelJob;
import cloudgene.mapred.resources.jobs.DeleteJob;
import cloudgene.mapred.resources.jobs.DownloadResults;
import cloudgene.mapred.resources.jobs.GetJobDetails;
import cloudgene.mapred.resources.jobs.GetJobStatus;
import cloudgene.mapred.resources.jobs.GetJobs;
import cloudgene.mapred.resources.jobs.GetLogs;
import cloudgene.mapred.resources.jobs.NewGetJobStatus;
import cloudgene.mapred.resources.jobs.NewSubmitJob;
import cloudgene.mapred.resources.jobs.RerunJob;
import cloudgene.mapred.resources.jobs.ShareResults;
import cloudgene.mapred.resources.jobs.SubmitJob;
import cloudgene.mapred.resources.users.ActivateUser;
import cloudgene.mapred.resources.users.DeleteUser;
import cloudgene.mapred.resources.users.GetUserDetails;
import cloudgene.mapred.resources.users.GetUsers;
import cloudgene.mapred.resources.users.LoginUser;
import cloudgene.mapred.resources.users.LogoutUser;
import cloudgene.mapred.resources.users.NewUser;
import cloudgene.mapred.resources.users.RegisterUser;
import cloudgene.mapred.resources.users.ResetPassword;
import cloudgene.mapred.resources.users.UpdateCredentials;
import cloudgene.mapred.resources.users.UpdateUser;
import cloudgene.mapred.resources.users.UpdateUser2;
import cloudgene.mapred.resources.users.UpdateUserPassword;
import cloudgene.mapred.resources.users.UpdateUserPassword2;
import cloudgene.mapred.resources.users.UpdateUserSettings;
import cloudgene.mapred.util.LoginFilter;

public class WebApp extends Application {

	private String root;
	
	private LocalReference webRoot;

	private LocalReference webRoot2;

	public WebApp(String root, String pages) {
		this.webRoot = LocalReference
				.createFileReference(new File(root));
		this.root = root;
		this.webRoot2 = LocalReference
				.createFileReference(new File(pages));
	}

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */

	@Override
	public synchronized Restlet createInboundRoot() {
		// Create a router Restlet that routes each call to a
		Router router = new Router(getContext());
		String target = "riap://host/index.html";
		Redirector redirector = new Redirector(getContext(), target,
				Redirector.MODE_SERVER_OUTBOUND);
		TemplateRoute route = router.attach("/", redirector);
		route.setMatchingMode(Template.MODE_EQUALS);

		router.attach("/index.html", Index.class);
		router.attach("/start.html", Start.class);
		router.attach("/admin.html", Admin.class);

		router.attach("/jobs", GetJobs.class);
		router.attach("/jobs/state", GetJobStatus.class);
		router.attach("/jobs/details", GetJobDetails.class);
		router.attach("/jobs/delete", DeleteJob.class);
		router.attach("/jobs/rerun", RerunJob.class);
		router.attach("/jobs/cancel", CancelJob.class);
		router.attach("/jobs/submit", SubmitJob.class);

		router.attach("/jobs/newsubmit", NewSubmitJob.class);
		router.attach("/jobs/newstate", NewGetJobStatus.class);

		router.attach("/counters", GetCounter.class);

		router.attach("/cluster", GetClusterDetails.class);

		// router.attach("/killAllJobs", KillAllJobs.class);

		router.attach("/results/{job}/{id}", DownloadResults.class);
		router.attach("/results/{job}/{id}/{filename}", DownloadResults.class);
		router.attach("/results/{job}/{id}/{filename}/{filename2}",
				DownloadResults.class);
		router.attach("/share/{username}/{hash}/{filename}", ShareResults.class);

		router.attach("/logs/{id}", GetLogs.class);
		router.attach("/logs/{id}/{file}", GetLogs.class);

		router.attach("/hdfs/files", GetFileList.class);
		router.attach("/hdfs/format/{format}", GetFormatsList.class);
		router.attach("/hdfs/folders", GetFolderList.class);
		router.attach("/hdfs/import", ImportFiles.class);
		router.attach("/hdfs/upload", FileUploadRessource.class);
		router.attach("/hdfs/delete", RemoveFiles.class);
		router.attach("/hdfs/new", NewFolder.class);
		router.attach("/hdfs/rename", RenameFile.class);

		router.attach("/local/files", GetLocalFiles.class);

		router.attach("/sftp/files", GetSftpFiles.class);

		router.attach("/import/validate", ValidateImport.class);

		router.attach("/buckets/public", GetBucketsPublic.class);
		router.attach("/buckets/private", GetBucketsPrivate.class);
		router.attach("/buckets/my", GetMyBuckets.class);

		router.attach("/app", GetApp.class);
		router.attach("/apps", GetApps.class);
		router.attach("/apps/details", GetAppDetails.class);
		router.attach("/apps/params", GetAppParams.class);

		// Users
		router.attach("/users", GetUsers.class);
		router.attach("/users/new", NewUser.class);
		router.attach("/users/register", RegisterUser.class);
		router.attach("/users/reset", ResetPassword.class);
		router.attach("/users/activate/{user}/{code}", ActivateUser.class);		
		router.attach("/users/delete", DeleteUser.class);
		router.attach("/users/update", UpdateUser.class);
		router.attach("/users/update2", UpdateUser2.class);
		router.attach("/users/update-password", UpdateUserPassword2.class);

		router.attach("/users/details", GetUserDetails.class);
		router.attach("/login", LoginUser.class);
		router.attach("/logout", LogoutUser.class);

		// Admin
		router.attach("/admin/queue/open", OpenQueue.class);
		router.attach("/admin/queue/block", BlockQueue.class);
		router.attach("/admin/maintenance/enter", EnterMaintenance.class);
		router.attach("/admin/maintenance/exit", ExitMaintenance.class);
		router.attach("/admin/templates", GetTemplates.class);
		router.attach("/admin/templates/update", UpdateHtmlSnippet.class);
		router.attach("/admin/jobs", GetAllJobs.class);

		router.attach("/admin/retire/start", StartRetire.class);

		router.attach("/updateUserSettings", UpdateUserSettings.class);
		router.attach("/updateCredentials", UpdateCredentials.class);
		router.attach("/updateUserPassword", UpdateUserPassword.class);

		router.attach("/installApp", InstallApp.class);
		router.attach("/getAppsFromRepo", GetAppsFromRepository.class);

		router.attach("/console/logs/{logfile}",
				cloudgene.mapred.resources.admin.GetLogs.class);

		router.attach("/statistics", GetStatistics.class);

		setStatusService(new CustomStatusService());

		Directory dir = new Directory(getContext(), webRoot2);
		dir.setListingAllowed(false);

		route = router.attach("/static", dir);
		route.setMatchingMode(Template.MODE_STARTS_WITH);

		dir = new Directory(getContext(), webRoot);
		dir.setListingAllowed(false);

		route = router.attach("/", dir);
		route.setMatchingMode(Template.MODE_STARTS_WITH);

		String[] protectedFiles = { "/start.html" };
		LoginFilter filter = new LoginFilter("/", protectedFiles);
		filter.setNext(router);

		return filter;
	}
	
	public String getRootFolder(){
		return root;
	}

}