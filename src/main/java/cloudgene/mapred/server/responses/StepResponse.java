package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import cloudgene.mapred.jobs.CloudgeneStep;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonClassDescription
public class StepResponse {

	private int id;
	
	private String name;

	@JsonProperty("logMessages")
	private List<MessageResponse> messages;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static StepResponse build(CloudgeneStep step) {
		StepResponse response = new StepResponse();
		response.setId(step.getId());
		response.setName(step.getName());
		List<MessageResponse> responses = MessageResponse.build(step.getLogMessages());
		response.setMessages(responses);
		return response;
	}

	public static List<StepResponse> build(List<CloudgeneStep> steps) {
		List<StepResponse> response = new Vector<StepResponse>();
		for (CloudgeneStep step : steps) {
			response.add(StepResponse.build(step));
		}
		return response;
	}

	public List<MessageResponse> getMessages() {
		return messages;
	}

	public void setMessages(List<MessageResponse> messages) {
		this.messages = messages;
	}

}
