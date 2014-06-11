package cloudgene.mapred.util;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LineReader {

	private String filename;

	private BufferedReader in;

	private String line;

	public LineReader(String filename) {
		this.filename = filename;
		try {
			in = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean next() throws IOException {
		line = in.readLine();
		return line != null;
	}

	public void close() {
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String get() {
		return line;
	}

	public String getFilename() {
		return filename;
	}

}
