package cloudgene.mapred.core;

import java.util.ArrayList;
import java.util.Collections;

public class AppList {
	private ArrayList<String> apps;

	public ArrayList<String> getApps() {
		return apps;
	}

	public void setApps(ArrayList<String> apps) {
		this.apps = apps;
	}
	
	public void sort(){
		Collections.sort(apps);
	}

}