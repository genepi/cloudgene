package cloudgene.mapred.steps;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.FileUtil;
import cloudgene.mapred.util.HdfsUtil;
import cloudgene.mapred.wdl.WdlStep;

public class Command extends Hadoop {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		String[] params = step.getExec().split(" ");

		List<String> command = new Vector<String>();
		for (String param : params) {

			// checkout hdfs file
			if (param.startsWith("hdfs://")) {
				String name = FileUtil.getFilename(param);
				String localFile = FileUtil.path(context.getLocalTemp(),
						"local_" + name);
				try {
					HdfsUtil.checkOut(param, localFile);
					command.add(new File(localFile).getAbsolutePath());
				} catch (IOException e) {
					context.println(e.getMessage());
					command.add(param);
				}

			} else {

				command.add(param);

			}

		}

		try {
			beginTask("Running Command...");
			boolean successful = executeCommand(command, context);
			if (successful) {
				endTask("Execution successful.", Message.OK);
				return true;
			} else {
				endTask("Execution failed. Please have a look at the logfile for details.",
						Message.ERROR);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
