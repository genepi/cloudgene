package cloudgene.mapred.wdl;

import java.util.HashMap;

public class WdlStep extends HashMap<String, Object>{

	public String getName() {
		return getString("name");
	}

	public String getClassname() {
		return getString("classname");
	}
	
	public void setClassname(String classname){
		put("classname", classname);
	}


	public String getJar() {
		return getString("jar");
	}

	public String getGenerates() {
		return getString("generates");
	}
	
	public String getString(String key){
		return getString(key, null);
	}

	public String getString(String key, String defaultValue){
		Object value = get(key);
		return value != null ? value.toString() : defaultValue;
	}
	
}
