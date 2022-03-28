package cloudgene.mapred.api.v2.jobs;

import java.util.List;

import cloudgene.mapred.Application;
import cloudgene.mapred.auth.AuthenticationService;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.ParameterDao;
import cloudgene.mapred.exceptions.JsonHttpStatusException;
import cloudgene.mapred.jobs.CloudgeneParameterOutput;
import cloudgene.mapred.jobs.Download;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

@Controller
public class DownloadScriptResults {

	@Inject
	protected Application application;

	@Inject
	protected AuthenticationService authenticationService;

	@Get("/get/{paramId}/{hash}")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String downloadScript(String paramId, String hash) {

		int id = -1;
		try {
			id = Integer.parseInt(paramId);
		} catch (NumberFormatException e) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "Parameter ID is not numeric.");
		}

		ParameterDao parameterDao = new ParameterDao(application.getDatabase());
		CloudgeneParameterOutput param = parameterDao.findById(id);

		if (param == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Param " + param + " not found.");
		}

		if (!hash.equals(param.createHash())) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "Download forbidden.");
		}

		DownloadDao dao = new DownloadDao(application.getDatabase());
		List<Download> downloads = dao.findAllByParameter(param);

		String hostname = application.getSettings().getHostname();

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
		return script.toString();

	}

}
