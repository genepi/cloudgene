package cloudgene.mapred.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import cloudgene.mapred.wdl.WdlApp;
import cloudgene.mapred.wdl.WdlHeader;
import cloudgene.mapred.wdl.WdlReader;

import com.esotericsoftware.yamlbeans.YamlReader;

public class Repository {

	private String url;

	private AppList appList;

	private List<WdlApp> apps;

	public Repository(String url) {
		this.url = url;
	}

	public boolean load() throws IOException {

		apps = new Vector<WdlApp>();

		YamlReader reader = null;
		try {
			URL url2 = new URL(url + "/apps.yaml");
			HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
			reader = new YamlReader(
					new InputStreamReader(conn.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		appList = reader.read(AppList.class);

		for (String pckg : appList.getApps()) {
			WdlApp app = WdlReader.loadAppFromUrl(url + "/" + pckg
					+ "/cloudgene.yaml");
			// update url
			app.setSource(url + "/" + pckg);
			apps.add(app);
		}

		return true;

	}

	public List<WdlApp> getApps() {
		return apps;
	}

	public static void main(String[] args) throws IOException {
		Repository repo = new Repository("http://cloudgene.uibk.ac.at/apps");
		repo.load();
		for (WdlHeader app : repo.getApps()) {
			System.out.println(app.getName());
		}

	}

}
