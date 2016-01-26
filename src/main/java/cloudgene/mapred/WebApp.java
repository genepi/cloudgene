package cloudgene.mapred;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.representations.CustomStatusService;
import cloudgene.mapred.resources.Admin;
import cloudgene.mapred.resources.Index;
import cloudgene.mapred.resources.Start;
import cloudgene.mapred.resources.admin.BlockQueue;
import cloudgene.mapred.resources.admin.EnterMaintenance;
import cloudgene.mapred.resources.admin.ExitMaintenance;
import cloudgene.mapred.resources.admin.GetAllJobs;
import cloudgene.mapred.resources.admin.GetClusterDetails;
import cloudgene.mapred.resources.admin.GetSettings;
import cloudgene.mapred.resources.admin.GetStatistics;
import cloudgene.mapred.resources.admin.GetTemplates;
import cloudgene.mapred.resources.admin.OpenQueue;
import cloudgene.mapred.resources.admin.ResetDownloads;
import cloudgene.mapred.resources.admin.RestartServer;
import cloudgene.mapred.resources.admin.StartRetire;
import cloudgene.mapred.resources.admin.UpdateSettings;
import cloudgene.mapred.resources.admin.UpdateTemplate;
import cloudgene.mapred.resources.apps.GetApp;
import cloudgene.mapred.resources.apps.GetApps;
import cloudgene.mapred.resources.data.GetCounter;
import cloudgene.mapred.resources.data.ValidateImport;
import cloudgene.mapred.resources.jobs.CancelJob;
import cloudgene.mapred.resources.jobs.DeleteJob;
import cloudgene.mapred.resources.jobs.DownloadResults;
import cloudgene.mapred.resources.jobs.GetJobDetails;
import cloudgene.mapred.resources.jobs.GetJobs;
import cloudgene.mapred.resources.jobs.GetLogs;
import cloudgene.mapred.resources.jobs.GetJobStatus;
import cloudgene.mapred.resources.jobs.SubmitJob;
import cloudgene.mapred.resources.jobs.RestartJob;
import cloudgene.mapred.resources.jobs.ShareResults;
import cloudgene.mapred.resources.users.ActivateUser;
import cloudgene.mapred.resources.users.ChangeGroupUser;
import cloudgene.mapred.resources.users.DeleteUser;
import cloudgene.mapred.resources.users.GetGroups;
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
import cloudgene.mapred.util.Settings;

public class WebApp extends Application {

	private String root;

	private LocalReference webRoot;

	private LocalReference webRoot2;

	private Database database;

	private Settings settings;

	private UserSessions sessions;

	private WorkflowEngine workflowEngine;

	private Map<String, String> cacheTemplates;

	public WebApp(String root, String pages) {
		this.webRoot = LocalReference.createFileReference(new File(root));
		this.root = root;
		this.webRoot2 = LocalReference.createFileReference(new File(pages));
	}

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */

