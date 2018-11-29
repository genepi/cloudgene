package cloudgene.mapred.plugins;

import cloudgene.mapred.util.Settings;

public interface IPlugin {

	public String getId();
	
	public String getName();
	
	public boolean isInstalled();
	
	public String getDetails();
	
	public void configure(Settings settings);
	
	public String getStatus();
		
}
