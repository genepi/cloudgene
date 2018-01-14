import genepi.hadoop.common.WorkflowContext
import genepi.hadoop.HdfsUtil

def run(WorkflowContext context) {

	def hdfs = context.getConfig("file");
	def tempFile = context.getLocalTemp()+"/file.txt";

	//export
	HdfsUtil.get(hdfs, tempFile);

	def content = new File(tempFile).text;
	context.ok(content);

	return true;
}
