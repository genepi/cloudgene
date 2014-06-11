package cloudgene.mapred.jobs.export;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class ExportRecordReader extends RecordReader<LongWritable, Text> {

	private LongWritable key = null;
	private Text value = null;
	private Path file;

	public ExportRecordReader() {
	}

	public void initialize(InputSplit genericSplit, TaskAttemptContext context)
			throws IOException {

		FileSplit split = (FileSplit) genericSplit;
		file = split.getPath();

	}

	public boolean nextKeyValue() throws IOException {
		if (value == null) {
			key = new LongWritable(27);
			value = new Text(file.toString());
			return true;
		} else {
			return false;
		}

	}

	@Override
	public LongWritable getCurrentKey() {
		return key;
	}

	@Override
	public Text getCurrentValue() {
		return value;
	}

	public synchronized void close() throws IOException {

	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return 0;
	}
}
