package cloudgene.mapred.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.esotericsoftware.yamlbeans.YamlException;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.plugins.PluginManager;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterOutput;
import cloudgene.mapred.wdl.WdlReader;
import genepi.hadoop.HadoopCluster;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class RunApplication extends BaseTool {

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	private WdlApp app = null;

	private boolean output = false;

	private boolean logging = false;

	private boolean force = false;

	public RunApplication(String[] args) {
		super(args);
	}

	@Override
	public int run() {
		return 0;
	}

	// we override start instead of run, because we use our own cli parser based
	// on inputs defined in the yaml file
	@Override
	public int start() {

		// call init manualy
		init();

		if (args.length < 1) {
			printError("No filename of a cloudgene.yaml file or id of an application found.");
			System.out.println("cloudgene run <path-to-cloudgene.yaml>");
			System.out.println();
			return 1;
		}

		String filename = args[0];

		// check if file exists
		if (new File(filename).exists()) {
			// load wdl app from yaml file
			try {
				app = WdlReader.loadAppFromFile(filename);

			} catch (FileNotFoundException e1) {
				printError("File '" + filename + "' not found.");
				return 1;
			} catch (YamlException e) {
				printError("Syntax error in file '" + filename + "':");
				printError(e.getMessage());
				return 1;
			} catch (Exception e2) {
				printError("Error loading file '" + filename + "':");
				printError(e2.getMessage());
				return 1;
			}
		} else {
			// check if application name is installed
			Application application = repository.getById(filename);
			if (application == null) {
				printError("Application or file " + filename + " not found.");
				return 1;
			}

			if (application.hasSyntaxError()) {
				printError("Syntax error in file '" + application.getFilename() + "':");
				printError(application.getErrorMessage());
				return 1;
			}

			app = application.getWdlApp();

		}

		// print application details
		System.out.println();
		System.out.println(app.getName() + " " + app.getVersion());
		if (app.getWebsite() != null && !app.getWebsite().isEmpty()) {
			System.out.println(app.getWebsite());
		}
		if (app.getAuthor() != null && !app.getAuthor().isEmpty()) {
			System.out.println(app.getAuthor());
		}
		System.out.println();

		if (app.getWorkflow() == null || app.getWorkflow().getSteps() == null
				|| app.getWorkflow().getSteps().size() == 0) {
			printError("Application has no workflow. It seems that " + app.getName() + " is a data package.");
			return 1;
		}

		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create options for each input param in yaml file
		Options options = CommandLineUtil.createOptionsFromApp(app);

		// add general options: run on docker
		Option loggingOption = new Option(null, "show-log", false, "Stream logging messages to stdout");
		loggingOption.setRequired(false);
		options.addOption(loggingOption);

		// add general options: run on docker
		Option noOutputOption = new Option(null, "show-output", false, "Stream output to stdout");
		noOutputOption.setRequired(false);
		options.addOption(noOutputOption);

		// add general options: run on docker
		Option forceOption = new Option(null, "force", false,
				"Force Cloudgene to reinstall application in HDFS even if it already installed.");
		forceOption.setRequired(false);
		options.addOption(forceOption);

		// add general options: hadoop configuration
		Option confOption = new Option(null, "conf", true, "Hadoop configuration folder");
		confOption.setRequired(false);
		options.addOption(confOption);

		// add general options: output folder
		Option outputFolderOption = new Option(null, "output", true, "Output folder");
		outputFolderOption.setRequired(false);
		options.addOption(outputFolderOption);

		// add general options: hadoop user
		Option usernameOption = new Option(null, "user", true,
				"Hadoop username [default: " + DEFAULT_HADOOP_USER + "]");
		usernameOption.setRequired(false);
		options.addOption(usernameOption);

		// parse the command line arguments
		CommandLine line = null;
		try {

			line = parser.parse(options, args);

		} catch (Exception e) {
			printError(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("input parameters:", options);
			System.out.println();
			return 1;
		}

		if (line.hasOption("show-log")) {
			logging = true;
		}

		if (line.hasOption("show-output")) {
			output = true;
		}

		if (line.hasOption("force")) {
			force = true;
		}

		if (line.hasOption("conf")) {

			String conf = line.getOptionValue("conf");
			String username = line.getOptionValue("user", null);
			printText(0, spaces("[INFO]", 8) + "Use Haddop configuration folder " + conf
					+ (username != null ? " with username " + username : ""));
			HadoopCluster.setConfPath("Unknown", conf, username);

		} else {
			if (settings.getCluster() == null) {
				//printText(0, spaces("[INFO]", 8) + "No external Haddop cluster set.");
			}
		}

		// show supported plugins
		PluginManager manager = PluginManager.getInstance();
		manager.initPlugins(settings);

		/*for (IPlugin plugin : manager.getPlugins()) {
			if (manager.isEnabled(plugin)) {
				printText(0, spaces("[INFO]", 8) + plugin.getStatus());
			} else {
				printText(0, spaces("[WARN]", 8) + plugin.getStatus());
			}
		}*/

		// create directories
		FileUtil.createDirectory(settings.getTempPath());

		// start workflow engine
		WorkflowEngine engine = new WorkflowEngine(1, 1);
		new Thread(engine).start();

		// init job

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String id = "job-" + sdf.format(new Date());
		String hdfs = "";
		try {
			hdfs = HdfsUtil.path("cloudgene-cli", id);
		} catch (NoClassDefFoundError e) {

		}
		String local = FileUtil.path(id);

		if (line.hasOption("output")) {
			local = line.getOptionValue("output");
		}

		FileUtil.createDirectory(local);

		// file params with values from cmdline
		try {
			Map<String, String> params = CommandLineUtil.createParams(app, line, local, hdfs);

			// dummy user
			User user = new User();
			user.setUsername("local");
			user.setPassword("local");
			user.makeAdmin();

			CloudgeneJob job = new CloudgeneJob(user, id, app, params) {

				private int count = 0;
				
				@Override
				public boolean afterSubmission() {
					boolean result = super.afterSubmission();
					if (result) {
						printParameters(this, app);
					}
					return result;
				}
				
				@Override
				public void onStepStarted(CloudgeneStep step) {
					super.onStepStarted(step);
					System.out.println();
					printText(0, spaces("[INFO]", 8) + step.getName() + "...");					
				}

				@Override
				public void onStepFinished(CloudgeneStep step) {
					super.onStepFinished(step);
					int c = 0;
					for (Message message : step.getLogMessages()) {
						String text = message.getMessage().replaceAll("<br>", "\n").replaceAll("\n",
								"\n" + spaces(4 + 8));
						String type = getDescription(message.getType());
						type = spaces("[" + type + "]", 8);
						if (message.getType() == Message.OK) {
							type = makeGreen(type);
						} else if (message.getType() == Message.ERROR) {
							type = makeRed(type);
						}

						printText(0, spaces(type, 8) + text);
						c++;
						if (c < step.getLogMessages().size()) {
							printSingleLine(4);
						}
					}
				}

				@Override
				public void writeOutputln(String line) {
					super.writeOutputln(line);
					if (output) {
						printText(0, spaces("[OUT]", 8) + line);
					}
				}

				@Override
				public void writeLog(String line) {
					super.writeLog(line);
					if (logging) {
						printText(0, spaces("[LOG]", 8) + line);
					}
				}

			};
			job.setId(id);
			job.setName(id);
			job.setLocalWorkspace(local);
			job.setHdfsWorkspace(hdfs);
			job.setSettings(settings);
			job.setRemoveHdfsWorkspace(true);
			job.setApplication(app.getName() + " " + app.getVersion());
			job.setApplicationId(app.getId());
			job.forceInstallation(force);

			if (force) {
				printText(0, spaces("[INFO]", 8) + "Force Cloudgene to reinstall application in HDFS");
			}

			printText(0, spaces("[INFO]", 8) + "Submit job " + id + "...");

			// submit job
			engine.submit(job);

			// wait until job is complete.
			while (!job.isComplete()) {
				Thread.sleep(1000);
			}
			long wallTime = (job.getFinishedOn() - job.getSubmittedOn()) / 1000;
			long setupTime = (job.getSetupEndTime() - job.getSetupStartTime()) / 1000;
			long pipelineTime = (job.getFinishedOn() - job.getSubmittedOn()) / 1000;
			// print steps and feedback
			System.out.println();

			if (job.getState() == CloudgeneJob.STATE_SUCCESS) {

				printlnInGreen("Done! Executed without errors.");
				System.out.println("Results can be found in file://" + (new File(local)).getAbsolutePath());
				System.out.println();
				if (logging) {
					printText(0, spaces("[LOG]", 8) + "Wall-Time: " + wallTime + " sec [Setup: " + setupTime
							+ " sec, Pipeline: " + pipelineTime + " sec]");
				}
				System.out.println();
				return 0;

			} else {
				printlnInRed("Error: Execution failed.");
				System.out.println();
				System.out.println();
				if (logging) {
					printText(0, spaces("[LOG]", 8) + "Wall-Time: " + wallTime + " [Setup: " + setupTime
							+ ", Pipeline: " + pipelineTime);
					System.out.println();
				}

				System.out.println();

				return 1;
			}
		}catch (FileNotFoundException e) {
			printlnInRed("Error: " + e.getMessage());			
			System.out.println();
			System.out.println();
			return 1;
			
		} catch (Exception e) {
			printlnInRed("Error: Execution failed.");
			System.out.println("Details:");
			e.printStackTrace();
			System.out.println();
			System.out.println();
			return 1;
		}

	}

	public void printParameters(CloudgeneJob job, WdlApp app) {
		if (app.getWorkflow().getInputs().size() > 0) {
			printText(2 + 6, "Input values: ");
			for (WdlParameterInput input : app.getWorkflow().getInputs()) {
				if (!input.getType().equals("agbcheckbox") && !input.getType().equals("terms_checkbox")
						&& !input.isAdminOnly() && input.isVisible()) {
					printText(4 + 6, input.getId() + ": " + job.getContext().get(input.getId()));
				}
			}
		}

		if (app.getWorkflow().getOutputs().size() > 0) {
			printText(2 + 6, "Results:");
			for (WdlParameterOutput output : app.getWorkflow().getOutputs()) {
				if (output.isDownload() && !output.isAdminOnly()) {
					printText(4 + 6, output.getDescription() + ": " + job.getContext().get(output.getId()));
				}
			}
		}
	};

	public String getDescription(int type) {
		switch (type) {
		case Message.ERROR:
			return "ERROR";
		case Message.OK:
			return "OK";
		case Message.WARNING:
			return "WARN";
		case Message.RUNNING:
			return "RUN";
		default:
			return "??";
		}
	}

	@Override
	public void createParameters() {

	}

}
