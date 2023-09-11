package cloudgene.mapred.plugins.nextflow;

import cloudgene.mapred.jobs.Message;

public class NextflowProcessRenderer {

	public static void render(NextflowProcessConfig config, NextflowProcess process, Message message) {

		String label = (config.getLabel() != null ? config.getLabel() : process.getName());

		switch (config.getView()) {
		case "progressbar":
			NextflowProcessRenderer.renderProgressbar(label, process, message);
			break;
		default:
			NextflowProcessRenderer.renderList(label, process, message);
		}
	}

	public static void renderList(String label, NextflowProcess process, Message message) {

		String text = "<b>" + label + "</b>";
		boolean running = false;
		boolean ok = true;
		for (NextflowTask task : process.getTasks()) {

			// TODO: use templates.

			String status = (String) task.getTrace().get("status");

			if (status.equals("RUNNING") || status.equals("SUBMITTED")) {
				running = true;
			}
			if (!status.equals("COMPLETED")) {
				ok = false;

			}
			text += "<br><small>";

			text += (String) task.getTrace().get("name");
			if (status.equals("RUNNING")) {
				text += "...";
			}
			if (status.equals("COMPLETED")) {
				text += "&nbsp;<i class=\"fas fa-check text-success\"></i>";
			}
			if (status.equals("KILLED") || status.equals("FAILED")) {
				text += "&nbsp;<i class=\"fas fa-times text-danger\"></i>";
			}

			if (task.getLogText() != null) {
				text += "<br>" + task.getLogText();
			}

			text += "</small>";
		}

		message.setMessage(text);

		if (running) {
			message.setType(Message.RUNNING);
		} else {
			if (ok) {
				message.setType(Message.OK);
			} else {
				message.setType(Message.ERROR);
			}
		}

	}

	public static void renderProgressbar(String label, NextflowProcess process, Message message) {

		String text = "<b>" + label + "</b><br><br>";
		int running = 0;
		boolean ok = true;
		for (NextflowTask task : process.getTasks()) {

			String status = (String) task.getTrace().get("status");

			if (status.equals("RUNNING") || status.equals("SUBMITTED")) {
				running++;
			}
			if (!status.equals("COMPLETED")) {
				ok = false;
			}
			text += "&nbsp;";

			String style = "";
			String name = "&nbsp;";

			if (status.equals("COMPLETED")) {
				style = "badge-success";
				name = "OK";
			}

			if (status.equals("RUNNING")) {
				style = "badge-info";
				name = "...";
			}

			if (status.equals("KILLED") || status.equals("FAILED")) {
				style = "badge-danger";
				name = "X";
			}

			text += "<span class=\"badge " + style + "\" style=\"width: 30px\">" + name + "</span>";

		}

		message.setMessage(text);

		if (running > 0) {
			message.setType(Message.RUNNING);
		} else {
			if (ok) {
				message.setType(Message.OK);
			} else {
				message.setType(Message.ERROR);
			}
		}

	}

}
