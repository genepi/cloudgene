package cloudgene.mapred;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import cloudgene.mapred.cli.InstallApplication;
import cloudgene.mapred.cli.ListApplications;
import cloudgene.mapred.cli.RemoveApplication;
import cloudgene.mapred.cli.RunApplication;
import cloudgene.mapred.cli.ShowVersion;
import cloudgene.mapred.cli.StartServer;
import genepi.base.Toolbox;

public class CommandLineInterface extends Toolbox {

	public CommandLineInterface(String command, String[] args) {
		super(command, args);
		printHeader();
	}

	private void printHeader() {
		System.out.println();
		System.out.println("Cloudgene " + Main.VERSION);
		System.out.println("http://cloudgene.uibk.ac.at");
		System.out.println("(c) 2009-2017 Lukas Forer and Sebastian Schoenherr");

		URLClassLoader cl = (URLClassLoader) InstallApplication.class.getClassLoader();
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

	public static void main(String[] args) throws Exception {
		CommandLineInterface toolbox = new CommandLineInterface("cloudgene", args);
		toolbox.addTool("run", RunApplication.class);
		toolbox.addTool("install", InstallApplication.class);
		toolbox.addTool("ls", ListApplications.class);
		toolbox.addTool("remove", RemoveApplication.class);
		toolbox.addTool("server", StartServer.class);
		toolbox.addTool("version", ShowVersion.class);
		toolbox.start();

	}
}