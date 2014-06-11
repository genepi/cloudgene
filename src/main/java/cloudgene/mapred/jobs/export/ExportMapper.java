package cloudgene.mapred.jobs.export;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import cloudgene.mapred.util.S3Util;

public class ExportMapper extends Mapper<LongWritable, Text, LongWritable, Text> {

	private String awsKey;

	private String awsSecretKey;

	private String s3Bucket;

	private String directory;

	protected void setup(
			org.apache.hadoop.mapreduce.Mapper<LongWritable, Text, LongWritable, Text>.Context context)
			throws IOException, InterruptedException {
		
		awsKey = context.getConfiguration().get("aws-key");
		awsSecretKey = context.getConfiguration().get("aws-secret-key");
		s3Bucket = context.getConfiguration().get("s3-bucket");
		directory = context.getConfiguration().get("directory");
		
		
	};

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {

		S3Util.copyHdfsFile(awsKey, awsSecretKey, s3Bucket, directory,
				value.toString());

	}
}