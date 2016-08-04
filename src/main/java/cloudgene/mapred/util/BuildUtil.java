package cloudgene.mapred.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import cloudgene.mapred.Main;

public class BuildUtil {

	public static String getBuildInfos(){
		
		//load build infos from manifest file
		URLClassLoader cl = (URLClassLoader) Main.class.getClassLoader();
		try {
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attr = manifest.getMainAttributes();
			String buildVesion = attr.getValue("Version");
			String buildTime = attr.getValue("Build-Time");
			String builtBy = attr.getValue("Built-By");
			return "Built by " + builtBy + " on " + buildTime;

		} catch (IOException E) {
			return "unkown";
		}
	}
	
}
