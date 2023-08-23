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
import cloudgene.mapred.wdl.WdlParameterInputType;

public class CommandLineUtil {

	public static Options createOptionsFromApp(WdlApp app) {
		Options options = new Options();
		if (app.getWorkflow() == null) {
			return options;
		}
		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (input.getTypeAsEnum() != WdlParameterInputType.SEPARATOR
					&& input.getTypeAsEnum() != WdlParameterInputType.INFO
					&& input.getTypeAsEnum() != WdlParameterInputType.LABEL
					&& input.getTypeAsEnum() != WdlParameterInputType.AGBCHECKBOX
					&& input.getTypeAsEnum() != WdlParameterInputType.TERMS_CHECKBOX && !input.isAdminOnly()
					&& input.isVisible()) {
				Option option = new Option(null, input.getId(), true, input.getDescription());

				boolean hasDefault = input.getValue() != null && !input.getValue().trim().isEmpty();
				option.setRequired(input.isRequired() && !hasDefault);

				if (input.getTypeAsEnum() != WdlParameterInputType.LIST) {
					option.setArgName(input.getTypeAsEnum().toString());
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
						throw new FileNotFoundException(input.getDescription() + ": file '" + value + "' not found.");
					}
					String entryName = tmpFile.getName();

					if (input.isHdfs()) {
						throw new RuntimeException("HDFS not supported in CG3");

					} else {
						props.put(input.getId(), value);
					}
				}
			} else {

				props.put(input.getId(), value);

			}

		}

		Map<String, String> params = new HashMap<String, String>();
		for (WdlParameterInput input : app.getWorkflow().getInputs()) {
			if (input.getTypeAsEnum() != WdlParameterInputType.SEPARATOR
					&& input.getTypeAsEnum() != WdlParameterInputType.INFO
					&& input.getTypeAsEnum() != WdlParameterInputType.LABEL
					&& input.getTypeAsEnum() != WdlParameterInputType.AGBCHECKBOX) {
				if (props.containsKey(input.getId())) {
					if (input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX) {
						params.put(input.getId(), input.getValues().get("true"));
					} else {
						params.put(input.getId(), props.get(input.getId()));
					}
				} else {
					if (input.getTypeAsEnum() == WdlParameterInputType.CHECKBOX) {
						params.put(input.getId(), input.getValues().get("false"));
					}
				}
			}
		}

		return params;
	}

}
