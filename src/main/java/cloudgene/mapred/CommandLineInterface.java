package cloudgene.mapred;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import cloudgene.mapred.Main;
import cloudgene.mapred.core.User;
import cloudgene.mapred.jobs.CloudgeneJob;
import cloudgene.mapred.jobs.CloudgeneStep;
import cloudgene.mapred.jobs.Message;
import cloudgene.mapred.jobs.WorkflowEngine;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameter;
import cloudgene.mapred.wdl.WdlReader;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class CommandLineInterface {

	private String[] args = new String[] {};

	// private String prefix = "lukas-cloudgene";

	private String cmd = "cloudgene";

	private int MAX_LENGTH = 80;

	private int n = 0;

	private WdlApp app = null;

	public void init(String[] args) {

		this.args = args;

		printHeader();

		turnOffLogging();

		if (args.length == 0) {
			System.out.println("Usage: " + cmd + " <filename> <inputs> <outputs>");
			System.out.println();
			System.exit(1);
		}

	}

	public void start() throws Exception {

		String filename = args[0];

		// load wdl app from yaml file
		try {
			System.out.println("Loading file " + filename + "...");
			app = WdlReader.loadAppFromFile(filename);

		} catch (Exception e1) {

			printError("File '" + filename + "' not found.");

		}

		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create options for each input param in yaml file
		Options options = CommandLineUtil.createOptionsFromApp(app);

		// parse the command line arguments
		CommandLine line = null;
		try {

			line = parser.parse(options, args);

		} catch (Exception e) {
			printError(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(cmd + " " + filename, options);
			System.out.println();
			System.exit(1);

		}

		// load config
		Settings settings = null;
		if (new File("config/settings.yaml").exists()) {
			settings = Settings.load("config/settings.yaml");
		} else {
			settings = new Settings();
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
					// print all parameters
					if (result) {
						printParameters(this, app);
					}
					return result;
				}

				@Override
				public void onStepStarted(CloudgeneStep step) {
					n++;
					printDoubleLine();
					System.out.println("[" + n + "] " + step.getName());
					printDoubleLine();
				}

				@Override
				public void onStepFinished(CloudgeneStep step) {
					int c = 0;
					for (Message message : step.getLogMessages()) {
						String text = message.getMessage().replaceAll("<br>", "\n").replaceAll("\n",
								"\n" + spaces(4 + 8));
						String type = getDescription(message.getType());
						printText(4, spaces("[" + type + "]", 8) + text);
						c++;
						if (c < step.getLogMessages().size()) {
							printSingleLine(4);
						}
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

			System.out.println("Submit job " + id + "..." + "\n\n");

			// print job tile
			printDoubleLine();
			System.out.println(app.getName() + " (Job " + id + ")");
			printSingleLine();

			// submit job
			engine.submit(job);

			// wait until job is complete. TODO: improve feedback!
			while (job.getState() != CloudgeneJob.STATE_SUCCESS && job.getState() != CloudgeneJob.STATE_CANCELED
					&& job.getState() != CloudgeneJob.STATE_FAILED) {
				Thread.sleep(1000);
			}

			// print steps and feedback
			// printSummary(job);
			printDoubleLine();
			System.out.println();

			if (job.getState() == CloudgeneJob.STATE_SUCCESS) {
				printlnInGreen("Done! Executed without errors.");
				System.out.println("Results can be found in " + (new File(local)).getAbsolutePath());
				System.out.println();
				System.out.println();
				System.exit(0);
			} else {
				System.out.println();
				printlnInRed("Error: Execution failed.");
				System.out.println("  Log: " + FileUtil.path(id, "job.txt"));
				System.out.println("  StdOut: " + FileUtil.path(id, "std.out"));
				System.out.println();
				System.out.println();
				System.exit(-1);
			}

		} catch (Exception e) {
			System.out.println();
			printlnInRed("Error: Execution failed.");
			System.out.println("Details:");
			e.printStackTrace();
			System.out.println("  Log: " + FileUtil.path(id, "job.txt"));
			System.out.println("  StdOut: " + FileUtil.path(id, "std.out"));
			System.out.println();
			System.out.println();

			System.exit(-1);

		}

	}

	public void printHeader() {
		System.out.println();
		System.out.println("Cloudgene " + Main.VERSION + " - CLI");
		System.out.println("(c) 2009-2017 Lukas Forer and Sebastian Schoenherr");

		URLClassLoader cl = (URLClassLoader) CommandLineInterface.class.getClassLoader();
		try {
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attr = manifest.getMainAttributes();
			String buildVesion = attr.getValue("Version");
			String buildTime = attr.getValue("Build-Time");
			String builtBy = attr.getValue("Built-By");
			System.out.println("Built by " + builtBy + " on " + buildTime);

		} catch (IOException E) {
			// handle
		}

		System.out.println();
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

	public String spaces(int n) {
		return spaces("", n);
	}

	public String spaces(String base, int n) {
		return chars(base, ' ', n);
	}

	public String chars(char c, int n) {
		return chars("", c, n);
	}

	public String chars(String base, char c, int n) {
		String result = base;
		for (int i = base.length(); i < n; i++) {
			result += c;
		}
		return result;
	}

	public void printlnInRed(String text){
		System.out.println((char)27 + "[31m" +text + (char)27 + "[0m");
	}
	
	public void printlnInGreen(String text){
		System.out.println((char)27 + "[32m" +text + (char)27 + "[0m");
	}
	
	public void printLine(int paddingLeft, char c) {
		printText(paddingLeft, chars(c, MAX_LENGTH - paddingLeft));
	}

	public void printLine(char c) {
		printLine(0, c);
	}

	public void printSingleLine() {
		printSingleLine(0);
	}

	public void printSingleLine(int paddingLeft) {
		printLine(paddingLeft, '-');
	}

	public void printDoubleLine(int paddingLeft) {
		printLine(paddingLeft, '=');
	}

	public void printDoubleLine() {
		printDoubleLine(0);
	}

	public void printText(int paddingLeft, String text) {
		System.out.println(spaces(paddingLeft) + text);
	}

	public void printError(String error) {
		System.out.println();
		System.out.println("ERROR: " + error);
		System.out.println();
	}

	public void turnOffLogging() {
		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
		loggers.add(LogManager.getRootLogger());
		for (Logger logger : loggers) {
			logger.setLevel(Level.OFF);
		}
	}

	public static void main(String[] args) throws Exception {

		CommandLineInterface main = new CommandLineInterface();
		main.init(args);
		main.start();
	}
}