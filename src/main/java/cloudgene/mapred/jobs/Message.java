package cloudgene.mapred.jobs;

public class Message {

	public static final int OK = 0;

	public static final int ERROR = 1;

	public static final int WARNING = 2;

	public static final int RUNNING = 3;

	private String message;

	private int type;

	private CloudgeneStep step;

	private long time;

	public Message() {

	}

	public Message(CloudgeneStep step, int type, String message) {
		this.type = type;
		this.message = message;
		this.step = step;
	}

	public String getMessage() {
		if (message != null) {
			return message;
		} else {
			return "";
		}
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public CloudgeneStep getStep() {
		return step;
	}

	public void setStep(CloudgeneStep step) {
		this.step = step;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
