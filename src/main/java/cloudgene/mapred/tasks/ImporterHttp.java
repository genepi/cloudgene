package cloudgene.mapred.tasks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import cloudgene.mapred.util.HdfsUtil;
import cloudgene.mapred.util.Settings;

public class ImporterHttp extends AbstractTask {

	private String url;

	private String path;

	private CountingOutputStream t;

	private long size = 0;

	public ImporterHttp(String url, String path) {

		setName("import-http");
		
		this.url = url;
		this.path = path;

		size = getSize();

	}

	private long getSize() {

		try {
			URL webUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) webUrl
					.openConnection();
			return conn.getContentLength();

		} catch (MalformedURLException e) {
			return 0;
		} catch (IOException e) {
			return 0;

		}

	}

	@Override
	public boolean execute() {

		Configuration conf = new Configuration();
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			return importIntoHdfs(url, fileSystem, path);
		} catch (IOException e) {
			writeOutput(e.getLocalizedMessage());
			return false;
		}

	}

	public boolean importIntoHdfs(String weburl, FileSystem fileSystem,
			String path) throws IOException {

		writeOutput("Importing File '" + weburl + "...");

		URL url = new URL(weburl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

		// path in hdfs
		String[] tiles = weburl.split("/");
		String name = tiles[tiles.length - 1];
		Settings settings = Settings.getInstance();
		String workspace = settings.getHdfsWorkspace(job.getUser().getUsername());
		String target = HdfsUtil.path(workspace, path, name);

		FSDataOutputStream out = fileSystem.create(new Path(target));

		t = new CountingOutputStream(out);

		IOUtils.copyBytes(in, t, fileSystem.getConf());

		IOUtils.closeStream(in);
		IOUtils.closeStream(out);

		return true;
	}

	@Override
	public String[] getParameters() {
		return new String[] { "Folder-Name", "Size", "Type", "URL" };
	}

	@Override
	public String[] getValues() {
		return new String[] { path, FileUtils.byteCountToDisplaySize(size),
				"HTTP-URL", url };
	}

	@Override
	public int getProgress() {
		if (t != null) {
			return (int) ((t.getByteCount() / (double) size) * 100);
		} else {
			return 0;
		}
	}

}
