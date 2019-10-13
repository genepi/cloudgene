package cloudgene.mapred.api.v2.jobs;

import java.util.List;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;

import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.ParameterDao;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import cloudgene.mapred.util.BaseResource;

public class DownloadScriptResults extends BaseResource {

	@Get
	public Representation get() {

		String paramId = getAttribute("id");
		String hash = getAttribute("hash");

		int id = -1;
		try {
			id = Integer.parseInt(paramId);
		} catch (NumberFormatException e) {
			return error(Status.CLIENT_ERROR_BAD_REQUEST, "Parameter ID is not numeric.");
		}

		ParameterDao parameterDao = new ParameterDao(getDatabase());
		CloudgeneParameterOutput param = parameterDao.findById(id);

		if (param == null) {
			return error404("Param " + param + " not found.");
		}

		if (!hash.equals(param.createHash())) {
			return error403("Download forbidden.");
		}

		DownloadDao dao = new DownloadDao(getDatabase());
		List<Download> downloads = dao.findAllByParameter(param);

		String hostname = "";
		if (getRequest().getReferrerRef() != null) {
			hostname = getRequest().getReferrerRef().getHostIdentifier();
		} else {
			hostname = getRequest().getHostRef().getHostIdentifier();
		}

		StringBuffer script = new StringBuffer();
		script.append("#!/bin/bash\n");
		script.append("set -e\n");
		script.append("GREEN='\033[0;32m'\n");
		script.append("NC='\033[0m'\n");
		int i = 1;
		for (Download download : downloads) {
			script.append("echo \"\"\n");
			script.append(
					"echo \"Downloading file " + download.getName() + " (" + i + "/" + downloads.size() + ")...\"\n");
			script.append("curl -L " + hostname + "/share/results/" + download.getHash() + "/" + download.getName()
					+ " -o " + download.getName() + " --create-dirs \n");
			i++;
		}
		script.append("echo \"\"\n");
		script.append("echo -e \"${GREEN}All " + downloads.size() + " file(s) downloaded.${NC}\"\n");
		script.append("echo \"\"\n");
		script.append("echo \"\"\n");
		return new StringRepresentation(script.toString());

	}

}
