package cloudgene.mapred.jobs.queue;

public abstract class PriorityRunnable implements Runnable, Comparable<PriorityRunnable>{

	private long priority;
	
	public void setPriority(long priority) {
		this.priority = priority;
	}
	
	public long getPriority() {
		return priority;
	}
	
	@Override
	public int compareTo(PriorityRunnable other) {
		if (this.getPriority() == other.getPriority()){
			return 0;
		}else{
			if (this.getPriority() < other.getPriority()){
				return -1;
			}else{
				return 1;
			}
		}
	}
}
