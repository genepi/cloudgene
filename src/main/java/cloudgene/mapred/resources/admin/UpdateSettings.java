package cloudgene.mapred.resources.admin;

import genepi.io.FileUtil;

import java.io.FileWriter;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.core.User;
import cloudgene.mapred.core.UserSessions;
import cloudgene.mapred.util.Settings;

import com.esotericsoftware.yamlbeans.YamlWriter;

public class UpdateSettings extends ServerResource {

	@Post
	public Representation post(Representation entity) {

		UserSessions sessions = UserSessions.getInstance();
		User user = sessions.getUserByRequest(getRequest());
		if (user == null) {

			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires user authentication.");

		}

		if (!user.isAdmin()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return new StringRepresentation(
					"The request requires administration rights.");
		}

		Form form = new Form(entity);
		String name = form.getFirstValue("name");
		String hadoopPath = form.getFirstValue("hadoopPath");
		String userApp = form.getFirstValue("userApp");
		String adminApp = form.getFirstValue("adminApp");
		String mailSmtp = form.getFirstValue("mail-smtp");
		String mailPort = form.getFirstValue("mail-port");
		String mailUser = form.getFirstValue("mail-user");
		String mailPassword = form.getFirstValue("mail-password");
		String mailName = form.getFirstValue("mail-name");

		Settings settings = Settings.getInstance();

		settings.setHadoopPath(hadoopPath);
		settings.setName(name);
		settings.getApps().put("user", userApp);
		settings.getApps().put("admin", adminApp);
		settings.getMail().put("smtp", mailSmtp);
		settings.getMail().put("port", mailPort);
		settings.getMail().put("user", mailUser);
		settings.getMail().put("password", mailPassword);
		settings.getMail().put("name", mailName);

		try {

			FileUtil.createDirectory("config");

			YamlWriter writer = new YamlWriter(new FileWriter(FileUtil.path(
					"config", "settings.yaml")));
			writer.write(settings);
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new StringRepresentation("OK.");
	}

}
