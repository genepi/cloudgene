package cloudgene.mapred.server.responses;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import cloudgene.mapred.jobs.CloudgeneStep;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class CloudgeneStepResponse {

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


	public static CloudgeneStepResponse build(CloudgeneStep step) {
		CloudgeneStepResponse response = new CloudgeneStepResponse();
		response.setId(step.getId());
		response.setName(step.getName());
		response.setProgress(step.getProgress());
		List<MessageResponse> responses = MessageResponse.build(step.getLogMessages());
		response.setMessageResponse(responses);
		return response;
	}

	public static List<CloudgeneStepResponse> build(List<CloudgeneStep> steps) {
		List<CloudgeneStepResponse> response = new Vector<CloudgeneStepResponse>();
		for (CloudgeneStep step : steps) {
			response.add(CloudgeneStepResponse.build(step));
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
