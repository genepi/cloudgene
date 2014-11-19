package cloudgene.mapred.resources.admin;

import genepi.io.FileUtil;

import java.io.FileWriter;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import cloudgene.mapred.core.User;
import cloudgene.mapred.util.BaseResource;

import com.esotericsoftware.yamlbeans.YamlWriter;

public class UpdateSettings extends BaseResource {

	@Post
	public Representation post(Representation entity) {

		User user = getUser(getRequest());

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

		getSettings().setHadoopPath(hadoopPath);
		getSettings().setName(name);
		getSettings().getApps().put("user", userApp);
		getSettings().getApps().put("admin", adminApp);
		getSettings().getMail().put("smtp", mailSmtp);
		getSettings().getMail().put("port", mailPort);
		getSettings().getMail().put("user", mailUser);
		getSettings().getMail().put("password", mailPassword);
		getSettings().getMail().put("name", mailName);

		try {

			FileUtil.createDirectory("config");

			YamlWriter writer = new YamlWriter(new FileWriter(FileUtil.path(
					"config", "settings.yaml")));
			writer.write(getSettings());
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new StringRepresentation("OK.");
	}

}
