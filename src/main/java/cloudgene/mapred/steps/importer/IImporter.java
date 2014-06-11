package cloudgene.mapred.steps.importer;

import java.util.List;

import cloudgene.mapred.util.FileItem;

public interface IImporter {

	public boolean importFiles();

	public String getErrorMessage();

	public List<FileItem> getFiles();

}
