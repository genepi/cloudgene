package cloudgene.mapred.plugins.nextflow;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;

import cloudgene.mapred.jobs.Message;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

public class NextflowProcessRenderer {

	private static final String TEMPLATES_LIST = "/templates/list.html";

	private static final String TEMPLATES_PROGRESSBAR = "/templates/progressbar.html";

	private static final String FAILED = "FAILED";

	private static final String KILLED = "KILLED";

	private static final String VIEW_PROGRESSBAR = "progressbar";

	private static final String TRACE_STATUS = "status";

	private static final String SUBMITTED = "SUBMITTED";

	private static final String COMPLETED = "COMPLETED";

	private static final String RUNNING = "RUNNING";

	public static final Map<String, Template> CACHE = new HashMap<String, Template>();

	public static void render(NextflowProcessConfig config, NextflowProcess process, Message message) {

		String label = (config.getLabel() != null ? config.getLabel() : process.getName());

		switch (config.getView()) {
		case VIEW_PROGRESSBAR:
			NextflowProcessRenderer.renderAsProgressbar(label, process, message);
			break;
		default:
			NextflowProcessRenderer.renderAsList(label, process, message);
		}
	}

	public static void renderAsList(String label, NextflowProcess process, Message message) {
		render(label, TEMPLATES_LIST, process, message);
	}

	public static void renderAsProgressbar(String label, NextflowProcess process, Message message) {
		render(label, TEMPLATES_PROGRESSBAR, process, message);
	}

	public static void render(String label, String template, NextflowProcess process, Message message) {

		int running = 0;
		int completed = 0;
		int failed = 0;
		for (NextflowTask task : process.getTasks()) {

			String status = (String) task.getTrace().get(TRACE_STATUS);

			if (status.equals(RUNNING) || status.equals(SUBMITTED)) {
				running++;
			}
			if (status.equals(COMPLETED)) {
				completed++;
			}
			if (status.equals(FAILED) || status.equals(KILLED)) {
				failed++;
			}
		}

		int total = running + completed + failed;
		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("label", label);
		bindings.put("total", total);
		bindings.put("running", running);
		bindings.put("completed", completed);
		bindings.put("failed", failed);
		bindings.put("tasks", process.getTasks());

		try {
			String text = renderTemplate(template, bindings);
			message.setMessage(text);
		} catch (Exception e) {
			message.setMessage("Template could not be renderer: " + e.toString());
		}
		if (running > 0) {
			message.setType(Message.RUNNING);
		} else if (completed > 0) {
			message.setType(Message.OK);
		} else {
			message.setType(Message.ERROR);
		}

	}

	public static String renderTemplate(String path, Map<String, Object> bindings)
			throws CompilationFailedException, ClassNotFoundException, IOException, URISyntaxException {
		Template template = getTemplate(path);
		String rendered = template.make(bindings).toString();
		return rendered.replaceAll("\n", "");
	}

	public static synchronized Template getTemplate(String path)
			throws IOException, URISyntaxException, CompilationFailedException, ClassNotFoundException {
		Template template = CACHE.get(path);

		if (template != null) {
			return template;
		}

		SimpleTemplateEngine engine = new SimpleTemplateEngine();
		String content = readTemplate(path);
		template = engine.createTemplate(content);
		CACHE.put(path, template);
		return template;
	}

	private static String readTemplate(String path) throws IOException, URISyntaxException {
		URI uri = NextflowProcess.class.getResource(path).toURI();
		Path test = Paths.get(uri);
		return Files.readString(test);
	}

}
