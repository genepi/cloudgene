package cloudgene.mapred.plugins.rscript;

import cloudgene.mapred.plugins.IPlugin;
import cloudgene.mapred.util.Settings;

public class RMarkdownPlugin implements IPlugin {

	public static final String ID = "rmarkdown";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "RMarkdown";
	}

	@Override
	public boolean isInstalled() {
		if (RScriptBinary.isInstalled()) {
			return RScriptBinary.isMarkdownInstalled();
		} else {
			return false;
		}
	}

	@Override
	public String getDetails() {
		return RScriptBinary.getMarkdownDetails();
	}

	@Override
	public void configure(Settings settings) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getStatus() {
		if (RScriptBinary.isInstalled()) {
			if (isInstalled()) {
				return "RMarkdown support enabled.";
			} else {
				return "RMarkdown support disabled. Please install the following packages: "
						+ String.join(" ", RScriptBinary.PACKAGES) + "<br><br><pre>"
						+ RScriptBinary.getMarkdownErrorDetails() + "</pre>";
			}
		} else {
			return "RMarkdown support disabled. Please install R.";
		}
	}
}
