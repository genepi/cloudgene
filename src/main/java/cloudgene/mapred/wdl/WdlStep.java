package cloudgene.mapred.wdl;

import java.util.HashMap;
import java.util.Map;

public class WdlStep {

	private String jar;

	private String mapper;

	private String reducer;

	private String params;

	private String name;

	private String exec;

	private String pig;

	private String spark;

	private String rmd;

	private String rmd2;

	private String template;

	private String output;

	private String job;

	private String classname;

	private String mainClass;
	
	private Map<String, String> config;

	private int id;

	private boolean cache = false;

	private String generates = "";

	private Map<String, String> mapping = new HashMap<String, String>();

	public String getJar() {
		return jar;
	}

	public void setJar(String jar) {
		this.jar = jar;
	}

	public String getMapper() {
		return mapper;
	}

	public void setMapper(String mapper) {
		this.mapper = mapper;
	}

	public String getReducer() {
		return reducer;
	}

	public void setReducer(String reducer) {
		this.reducer = reducer;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setExec(String exec) {
		this.exec = exec;
	}

	public String getExec() {
		return exec;
	}

	public void setPig(String pig) {
		this.pig = pig;
	}

	public String getPig() {
		return pig;
	}

	public String getRmd() {
		return rmd;
	}

	public void setRmd(String rmd) {
		this.rmd = rmd;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getJob() {
		return job;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setGenerates(String generates) {
		this.generates = generates;
	}

	public String getGenerates() {
		return generates;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setMapping(Map<String, String> mapping) {
		this.mapping = mapping;
	}

	public Map<String, String> getMapping() {
		return mapping;
	}

	public String getSpark() {
		return spark;
	}

	public void setSpark(String spark) {
		this.spark = spark;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public void setRmd2(String rmd2) {
		this.rmd2 = rmd2;
	}

	public String getRmd2() {
		return rmd2;
	}
	
	public void setConfig(Map<String, String> config) {
		this.config = config;
	}
	
	public Map<String, String> getConfig() {
		return config;
	}

}
