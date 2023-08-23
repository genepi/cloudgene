package cloudgene.mapred.jobs.engine.plugins;

import java.io.File;

import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;
import genepi.io.FileUtil;
import genepi.io.WildCardFileFilter;

public class ParameterValueInput {

	private WdlParameterInput parameter;
	private String value;

	public ParameterValueInput(WdlParameterInput parameter, String value) {
		this.parameter = parameter;
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public String[] listFiles(String ext) {

		if (parameter.getTypeAsEnum() == WdlParameterInputType.LOCAL_FOLDER) {
			return getFiles(value, ext);

		}

		return new String[] { value };

	}

	private static String[] getFiles(String path, String ext) {
		File dir = new File(path);
		File[] files = dir.listFiles(new WildCardFileFilter(ext));

		if (files != null) {

			String[] names = new String[files.length];

			for (int i = 0; i < names.length; ++i) {
				names[i] = files[i].getName();
			}
			return names;

			
		} else {
			String[] names = new String[1];
			names[0] = FileUtil.getFilename(path);
			return names;

		}
	}

	public String getName() {

		return FileUtil.getFilename(value);

	}
}
