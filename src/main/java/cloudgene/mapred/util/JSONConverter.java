package cloudgene.mapred.util;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import cloudgene.mapred.jobs.AbstractJob;

public class JSONConverter {

	public static JSONObject fromJob(AbstractJob job) {

		JsonConfig config = new JsonConfig();
		config.setExcludes(new String[] { "user", "inputParams", "output", "error", "s3Url", "task", "config",
				"mapReduceJob", "job", "step", "context", "hdfsWorkspace", "localWorkspace", "logOutFiles",
				"removeHdfsWorkspace", "settings", "setupComplete", "stdOutFile", "workingDirectory", "parameter",
				"logOutFile", "map", "reduce", "mapProgress", "reduceProgress", "jobId", "makeAbsolute", "mergeOutput",
				"removeHeader", "value", "autoExport", "adminOnly", "download", "tip", "apiToken", "parameterId",
				"count", "username" });
		return JSONObject.fromObject(job, config);
	}

}
