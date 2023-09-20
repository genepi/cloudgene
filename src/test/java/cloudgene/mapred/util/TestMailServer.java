package cloudgene.mapred.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

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
		Iterator<SmtpMessage> iterator = (Iterator<SmtpMessage>) getReceivedEmail();
		List<SmtpMessage> list = new ArrayList<SmtpMessage>();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}
		return list;
	}

}