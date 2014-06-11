package cloudgene.mapred.util.rscript;

public class RScript extends Command {

	public RScript(String script) {
		super("/usr/bin/Rscript");
		setParams(script);
	}

	public RScript() {
		super("/usr/bin/Rscript");
	}

	public void setScript(String script) {
		setParams(script);
	}

}
