package cloudgene.mapred.util.rscript;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class RScriptUtil {

	public static void createRScript(String filename, StringBuffer content) {

		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(content.toString());
			out.close();
			fstream.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

}
