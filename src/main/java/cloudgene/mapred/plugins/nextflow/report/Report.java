package cloudgene.mapred.plugins.nextflow.report;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Vector;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.JsonIOException;
import com.nimbusds.jose.shaded.gson.JsonSyntaxException;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;

import cloudgene.mapred.plugins.nextflow.report.ReportEvent.WebCommand;

public class Report {

	public static final String DEFAULT_FILENAME = "cloudgene.report.json";

	private List<ReportEvent> events = new Vector<ReportEvent>();

	public Report() {

	}

	public Report(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		loadFromFile(filename);
	}

	public Report(InputStream in) throws JsonIOException, JsonSyntaxException, IOException {
		loadFromInputStream(in);
	}

	public void loadFromFile(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		Type collectionType = new TypeToken<List<ReportEvent>>() {
		}.getType();
		Gson gson = (new GsonBuilder()).create();
		events = gson.fromJson(new FileReader(filename), collectionType);
	}

	public void loadFromInputStream(InputStream in) throws JsonIOException, JsonSyntaxException, IOException {
		Type collectionType = new TypeToken<List<ReportEvent>>() {
		}.getType();
		Gson gson = (new GsonBuilder()).create();
		events = gson.fromJson(new InputStreamReader(in), collectionType);
		in.close();
	}

	public boolean hasInMemory(String content) {
		for (ReportEvent event : events) {
			if (event.toString().contains(content)) {
				return true;
			}
		}
		return false;
	}

	public void saveToFile(String filename) throws JsonIOException, IOException {
		Gson gson = (new GsonBuilder()).create();
		gson.toJson(events, new FileWriter(filename));
	}

	public List<ReportEvent> getEvents() {
		return events;
	}

	public void println(String line) {
		addEvent(WebCommand.PRINTLN, line);
	}

	public void log(String line) {
		addEvent(WebCommand.LOG, line);
	}

	public void incCounter(String name, int value) {
		addEvent(WebCommand.INC_COUNTER, name, value);
	}

	public void submitCounter(String name) {
		addEvent(WebCommand.SUBMIT_COUNTER, name);
	}

	public void message(String message, int type) {
		addEvent(WebCommand.MESSAGE, message, type);
	}

	public void beginTask(String name) {
		addEvent(WebCommand.BEGIN_TASK, name);
	}

	public void updateTask(String name, int type) {
		addEvent(WebCommand.UPDATE_TASK, name, type);
	}

	public void endTask(String message, int type) {
		addEvent(WebCommand.END_TASK, message, type);
	}

	public void addEvent(WebCommand command, Object... params) {
		ReportEvent event = new ReportEvent(command, params);
		events.add(event);
		// TODO: autosave?
	}

}
