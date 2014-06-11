package cloudgene.mapred.jobs.export;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ExportJob {

	private String output;

	private String input;

	private String localOut;

	private String s3Bucket;

	private String directory;

	private String awsKey;

	private String awsSecretKey;

	private String name;

	public String getLocalOut() {
		return localOut;
	}

	public void setLocalOut(String localOut) {
		this.localOut = localOut;
	}

	public ExportJob(String name) throws IOException {

		this.name = name;
	}

	public boolean execute() {

		Configuration conf = new Configuration();
		conf.set("aws-key", awsKey);
		conf.set("aws-secret-key", awsSecretKey);
		conf.set("s3-bucket", s3Bucket);
		conf.set("directory", directory);

		Job job;
		try {
			job = new Job(conf, name);
			job.setJarByClass(ExportJob.class);
			job.setInputFormatClass(ExportInputFormat.class);
			job.setMapperClass(ExportMapper.class);
			
			try {
				FileInputFormat.addInputPath(job, new Path(input));
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileOutputFormat.setOutputPath(job, new Path(output));

			job.waitForCompletion(false);
			return job.isSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getOutput() {
		return output;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getInput() {
		return input;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getDirectory() {
		return directory;
	}

	public void setAwsKey(String awsKey) {
		this.awsKey = awsKey;
	}

	public String getAwsKey() {
		return awsKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

}