package cloudgene.mapred.steps;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.importer.IImporter;
import genepi.hadoop.importer.ImporterFactory;
import cloudgene.mapred.jobs.CloudgeneContext;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.util.Technology;
import cloudgene.mapred.wdl.WdlStep;

public class HdfsImporterStep extends CloudgeneStep {

	@Override
	public boolean run(WdlStep step, CloudgeneContext context) {

		try {

			for (String input : context.getInputs()) {

				if (ImporterFactory.needsImport(context.get(input))) {

					String[] urlList = context.get(input).split(";")[0].split("\\s+");

					String username = "";
					if (context.get(input).split(";").length > 1) {
						username = context.get(input).split(";")[1];
					}

					String password = "";
					if (context.get(input).split(";").length > 2) {
						password = context.get(input).split(";")[2];
					}

					for (String url2 : urlList) {

						String url = url2 + ";" + username + ";" + password;

						String target = HdfsUtil.path(((CloudgeneContext) context).getHdfsTemp(), "importer", input);

						try {

							context.beginTask("Import File(s) " + url2 + "...");

							IImporter importer = ImporterFactory.createImporter(url, target);

							if (importer != null) {

								boolean successful = importer.importFiles();

								if (successful) {

									context.setInput(input, target);

									context.endTask("Import File(s) " + url2 + " successful.", Message.OK);

								} else {

									context.endTask("Import File(s) " + url2 + " failed: " + importer.getErrorMessage(),
											Message.ERROR);

									return false;

								}

							} else {

								context.endTask("Import File(s) " + url2 + " failed: Protocol not supported",
										Message.ERROR);

								return false;

							}

						} catch (Exception e) {
							context.endTask("Import File(s) " + url2 + " failed: " + e.toString(), Message.ERROR);
							return false;
						}

					}

				}
			}
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public Technology[] getRequirements() {
		return new Technology[] { Technology.HADOOP_CLUSTER };
	}

}
