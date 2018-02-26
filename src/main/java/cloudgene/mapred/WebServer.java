package cloudgene.mapred;

import genepi.db.Database;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.routing.VirtualHost;

import cloudgene.mapred.cron.CronJobScheduler;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Settings;

public class WebServer extends Component {

	private Database database;

	private Settings settings;

	private int port;

	private String keystore;

	private String password;

	private boolean useSSL = false;

	private String rootDirectory = "";

	private String pagesDirectory = "";

	private WorkflowEngine workflowEngine;

	private CronJobScheduler scheduler;
	
	private static final Log log = LogFactory.getLog(WebServer.class);

	@Override
	public synchronized void start() throws Exception {

		// ------------------
		// Add the connectors
		// ------------------
		getServers().add(Protocol.HTTP, port);
		getClients().add(Protocol.HTTP);
		
		if (useSSL) {

			File keystoreFile = new File(keystore);
			if (keystoreFile.exists()) {
				org.restlet.Server https = getServers()
						.add(Protocol.HTTPS, 443);
				https.getContext().getParameters()
						.add("keystorePath", keystoreFile.getAbsolutePath());
				https.getContext().getParameters()
						.add("keystorePassword", password);
				https.getContext().getParameters().add("keyPassword", password);
				https.getContext().getParameters()
						.add("headerBufferSize", "50000"); // #28573
				https.getContext().getParameters()
						.add("requestBufferSize", "50000"); // #28573*/
				https.getContext().getParameters().add("allowRenegotiate", "false");
				https.getContext().getParameters()
				.add("sslContextFactory",
					        "org.restlet.engine.ssl.DefaultSslContextFactory");
				https.getContext().getParameters()
				.add("enabledCipherSuites",
				             "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384"
				             + " TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"
				             + " TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"
				             + " TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"
				             + " TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256"
				             + " TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
				             + " TLS_RSA_WITH_AES_256_CBC_SHA"
				             + " TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA"
				             + " TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
			}

		}

		getClients().add(Protocol.FILE);
		getClients().add(Protocol.CLAP);

		VirtualHost host = new VirtualHost(getContext());

		WebApp webapp = new WebApp(rootDirectory, pagesDirectory);
		webapp.setSettings(settings);
		webapp.setDatabase(database);
		webapp.setWorkflowEngine(workflowEngine);
		webapp.reloadTemplates();
		host.attach(webapp);
		getHosts().add(host);

		log.info("Start CronJobScheduler...");
		scheduler = new CronJobScheduler(webapp);
		scheduler.start();

		super.start();
	}

	public void setPort(int port) {
		this.port = port;
	}

	
	@Override
	public synchronized void stop() throws Exception {
		scheduler.stop();
		super.stop();
	}
	
	public void setHttpsCertificate(String keystore, String password) {
		useSSL = true;
		this.keystore = keystore;
		this.password = password;
	}

	public void setRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public void setPagesDirectory(String pagesDirectory) {
		this.pagesDirectory = pagesDirectory;
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

	public WorkflowEngine getWorkflowEngine() {
		return workflowEngine;
	}

	public void setWorkflowEngine(WorkflowEngine workflowEngine) {
		this.workflowEngine = workflowEngine;
	}

}
