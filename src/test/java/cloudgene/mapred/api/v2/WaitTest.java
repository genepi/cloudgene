package cloudgene.mapred.api.v2;

import java.io.IOException;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.ext.html.FormDataSet;
import org.restlet.resource.ClientResource;

import cloudgene.mapred.util.junit.TestServer;

public class WaitTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		TestServer.getInstance().start();

	}
	public void testWait() throws InterruptedException {

		for (int i = 0; i < 200; i++) {
			final int j = i;
			new Thread(new Runnable() {

				@Override
				public void run() {
					ClientResource resource = createClientResource("/wait/"+j);
					resource.get();

				}
			}).start();

		}
		
		Thread.sleep(1000000000);

	}

	public ClientResource createClientResource(String path) {
		return new ClientResource(TestServer.HOSTNAME + path);
	}

}
