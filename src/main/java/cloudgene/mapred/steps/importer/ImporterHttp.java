package cloudgene.mapred.steps.importer;

import genepi.hadoop.HdfsUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import cloudgene.mapred.util.FileItem;

public class ImporterHttp implements IImporter {

	private String url;

	private String path;

	private CountingOutputStream t;

	private String error;

	public ImporterHttp(String url, String path) {

		this.url = url.split(";")[0];
		this.path = path;

	}

	public long getFileSize() {

		try {
			URL webUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) webUrl
					.openConnection();
			return conn.getContentLength();

		} catch (MalformedURLException e) {
			error = e.getMessage();
			return -1;
		} catch (IOException e) {
			error = e.getMessage();
			return -1;

		}

	}

	@Override
	public boolean importFiles() {
		return importFiles(null);
	}

	@Override
	public boolean importFiles(String extension) {

		try {
			FileSystem fileSystem = HdfsUtil.getFileSystem();
			return importIntoHdfs(url, fileSystem, path);
		} catch (IOException e) {
			error = e.getMessage();
			return false;
		}

	}

	public boolean importIntoHdfs(String weburl, FileSystem fileSystem,
			String path) throws IOException {

		URL url = new URL(weburl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

		// path in hdfs
		String[] tiles = weburl.split("/");
		String name = tiles[tiles.length - 1];

		String target = HdfsUtil.path(path, name);

		FSDataOutputStream out = fileSystem.create(new Path(target));

		t = new CountingOutputStream(out);

		IOUtils.copyBytes(in, t, fileSystem.getConf());

		IOUtils.closeStream(in);
		IOUtils.closeStream(out);

		return true;
	}

	@Override
	public List<FileItem> getFiles() {
		List<FileItem> items = new Vector<FileItem>();
		FileItem file = new FileItem();
		file.setText(FilenameUtils.getName(url));
		file.setPath("/");
		file.setId("/");
		file.setSize(FileUtils.byteCountToDisplaySize(getFileSize()));
		items.add(file);
		return items;
	}

	@Override
	public String getErrorMessage() {
		return error;
	}

}
