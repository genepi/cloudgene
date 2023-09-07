package cloudgene.mapred.plugins.nextflow.report;

import java.io.IOException;

import cloudgene.mapred.jobs.sdk.WorkflowContext;

public class ReportEventExecutor {

	public static void execute(ReportEvent event, WorkflowContext context) throws IOException {

		switch (event.getCommand()) {
		case SUBMIT_COUNTER: {
			String name = (String) event.getParams()[0];
			context.submitCounter(name);
			break;
		}
		case MESSAGE: {
			String message = (String) event.getParams()[0];
			int type = ((Double) event.getParams()[1]).intValue();
			context.message(message, type);
			break;
		}
		case BEGIN_TASK: {
			String name = (String) event.getParams()[0];
			context.beginTask(name);
			break;
		}
		case UPDATE_TASK: {
			String name = (String) event.getParams()[0];
			int type = ((Double) event.getParams()[1]).intValue();
			context.updateTask(name, type);
			break;
		}
		case LOG: {
			String line = (String) event.getParams()[0];
			context.log(line);
			break;
		}
		case END_TASK: {
			String name = (String) event.getParams()[0];
			int type = ((Double) event.getParams()[1]).intValue();
			context.endTask(name, type);
			break;
		}
		case INC_COUNTER: {
			String name = (String) event.getParams()[0];
			int count = ((Double) event.getParams()[1]).intValue();
			context.incCounter(name, count);
			break;
		}
		default:
			throw new IOException("Command " + event.getCommand() + " not yet supported.");
		}

	}

}
