package cloudgene.mapred.util.junit;

import java.util.Iterator;
import java.util.List;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.google.common.collect.Lists;

/**
 * A JUnit Rule which runs a mock SMTP server
 *
 * @see org.junit.rules.TestRule
 */
public class TestMailServer {
	public final static int PORT = 9985;

	private SimpleSmtpServer smtp;

	private static TestMailServer instance;

	/**
	 * Creates a SMTP server listening on port 25.
	 */
	private TestMailServer() {
	}

	public static TestMailServer getInstance() {
		if (instance == null) {
			instance = new TestMailServer();
		}
		return instance;
	}

	public void start() {
		if (smtp == null) {
			smtp = SimpleSmtpServer.start(PORT);
		}
	}

	public synchronized int getReceivedEmailSize() {
		return smtp.getReceivedEmailSize();
	}

	public synchronized Iterator getReceivedEmail() {
		return smtp.getReceivedEmail();
	}

	public List<SmtpMessage> getReceivedEmailAsList() {
		return Lists.newArrayList((Iterator<SmtpMessage>) getReceivedEmail());
	}

}