import cloudgene.mapred.jobs.sdk.WorkflowContext

def run(WorkflowContext context) {

	def name = context.getData("app");

	println(name);

	context.ok("Hello ${name}!");

	return true;
}
