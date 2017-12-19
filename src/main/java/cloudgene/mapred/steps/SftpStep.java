package cloudgene.mapred.steps;

import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.hadoop.importer.IImporter;
import genepi.hadoop.importer.ImporterFactory;
import genepi.io.FileUtil;

public class SftpStep extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		try {
			Thread.sleep(5000);
			return importVcfFiles(context);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private boolean importVcfFiles(WorkflowContext context) {
		try {
			for (String input : context.getInputs()) {

				if (ImporterFactory.needsImport(context.get(input))) {

					context.beginTask("Importing files...");

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
						String target = FileUtil.path(context.getLocalTemp(), "importer", input);
						FileUtil.createDirectory(target);
						context.log("Import to local workspace " + target + "...");

						try {

							context.updateTask("Import " + url2 + "...", WorkflowContext.RUNNING);

							IImporter importer = ImporterFactory.createImporter(url, target);

							if (importer != null) {

								boolean successful = importer.importFiles("vcf.gz");

								if (successful) {

									context.setInput(input, target);

								} else {

									context.updateTask("Import " + url2 + " failed: " + importer.getErrorMessage(),
											WorkflowContext.ERROR);

									return false;

								}

							} else {

								context.updateTask("Import " + url2 + " failed: Protocol not supported",
										WorkflowContext.ERROR);

								return false;

							}

						} catch (Exception e) {
							context.updateTask("Import File(s) " + url2 + " failed: " + e.toString(),
									WorkflowContext.ERROR);
							e.printStackTrace();

							return false;
						}

					}

					context.updateTask("File Import successful. ", WorkflowContext.OK);

				}

			}

			return true;
		} catch (Exception e) {
			// context.updateTask("Import File(s) " + url2 + " failed: " +
			// e.toString(),
			// WorkflowContext.ERROR);
			e.printStackTrace();

			return false;
		}

	}

}
