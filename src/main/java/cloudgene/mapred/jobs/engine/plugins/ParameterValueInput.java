package cloudgene.mapred.jobs.engine.plugins;

import cloudgene.mapred.wdl.WdlParameterInput;
import cloudgene.mapred.wdl.WdlParameterInputType;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import genepi.io.WildCardFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

		if (parameter.getTypeAsEnum() == WdlParameterInputType.HDFS_FOLDER) {
			List<String> files = null;
			try {

				files = HdfsUtil.getFiles(value, ext);
				String[] result = new String[files.size()];
				for (int i = 0; i < files.size(); i++) {
					result[i] = FileUtil.getFilename(files.get(i));
				}
				return result;
			} catch (IOException e) {
				e.printStackTrace();
				String[] result = new String[1];
				result[0] = FileUtil.getFilename(value);
				;
				return result;
			}
		}

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
