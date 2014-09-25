package cloudgene.mapred;

import java.io.File;

import org.restlet.Component;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.routing.VirtualHost;

public class WebServer extends Component {

	public WebServer(String webRoot, String webRoot2, int port,
			boolean useSSL, String keystore, String password) throws Exception {

		// ------------------
		// Add the connectors
		// ------------------
		getServers().add(Protocol.HTTP, port);

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
			}

		}

		getClients().add(Protocol.FILE);
		getClients().add(Protocol.CLAP);

		VirtualHost host = new VirtualHost(getContext());
		host.attach(new WebApp(webRoot, webRoot2));
		getHosts().add(host);

	}
}
