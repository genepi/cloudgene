package cloudgene.mapred;

import cloudgene.mapred.util.HadoopUtil;

public class HadoopMonitor {

	public static void main(String[] args) throws InterruptedException {

		int time = 0;

		System.out.println("Time\tMap\tReduce\tMaxMap\tMaxReduce");
		
		while (true) {

			System.out.println(System.currentTimeMillis()+"\t"+ HadoopUtil.getInstance().getClusterDetails()
					.getMapTasks()
					+ "\t"
					+ HadoopUtil.getInstance().getClusterDetails()
							.getReduceTasks()
					+ "\t"
					+ HadoopUtil.getInstance().getClusterDetails()
							.getMaxMapTasks()
					+ "\t"
					+ HadoopUtil.getInstance().getClusterDetails()
							.getMaxReduceTasks());

			Thread.sleep(1000);

			time++;
		}

	}

}
