package cloudgene.mapred.plugins.rscript;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.util.Settings;

public class RScriptPlugin implements IPlugin{

	public static final String ID = "rscript";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "RScript";
	}

	@Override
	public boolean isInstalled() {
		return RScriptBinary.isInstalled();
	}

	@Override
	public String getDetails() {
		return RScriptBinary.getVersion();
	}

	@Override
	public void configure(Settings settings) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getStatus() {
		if (isInstalled()) {
			return "RScript support enabled.";
		} else {
			return "RScript Binary not found.";
		}
	}
}
