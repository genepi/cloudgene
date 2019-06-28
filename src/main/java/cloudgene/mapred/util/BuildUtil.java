package cloudgene.mapred.util;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class BuildUtil {

	public static String getBuildInfos() {

		String buildTime = "unknown";
		String builtBy = "unknown";

		try {

			URL url = BuildUtil.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attr = manifest.getMainAttributes();
			buildTime = attr.getValue("Build-Time");
			builtBy = attr.getValue("Built-By");

		} catch (IOException E) {

		}

		return "Built by " + builtBy + " on " + buildTime;

	}

}
