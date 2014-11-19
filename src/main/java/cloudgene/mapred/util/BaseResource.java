package cloudgene.mapred.util;

import org.restlet.Request;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.WebApp;
import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.jobs.WorkflowEngine;

public class BaseResource extends ServerResource {

	private WebApp application;

	private Database database;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();

		application = (WebApp) getApplication();
		database = application.getDatabase();

	}

	public Database getDatabase() {
		return database;
	}

	public WebApp getWebApp() {
		return application;
	}

	public UserSessions getUserSessions() {
		return application.getSessions();
	}

	public User getUser(Request request) {
		return getUserSessions().getUserByRequest(getRequest());
	}
	
	public Settings getSettings(){
		return application.getSettings();
	}
	
	public WorkflowEngine getWorkflowEngine(){
		return application.getWorkflowEngine();
	}

}