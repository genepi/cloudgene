package cloudgene.mapred.util.rscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Command {

	private String cmd;

	private String[] params;

	private boolean silent = false;

	private boolean deleteInput = false;

	private String directory = null;

	private StringBuffer stout = new StringBuffer();

	public Command(String cmd, String... params) {
		this.cmd = cmd;
		this.params = params;
	}

	public Command(String cmd) {
		this.cmd = cmd;
	}

	public void setParams(String... params) {
		this.params = params;
	}

	public void setParams(List<String> params) {
		this.params = new String[params.size()];
		for (int i = 0; i < params.size(); i++) {
			this.params[i] = params.get(i);
		}
	}

	public int execute() {

		List<String> command = new ArrayList<String>();

		command.add(cmd);
		System.out.println(cmd);
		if (params != null) {
			for (String param : params) {
				command.add(param);
				System.out.println(param);
			}
		}

		try {

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			if (directory != null) {
				builder.directory(new File(directory));
			}
			Process process = builder.start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				stout.append(line + "\n");
				if (!silent) {
					System.out.println(line);
				}
			}
			br.close();
			isr.close();
			is.close();
			process.waitFor();
			if (process.exitValue() != 0) {
				return process.exitValue();
			} else {
				process.destroy();
			}

			if (deleteInput) {
				new File(cmd).delete();
			}

			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public boolean isDeleteInput() {
		return deleteInput;
	}

	public void setDeleteInput(boolean deleteInput) {
		this.deleteInput = deleteInput;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getDirectory() {
		return directory;
	}

	@Override
	public String toString() {
		String result = cmd;
		if (params != null) {
			for (String param : params) {
				result += " " + param;
			}
		}
		return result;
	}

	public String getStdOut() {
		return stout.toString();
	}

}
