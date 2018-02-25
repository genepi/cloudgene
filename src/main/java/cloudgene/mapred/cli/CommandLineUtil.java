package cloudgene.mapred.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlParameterInput;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class CommandLineUtil {

	public static Options createOptionsFromApp(WdlApp app) {
		Options options = new Options();
		if (app.getWorkflow() == null) {
			return options;
		}
		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (!input.getType().equals("agbcheckbox") && !input.getType().equals("terms_checkbox") && !input.isAdminOnly() && input.isVisible()) {
				Option option = new Option(null, input.getId(), true, input.getDescription());

				boolean hasDefault = input.getValue() != null && !input.getValue().trim().isEmpty();
				option.setRequired(input.isRequired() && !hasDefault);

				if (!input.getType().equals("list")) {
					option.setArgName(input.getType().toString());
				} else {
					String value = input.getDescription();
					for (String key : input.getValues().keySet()) {
						value += "\n  " + key + ": " + input.getValues().get(key);
					}
					option.setArgName(input.getId());
					option.setDescription(value);
				}

				if (hasDefault) {
					option.setDescription(option.getDescription() + "\n(default: " + input.getValue() + ")");
				}

				options.addOption(option);

			}
		}
		return options;
	}

	public static Map<String, String> createParams(WdlApp app, CommandLine line, String local, String hdfs)
			throws FileNotFoundException {
		Map<String, String> props = new HashMap<String, String>();
		if (app.getWorkflow() == null) {
			return props;
		}
		for (WdlParameterInput input : app.getWorkflow().getInputs()) {

			String value = line.getOptionValue(input.getId(), input.getValue());

			if (input.isFileOrFolder()) {

				if (value.startsWith("http://") || value.startsWith("https://")) {
					props.put(input.getId(), value);
				} else {

					File tmpFile = new File(value);
					if (!tmpFile.exists()) {
						throw new FileNotFoundException("File " + value + " not found.");
					}
					String entryName = tmpFile.getName();

					if (input.isHdfs()) {
						String targetPath = HdfsUtil.path(hdfs, "input", input.getId());
						if (tmpFile.isDirectory()) {
							String[] files = FileUtil.getFiles(value, "");
							for (String sourceFile : files) {
								if (!new File(sourceFile).isDirectory()) {
									String name = FileUtil.getFilename(sourceFile);
									String target = HdfsUtil.path(targetPath, name);
									HdfsUtil.put(sourceFile, target);
								}

							}
						} else {
							String target = HdfsUtil.path(targetPath, entryName);
							HdfsUtil.put(value, target);
						}

						if (input.isFolder()) {
							// folder
							props.put(input.getId(), HdfsUtil.path(hdfs, "input", input.getId()));
						} else {
							// file
							props.put(input.getId(), HdfsUtil.path(hdfs, "input", input.getId(), entryName));
						}

					} else {

						String targetPath = FileUtil.path(local, "input", input.getId());

						FileUtil.createDirectory(targetPath);

						if (tmpFile.isDirectory()) {
							String[] files = FileUtil.getFiles(value, "");
							for (String sourceFile : files) {
								if (!new File(sourceFile).isDirectory()) {
									String name = FileUtil.getFilename(sourceFile);
									String targetFile = FileUtil.path(targetPath, name);
									FileUtil.copy(sourceFile, targetFile);
								}
							}
						} else {
							String targetFile = FileUtil.path(targetPath, entryName);
							// System.out.println("Copy file " + value + " to "
							// +
							// targetFile);
							FileUtil.copy(value, targetFile);
						}

						if (input.isFolder()) {
							// folder
							props.put(input.getId(),
									new File(FileUtil.path(local, "input", input.getId())).getAbsolutePath());
						} else {
							// file
							props.put(input.getId(), new File(FileUtil.path(local, "input", input.getId(), entryName))
									.getAbsolutePath());
						}

					}
				}
			} else {

				props.put(input.getId(), value);

			}

		}

		Map<String, String> params = new HashMap<String, String>();
		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (!input.getType().equals("agbcheckbox") &&! input.getType().equals("terms_checkbox")) {
				if (props.containsKey(input.getId())) {
					if (input.getType().equals("checkbox")) {
						params.put(input.getId(), input.getValues().get("true"));
					} else {
						params.put(input.getId(), props.get(input.getId()));
					}
				} else {
					if (input.getType().equals("checkbox")) {
						params.put(input.getId(), input.getValues().get("false"));
					}
				}
			}
		}

		return params;
	}

}
