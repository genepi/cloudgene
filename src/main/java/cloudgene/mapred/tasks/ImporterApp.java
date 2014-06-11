package cloudgene.mapred.tasks;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import cloudgene.mapred.util.FileUtil;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.util.ZipUtil;

public class ImporterApp extends AbstractTask {

	private String url;

	public ImporterApp(String url) {
		this.url = url;

		String[] tiles = url.split("/");
		String name = tiles[tiles.length - 1];

		setName("install-" + name);

	}

	@Override
	public boolean execute() {

		String localFile = FileUtil.getTempFilename(url);
		try {

			String[] tiles = url.split("/");
			String name = tiles[tiles.length - 1];

			setName("install-" + name);

			downloadPackage(url + "/app.zip", localFile);
			installPackage(localFile, name);

			downloadManifest(url + "/cloudgene.yaml", name);

			return true;
		} catch (IOException e) {
			writeOutput(e.getLocalizedMessage());
			return false;
		}

	}

	public static boolean downloadPackage(String weburl, String localFile)
			throws IOException {

		URL url = new URL(weburl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

		FileOutputStream out = new FileOutputStream(localFile);

		IOUtils.copy(in, out);

		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);

		return true;
	}

	public static boolean downloadManifest(String weburl, String name)
			throws IOException {

		String apps = Settings.getInstance().getAppsPath();

		String target = FileUtil.path(apps, name, "cloudgene.yaml");

		URL url = new URL(weburl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

		FileOutputStream out = new FileOutputStream(target);

		IOUtils.copy(in, out);

		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);

		return true;
	}

	public static boolean installPackage(String localFile, String name) {

		String tools = Settings.getInstance().getAppsPath();
		ZipUtil.extract(localFile, FileUtil.path(tools, name));

		return true;
	}

	@Override
	public String[] getParameters() {
		return new String[] { "URL" };
	}

	@Override
	public String[] getValues() {
		return new String[] { url };
	}

}
