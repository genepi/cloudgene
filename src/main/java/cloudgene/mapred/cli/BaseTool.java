package cloudgene.mapred.cli;

import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import com.esotericsoftware.yamlbeans.YamlReader;
import genepi.base.Tool;

import java.io.File;
import java.io.FileReader;

public abstract class BaseTool extends Tool {

	private int MAX_LENGTH = 80;

	protected Settings settings;

	protected Config config;

	protected String[] args;

	protected ApplicationRepository repository;

	public BaseTool(String[] args) {
		super(args);
		this.args = args;
	}

	@Override
	public void init() {
				
		try {
			turnLoggingOff();
			// turnLoggingOn();

			// load cloudgene.conf file. contains path to settings, db, apps, ..
			config = new Config();
			if (new File(Config.CONFIG_FILENAME).exists()) {
				try {
					YamlReader reader = new YamlReader(new FileReader(Config.CONFIG_FILENAME));
					config = reader.read(Config.class);
				} catch (Exception e) {
					printError("Error loading cloudgene.conf file:");
					printError(e.getMessage());
					return;
				}
			}

			// load default settings when not yet loaded
			String settingsFilename = config.getSettings();
			settings = null;
			if (settingsFilename != null && new File(settingsFilename).exists()) {
				try {
					settings = Settings.load(config);
				} catch (Exception e) {
					printError("Error loading settings file '" + settingsFilename + "' :");
					printError(e.getMessage());
					return;
				}
			} else {
				settings = new Settings(config);
			}

			repository = settings.getApplicationRepository();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Settings getSettings() {
		return settings;
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

	public void printlnInRed(String text) {
		System.out.println(makeRed(text));
	}

	public String makeRed(String text) {
		return ((char) 27 + "[31m" + text + (char) 27 + "[0m");
	}

	public void printlnInGreen(String text) {
		System.out.println(makeGreen(text));
	}

	public String makeGreen(String text) {
		return ((char) 27 + "[32m" + text + (char) 27 + "[0m");
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

		// remove html tags
		String cleanText = text.replaceAll("\\<[^>]*>", "");
		System.out.println(spaces(paddingLeft) + cleanText);

	}

	public void printError(String error) {
		System.out.println();
		System.out.println("ERROR: " + error);
		System.out.println();
	}

	public void turnLoggingOff() {
		
	}

}
