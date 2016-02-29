package cloudgene.mapred.jobs.steps;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

public class CheckHdfsInputs extends WorkflowStep {

	@Override
	public boolean run(WorkflowContext context) {

		try {

			String fileContent = readFrom(context.get("file"));

			String fileContentInFolder1 = readFrom(context.get("folder") + "/"
					+ "test1.txt");
			String fileContentInFolder2 = readFrom(context.get("folder") + "/"
					+ "test2.txt");

			boolean result = fileContent.equals("content-of-my-file")
					&& fileContentInFolder1
							.equals("content-of-my-file-in-folder1")
					&& fileContentInFolder2
							.equals("content-of-my-file-in-folder2");

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String readFrom(String hdfs) throws IOException {
		FileSystem fs = HdfsUtil.getFileSystem();
		LineReader reader = new LineReader(fs.open(new Path(hdfs)));
		Text text = new Text();
		reader.readLine(text);
		reader.close();
		return text.toString();
	}

}
