package cloudgene.mapred.util;

import java.io.File;
import java.io.IOException;

import cloudgene.mapred.util.rscript.MyRScript;
import cloudgene.mapred.util.rscript.RScript;

public class RMarkdown {

	public static int convert(String rdmScript, String outputHtml, String[] args) {

		System.out.println("Creating RMarkdown report from " + rdmScript
				+ "...");

		String name = rdmScript.replace(".Rmd", "");

		outputHtml = new File(outputHtml).getAbsolutePath();

		String folder = new File(outputHtml).getParentFile().getAbsolutePath()
				+ "/figures-temp/";

		MyRScript script = new MyRScript("convert.R");
		script.append("library(knitr)");
		script.append("opts_chunk$set(fig.path='" + folder + "')");
		script.append("library(markdown)");
		script.append("knit(\"" + rdmScript + "\", \"" + outputHtml + ".md\")");
		script.append("markdownToHTML(\"" + outputHtml + ".md\", \""
				+ outputHtml + "\")");
		script.save();

		RScript rScript = new RScript();
		rScript.setSilent(false);

		String[] argsForScript = new String[args.length + 2];
		argsForScript[0] = "convert.R";
		argsForScript[1] = "--args";
		for (int i = 0; i < args.length; i++) {

			// checkout hdfs file
			if (args[i].startsWith("hdfs://")) {

				String localFile = new File(outputHtml).getParentFile()
						.getAbsolutePath() + "/local_file_" + i;
				try {
					HdfsUtil.checkOut(args[i], localFile);
					argsForScript[i + 2] = localFile;
				} catch (IOException e) {
					e.printStackTrace();
					argsForScript[i + 2] = args[i];
				}

			} else {

				argsForScript[i + 2] = args[i];

			}
		}

		rScript.setParams(argsForScript);
		int result = rScript.execute();

		new File(outputHtml + ".md").delete();
		new File("convert.R").delete();
		RMarkdown.deleteFolder(new File(folder));

		return result;

	}

	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}
}
