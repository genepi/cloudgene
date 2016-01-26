package cloudgene.mapred.api.v2;

import junit.framework.TestCase;

import org.restlet.resource.ClientResource;

import cloudgene.mapred.util.TestEnvironment;

public class ApiTestCase extends TestCase {

	public ClientResource createClientResource(String path) {
		return new ClientResource(TestEnvironment.HOSTNAME + path);
	}

}