	@Override
	public synchronized Restlet createInboundRoot() {

		String prefix = settings.getUrlPrefix();
		System.out.println("Prefix: " + prefix);

		// Create a router Restlet that routes each call to a
		Router router = new Router(getContext());
		String target = prefix + "/index.html";
		Redirector redirector = new Redirector(getContext(), target,
				Redirector.MODE_CLIENT_PERMANENT);
		TemplateRoute route = router.attach(prefix, redirector);
		route.setMatchingMode(Template.MODE_EQUALS);

		route = router.attach(prefix + "/", redirector);
		route.setMatchingMode(Template.MODE_EQUALS);

		router.attach(prefix + "/index.html", Index.class);
		router.attach(prefix + "/start.html", Start.class);
		router.attach(prefix + "/admin.html", Admin.class);

		router.attach(prefix + "/jobs", GetJobs.class);
		router.attach(prefix + "/jobs/details", GetJobDetails.class); //todo: job_id in url
		router.attach(prefix + "/jobs/delete", DeleteJob.class);  //todo: job_id in url
		router.attach(prefix + "/jobs/cancel", CancelJob.class); 
		router.attach(prefix + "/jobs/restart", RestartJob.class);

		router.attach(prefix + "/jobs/newsubmit/{tool}", SubmitJob.class);
		router.attach(prefix + "/jobs/newstate", GetJobStatus.class);  //todo: job_id in url

		router.attach(prefix + "/counters", GetCounter.class);

		router.attach(prefix + "/cluster", GetClusterDetails.class);

		// router.attach("/killAllJobs", KillAllJobs.class);

		router.attach(prefix + "/results/{job}/{id}", DownloadResults.class);  //todo: jobs/job_id/results/...
		router.attach(prefix + "/results/{job}/{id}/{filename}",
				DownloadResults.class);
		router.attach(prefix + "/results/{job}/{id}/{filename}/{filename2}",
				DownloadResults.class);
		
		router.attach(prefix + "/share/{username}/{hash}/{filename}",
				ShareResults.class);

		router.attach(prefix + "/logs/{id}", GetLogs.class); //todo: jobs/job_id/logs/...
		router.attach(prefix + "/logs/{id}/{file}", GetLogs.class); //todo: jobs/job_id/logs/...

		router.attach(prefix + "/import/validate", ValidateImport.class);

		router.attach(prefix + "/app", GetApp.class);
		router.attach(prefix + "/apps", GetApps.class);

		// Users
		router.attach(prefix + "/users", GetUsers.class);
		router.attach(prefix + "/users/new", NewUser.class);
		router.attach(prefix + "/users/register", RegisterUser.class);
		router.attach(prefix + "/users/reset", ResetPassword.class);
		router.attach(prefix + "/users/activate/{user}/{code}",
				ActivateUser.class);
		router.attach(prefix + "/users/delete", DeleteUser.class);
		router.attach(prefix + "/users/changegroup}", ChangeGroupUser.class);
		router.attach(prefix + "/admin/groups}", GetGroups.class);

		router.attach(prefix + "/users/update", UpdateUser.class);
		router.attach(prefix + "/users/update2", UpdateUser2.class);
		router.attach(prefix + "/users/update-password",
				UpdateUserPassword2.class);

		router.attach(prefix + "/users/details", GetUserDetails.class);
		router.attach(prefix + "/login", LoginUser.class);
		router.attach(prefix + "/logout", LogoutUser.class);

		// Admin
		router.attach(prefix + "/admin/queue/open", OpenQueue.class);
		router.attach(prefix + "/admin/queue/block", BlockQueue.class);
		router.attach(prefix + "/admin/maintenance/enter",
				EnterMaintenance.class);
		router.attach(prefix + "/admin/maintenance/exit", ExitMaintenance.class);
		router.attach(prefix + "/admin/templates", GetTemplates.class);
		router.attach(prefix + "/admin/templates/update", UpdateTemplate.class);
		router.attach(prefix + "/admin/jobs/{job}/reset", ResetDownloads.class);
		router.attach(prefix + "/admin/jobs", GetAllJobs.class);
		router.attach(prefix + "/admin/settings", GetSettings.class);
		router.attach(prefix + "/admin/settings/update", UpdateSettings.class);
		router.attach(prefix + "/admin/server/restart", RestartServer.class);

		router.attach(prefix + "/admin/retire/start", StartRetire.class);

		router.attach(prefix + "/updateUserSettings", UpdateUserSettings.class);
		router.attach(prefix + "/updateCredentials", UpdateCredentials.class);
		router.attach(prefix + "/updateUserPassword", UpdateUserPassword.class);

		router.attach(prefix + "/console/logs/{logfile}",
				cloudgene.mapred.resources.admin.GetLogs.class);

		router.attach(prefix + "/statistics", GetStatistics.class);

		setStatusService(new CustomStatusService());

		Directory dir = new Directory(getContext(), webRoot2);
		dir.setListingAllowed(false);

		route = router.attach(prefix + "/static", dir);
		route.setMatchingMode(Template.MODE_STARTS_WITH);

		dir = new Directory(getContext(), webRoot);
		dir.setListingAllowed(false);

		route = router.attach(prefix + "/", dir);
		route.setMatchingMode(Template.MODE_STARTS_WITH);

		String[] protectedFiles = { prefix + "/start.html" };
		LoginFilter filter = new LoginFilter("/index.html", prefix,
				protectedFiles, getSessions());
		filter.setNext(router);

		return filter;
	}

	public String getRootFolder() {
		return root;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public UserSessions getSessions() {
		return sessions;
	}

	public void setSessions(UserSessions sessions) {
		this.sessions = sessions;
	}

	public WorkflowEngine getWorkflowEngine() {
		return workflowEngine;
	}

	public void setWorkflowEngine(WorkflowEngine workflowEngine) {
		this.workflowEngine = workflowEngine;
	}

	public void reloadTemplates() {
		TemplateDao dao = new TemplateDao(database);
		List<cloudgene.mapred.util.Template> templates = dao.findAll();

		cacheTemplates = new HashMap<String, String>();
		for (cloudgene.mapred.util.Template snippet : templates) {
			cacheTemplates.put(snippet.getKey(), snippet.getText());
		}
	}

	public String getTemplate(String key) {

		String template = cacheTemplates.get(key);

		if (template != null) {
			return template;
		} else {
			return "!" + key;
		}

	}

	public String getTemplate(String key, Object... strings) {

		String template = cacheTemplates.get(key);

		if (template != null) {
			return String.format(template, strings);
		} else {
			return "!" + key;
		}

	}

}