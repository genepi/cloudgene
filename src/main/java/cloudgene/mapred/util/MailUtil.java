package cloudgene.mapred.util;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.apache.commons.logging.LogFactory;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailUtil {

	private static final org.apache.commons.logging.Log log = LogFactory.getLog(MailUtil.class);

	public static void notifyAdmin(Settings settings, String subject, String text) throws Exception {

		if (settings.getAdminMail() != null && !settings.getAdminMail().isEmpty()) {

			send(settings.getMail().get("smtp"), settings.getMail().get("port"), settings.getMail().get("user"),
					settings.getMail().get("password"), settings.getMail().get("name"), settings.getAdminMail(),
					subject, text);
		}

	}

	public static void notifySlack(Settings settings,
			String text){
		if (settings.getSlack() != null && !settings.getSlack().isEmpty()){
			SlackApi api = new SlackApi(settings.getSlack());
			SlackMessage message = new SlackMessage("Cloudgene", text);
			message.setIcon(":rocket:");
			api.call(message);
			log.debug("Sent slack notification");
		}
	}

	public static void send(Settings settings, String tos, String subject, String text) throws Exception {

		send(settings.getMail().get("smtp"), settings.getMail().get("port"), settings.getMail().get("user"),
				settings.getMail().get("password"), settings.getMail().get("name"), tos, subject, text);

	}

	public static void send(final String smtp, final String port, final String username, final String password,
			final String name, String tos, String subject, String text) throws Exception {

		Properties props = new Properties();

		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.port", port);

		props.put("mail.smtp.host", smtp);
		props.put("mail.smtp.port", port);

		Session session = null;
		if (username != null && !username.isEmpty()) {

			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");

			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		} else {

			session = Session.getInstance(props);

		}

		if (session != null) {
			log.info("Connect SMTP server using: " + username);
		} else {
			log.error("Cannot find SMTP server at port.");
		}

		try {

			InternetAddress[] addresses = InternetAddress.parse(tos);

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(name));
			message.setRecipients(Message.RecipientType.TO, addresses);
			message.setSubject(subject);
			message.setText(text);

			log.info("E-Mail sending from: " + name + " to " + tos + ".");
			log.info("E-Mail subject: " + subject);

			Transport.send(message);

		} catch (AuthenticationFailedException e) {

			log.error(e);
			throw new Exception("admin SMTP server auth failed.");

		} catch (MessagingException e) {

			log.error(e);
			throw new Exception("mail could not be sent.");
		}
	}
}
