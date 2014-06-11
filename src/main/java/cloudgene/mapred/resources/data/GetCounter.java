package cloudgene.mapred.resources.data;

import java.util.Map;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import cloudgene.mapred.database.CounterDao;

public class GetCounter extends ServerResource {

	@Get
	public Representation get() {


		CounterDao dao = new CounterDao();
		Map<String, Integer> counters = dao.getAll();
		
		String temp = "{";
		boolean first = true;
		for (String key: counters.keySet()){
			
			if (!first){
				temp +=",";
			}
			
			temp += "\"" + key + "\": \"" + counters.get(key) + "\"";
			first = false;
		}
		
		temp += "}";

		return new StringRepresentation(temp);

	}

}
