package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import cloudgene.mapred.jobs.CloudgeneStep;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class StepResponse {

	private int id;
	private String name;
	private int progress;
	
	@JsonProperty("logMessages")    
	private List <MessageResponse> messageResponse;

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

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}


	public static StepResponse build(CloudgeneStep step) {
		StepResponse response = new StepResponse();
		response.setId(step.getId());
		response.setName(step.getName());
		response.setProgress(step.getProgress());
		List<MessageResponse> responses = MessageResponse.build(step.getLogMessages());
		response.setMessageResponse(responses);
		return response;
	}

	public static List<StepResponse> build(List<CloudgeneStep> steps) {
		List<StepResponse> response = new Vector<StepResponse>();
		for (CloudgeneStep step : steps) {
			response.add(StepResponse.build(step));
		}
		return response;
	}

	public List <MessageResponse> getMessageResponse() {
		return messageResponse;
	}

	public void setMessageResponse(List <MessageResponse> messageResponse) {
		this.messageResponse = messageResponse;
	}

}
