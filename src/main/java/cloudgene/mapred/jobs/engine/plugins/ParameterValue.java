package cloudgene.mapred.jobs.engine.plugins;

import genepi.io.FileUtil;

import java.io.IOException;
import java.util.List;

import cloudgene.mapred.util.HdfsUtil;
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

	/*public String[] listFiles(String ext) {

		//if (parameter.getType().equals(Parameter.HDFS_FOLDER)) {
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
		/*}

		if (parameter.getType().equals(Parameter.LOCAL_FOLDER)) {

		}

		return new String[] { value };*/

	/*}*/
	
	public String getName(){
		
		return FileUtil.getFilename(value);
		
	}
}
