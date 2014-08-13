package cloudgene.mapred.util;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class MyRScript {

	private String filename;

	private StringBuffer content;

	public MyRScript(String filename) {
		this.filename = filename;
		this.content = new StringBuffer();
	}

	public void source(String filename) {

		content.append("source(\"" + filename + "\")\n");

	}

	public void append(String... lines) {
		for (String line : lines) {
			content.append(line + "\n");
		}

	}

	public void save() {

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
