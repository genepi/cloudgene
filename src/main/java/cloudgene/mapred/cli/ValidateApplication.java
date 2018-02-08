package cloudgene.mapred.cli;

import java.io.File;
import java.io.FileNotFoundException;

import com.esotericsoftware.yamlbeans.YamlException;

import cloudgene.mapred.util.Application;
import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlReader;

public class ValidateApplication extends BaseTool {

	private WdlApp app = null;

	public ValidateApplication(String[] args) {
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
			Application application = settings.getApp(filename);
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
		if (app.getAuthor() != null && !app.getAuthor().isEmpty()) {
			System.out.println(app.getVersion());
		}
		if (app.getWebsite() != null && !app.getWebsite().isEmpty()) {
			System.out.println(app.getWebsite());
		}
		System.out.println();
		printlnInGreen("[OK] Application has no syntax errors.\n");

		return 0;
	}

	@Override
	public void createParameters() {

	}

}