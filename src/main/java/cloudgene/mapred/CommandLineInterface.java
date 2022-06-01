package cloudgene.mapred;

import cloudgene.mapred.cli.*;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.BuildUtil;
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
		System.out.println("(c) 2009-2022 Lukas Forer and Sebastian Schoenherr");
		System.out.println(BuildUtil.getBuildInfos());
		System.out.println();
	}

	public static void main(String[] args) throws Exception {
		CommandLineInterface toolbox = new CommandLineInterface("cloudgene", args);
		toolbox.addTool("run", RunApplication.class);
		toolbox.addTool("install", InstallApplication.class);
		toolbox.addTool("gh", InstallGitHubApplication.class);
		toolbox.addTool("github-install", InstallGitHubApplication.class);
		toolbox.addTool("clone", CloneApplications.class);
		toolbox.addTool("ls", ListApplications.class);
		toolbox.addTool("remove", RemoveApplication.class);
		toolbox.addTool("server", StartServer.class);
		toolbox.addTool("validate", ValidateApplication.class);
		toolbox.addTool("verify-cluster", VerifyCluster.class);
		toolbox.addTool("plugins", ShowPlugins.class);
		toolbox.addTool("version", ShowVersion.class);
		toolbox.start();

	}
}