package cloudgene.mapred;

import cloudgene.mapred.cli.CloneApplications;
import cloudgene.mapred.cli.InstallApplication;
import cloudgene.mapred.cli.InstallGitHubApplication;
import cloudgene.mapred.cli.ListApplications;
import cloudgene.mapred.cli.RemoveApplication;
import cloudgene.mapred.cli.ShowPlugins;
import cloudgene.mapred.cli.ShowVersion;
import cloudgene.mapred.cli.StartServer;
import cloudgene.mapred.cli.ValidateApplication;
import cloudgene.mapred.server.Application;
import genepi.base.Toolbox;

public class CommandLineInterface extends Toolbox {

	public CommandLineInterface(String command, String[] args) {
		super(command, args);
		printHeader();
	}

	private void printHeader() {
		System.out.println();
		System.out.println("Cloudgene " + Application.VERSION);
		System.out.println("http://www.cloudgene.io");
		System.out.println("(c) 2009-2024 Lukas Forer and Sebastian Schoenherr");
		System.out.println();
	}

	public static void main(String[] args) throws Exception {
		CommandLineInterface toolbox = new CommandLineInterface("cloudgene", args);
		toolbox.addTool("install", InstallApplication.class);
		toolbox.addTool("gh", InstallGitHubApplication.class);
		toolbox.addTool("github-install", InstallGitHubApplication.class);
		toolbox.addTool("clone", CloneApplications.class);
		toolbox.addTool("ls", ListApplications.class);
		toolbox.addTool("remove", RemoveApplication.class);
		toolbox.addTool("server", StartServer.class);
		toolbox.addTool("validate", ValidateApplication.class);
		toolbox.addTool("plugins", ShowPlugins.class);
		toolbox.addTool("version", ShowVersion.class);
		toolbox.start();

	}
}