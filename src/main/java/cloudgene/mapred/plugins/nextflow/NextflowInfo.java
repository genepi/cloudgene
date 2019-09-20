package cloudgene.mapred.plugins.nextflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sf.json.JSONObject;

public class NextflowInfo {

	private static NextflowInfo instance;
	
	private Map<String, List<JSONObject>> data;
	
	public static NextflowInfo getInstance() {
		if (instance == null) {
			instance = new NextflowInfo();
		}
		
		return instance;
	}
	
	private NextflowInfo() {
		data = new HashMap<String, List<JSONObject>>();
	}
	
	public void addEvent(String job, JSONObject event) {
		List<JSONObject> events = data.get(job);
		if (events == null) {
			events = new Vector<JSONObject>();
			data.put(job, events);
		}
		events.add(event);
	}
	
	public List<JSONObject> getEvents(String job){
		List<JSONObject> events = data.get(job);
		if (events == null) {
			events = new Vector<JSONObject>();
		}
		return events;
	}
	
}
