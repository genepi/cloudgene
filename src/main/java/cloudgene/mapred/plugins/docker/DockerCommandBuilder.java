package cloudgene.mapred.plugins.docker;

import java.util.List;
import java.util.Vector;

public class DockerCommandBuilder {

	private String image;

	private String[] cmd;

	private String[] volumes;

	public DockerCommandBuilder image(String image) {
		this.image = image;
		return this;
	}

	public DockerCommandBuilder binds(String[] volumes) {
		this.volumes = volumes;
		return this;
	}

	public DockerCommandBuilder command(String[] cmd) {
		this.cmd = cmd;
		return this;
	}

	public List<String> build() {
		List<String> commands = new Vector<>();
		commands.add(DockerBinary.BINARY_PATH);
		commands.add("run");

		if (volumes != null) {
			for (String volume : volumes) {
				commands.add("-v");
				commands.add(volume);
			}
		}

		commands.add(image);

		for (String a : cmd) {
			commands.add(a);
		}
		return commands;
	}

}
