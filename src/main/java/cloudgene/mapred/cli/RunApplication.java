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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.ClusterStatus;

import com.esotericsoftware.yamlbeans.YamlException;

import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Application;
import cloudgene.mapred.util.DockerHadoopCluster;
import cloudgene.mapred.util.HadoopCluster;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;
import genepi.hadoop.HadoopUtil;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class RunApplication extends BaseTool {

	public static final String DEFAULT_DOCKER_IMAGE = "seppinho/cdh5-hadoop-mrv1";

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	private WdlApp app = null;
	
	private boolean output = true;
	
	private boolean logging = true;

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
			Application application = settings.getApp(filename);
			if (application == null) {
				printError("Application or file " + filename + " not found.");
				return 1;
			}
			app = application.getWdlApp();
		}

		// print application details
		System.out.println();
		System.out.println(app.getName() + " " + app.getVersion());
		if (app.getAuthor() != null && !app.getAuthor().isEmpty()) {
			System.out.println(app.getVersion());
		}
		if (app.getWebsite() != null && !app.getWebsite().isEmpty()) {
			System.out.println(app.getWebsite());
		}
		System.out.println();

		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create options for each input param in yaml file
		Options options = CommandLineUtil.createOptionsFromApp(app);

		// add general options: run on docker
		Option loggingOption = new Option(null, "no-logging", false, "Don’t stream logging messages to stdout");
		loggingOption.setRequired(false);
		options.addOption(loggingOption);
		
		// add general options: run on docker
		Option outputOption = new Option(null, "no-output", false, "Don’t stream output to stdout");
		outputOption.setRequired(false);
		options.addOption(outputOption);
		
		
		// add general options: run on docker
		Option dockerOption = new Option(null, "docker", false, "use docker hadoop cluster");
		dockerOption.setRequired(false);
		options.addOption(dockerOption);

		// add general options: run on custom docker image
		Option dockerImageOption = new Option(null, "image", true,
				"use custom docker image [default: " + DEFAULT_DOCKER_IMAGE + "]");
		dockerImageOption.setRequired(false);
		options.addOption(dockerImageOption);

		// add general options: hadoop hostname
		Option hostOption = new Option(null, "host", true, "Hadoop namenode hostname [default: localhost]");
		hostOption.setRequired(false);
		options.addOption(hostOption);

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

		DockerHadoopCluster cluster = null;

	 
		if (line.hasOption("no-logging")) {
			logging = false;
		}
		
		if (line.hasOption("no-output")) {
			output = false;
		}
		
		if (line.hasOption("host")) {

			String host = line.getOptionValue("host");
			String username = line.getOptionValue("user", DEFAULT_HADOOP_USER);
			System.out.println("Use external Haddop cluster running on " + host + " with username " + username);
			HadoopCluster.init(host, username);

		} else if (line.hasOption("docker")) {

			String image = line.getOptionValue("image", DEFAULT_DOCKER_IMAGE);

			cluster = new DockerHadoopCluster();
			try {
				cluster.start(image);
			} catch (Exception e) {
				printError("Error starting cluster.");
				printError(e.getMessage());
				return 1;
			}

			HadoopCluster.init(cluster.getIpAddress(), "cloudgene");

		} else {
			System.out.println("No external Haddop cluster set. Be sure cloudgene is running on your namenode");
		}

		// check cluster status
		ClusterStatus details = HadoopUtil.getInstance().getClusterDetails();
		if (details != null) {
			System.out.println("  TaskTrackers: " + details.getTaskTrackers());
		} else {
			System.out.println("Error: Hadoop cluster is unreachable.");
			System.out.println();
			return 1;
		}

		// create directories
		FileUtil.createDirectory(settings.getTempPath());

		// start workflow engine
		WorkflowEngine engine = new WorkflowEngine(1, 1);
		new Thread(engine).start();

		// init job

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String id = "job-" + sdf.format(new Date());
		String hdfs = HdfsUtil.path("cloudgene-cli", id);
		String local = FileUtil.path(id);
		FileUtil.createDirectory(local);

		// file params with values from cmdline
		Map<String, String> params = CommandLineUtil.createParams(app, line, local, hdfs);

		try {

			// dummy user
			User user = new User();
			user.setUsername("local");
			user.setPassword("local");
			user.setRole("admin");
			CloudgeneJob job = new CloudgeneJob(user, id, app.getWorkflow(), params) {
				@Override
				public boolean afterSubmission() {
					boolean result = super.afterSubmission();
					if (result) {
						printParameters(this, app);
					}
					return result;
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
					if (output){
						printText(0, spaces("[OUT]", 8) + line);
					}
				}

				@Override
				public void writeLog(String line) {
					super.writeLog(line);
					if (logging){
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

			printText(0, spaces("[INFO]", 8) + "Submit job " + id + "...");

			// submit job
			engine.submit(job);

			// wait until job is complete.
			while (job.isRunning()) {
				Thread.sleep(1000);
			}

			// print steps and feedback
			System.out.println();

			if (job.getState() == CloudgeneJob.STATE_SUCCESS) {

				if (cluster != null) {
					cluster.stop();
				}
				printlnInGreen("Done! Executed without errors.");
				System.out.println("Results can be found in file://" + (new File(local)).getAbsolutePath());
				System.out.println();
				System.out.println();
				return 0;

			} else {
				printlnInRed("Error: Execution failed.");
				System.out.println();
				System.out.println();
				if (cluster != null) {
					cluster.stop();
				}
				return 1;
			}

		} catch (Exception e) {
			printlnInRed("Error: Execution failed.");
			System.out.println("Details:");
			e.printStackTrace();
			System.out.println();
			System.out.println();
			if (cluster != null) {
				try {
					cluster.stop();
				} catch (Exception e1) {
					printlnInRed("Error: Stoping Cluster failed.");
					System.out.println(e.getMessage());
				}
			}
			return 1;
		}

	}

	public void printParameters(CloudgeneJob job, WdlApp app) {
		if (app.getWorkflow().getInputs().size() > 0) {
			printText(2, "Input values: ");
			for (WdlParameter input : app.getWorkflow().getInputs()) {
				if (!input.getType().equals("agbcheckbox") && !input.isAdminOnly() && input.isVisible()) {
					printText(4, input.getId() + ": " + job.getContext().get(input.getId()));
				}
			}
		}

		if (app.getWorkflow().getOutputs().size() > 0) {
			printText(2, "Results:");
			for (WdlParameter output : app.getWorkflow().getOutputs()) {
				if (output.isDownload() && !output.isAdminOnly()) {
					printText(4, output.getDescription() + ": " + job.getContext().get(output.getId()));
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