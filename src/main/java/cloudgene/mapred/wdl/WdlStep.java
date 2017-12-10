package cloudgene.mapred.wdl;

import java.util.HashMap;

public class WdlStep extends HashMap<String, String>{

	public String getName() {
		return get("name");
	}

	public String getClassname() {
		return get("classname");
	}
	
	public void setClassname(String classname){
		put("classname", classname);
	}


	public String getJar() {
		return get("jar");
	}

	public String getGenerates() {
		return get("generates");
	}
	
}
