package cloudgene.mapred.jobs.engine.plugins;

import java.io.File;
import java.io.IOException;
import java.util.List;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import genepi.io.WildCardFileFilter;
import cloudgene.mapred.wdl.WdlParameter;

public class ParameterValue {

	private WdlParameter parameter;
	private String value;

	public ParameterValue(WdlParameter parameter, String value) {
		this.parameter = parameter;
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public String[] listFiles(String ext) {

		if (parameter.getType().equals(WdlParameter.HDFS_FOLDER)) {
			List<String> files = null;
			try {
				files = HdfsUtil.getFiles(value, ext);
				String[] result = new String[files.size()];
				for (int i = 0; i < files.size(); i++) {
					result[i] = FileUtil.getFilename(files.get(i));
				}
				return result;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
				return null;
			}
		}

		if (parameter.getType().equals(WdlParameter.LOCAL_FOLDER)) {

			System.out.println("---> " + value);
			
			return getFiles(value, ext);
			
		}

		return new String[] { value };

	}
	
	
	private static String[] getFiles(String path, String ext) {
		File dir = new File(path);
		File[] files = dir.listFiles(new WildCardFileFilter(ext));

		String[] names = new String[files.length];

		for (int i = 0; i < names.length; ++i) {
			names[i] = files[i].getName();
		}

		return names;
	}
	
	public String getName(){
		
		return FileUtil.getFilename(value);
		
	}
}
