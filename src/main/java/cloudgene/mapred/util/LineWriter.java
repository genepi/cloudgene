package cloudgene.mapred.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LineWriter {

	private BufferedWriter bw;

	private boolean first = true;

	public LineWriter(String filename) throws IOException {
		bw = new BufferedWriter(new FileWriter(new File(filename), false));
		first = true;
	}

	public void write(String line) throws IOException {
		if (first) {
			first = false;
		} else {
			bw.newLine();
		}

		bw.write(line);
	}

	public void close() throws IOException {
		bw.close();
	}

}
