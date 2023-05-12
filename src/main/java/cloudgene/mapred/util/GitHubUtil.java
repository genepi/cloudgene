package cloudgene.mapred.util;

import static io.micronaut.http.HttpHeaders.ACCEPT;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.netty.DefaultHttpClient;
import reactor.core.publisher.Mono;;

public class GitHubUtil {

	public static class Repository {
		private String user;

		private String repo;

		private String tag;

		private String directory;

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getRepo() {
			return repo;
		}

		public void setRepo(String repo) {
			this.repo = repo;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public String getDirectory() {
			return directory;
		}

		public void setDirectory(String directory) {
			this.directory = directory;
		}

	}

	public static Repository parseShorthand(String shorthand) {
		Repository repo = new Repository();
		// username/repo[/subdir][@ref]
		String[] tiles2 = shorthand.split("@");

		String[] tiles = tiles2[0].split("/", 3);
		if (tiles.length < 2) {
			return null;
		}
		repo.setUser(tiles[0]);
		repo.setRepo(tiles[1]);
		if (tiles.length > 2) {
			repo.setDirectory(tiles[2]);
		}

		if (tiles2.length == 2) {
			repo.setTag(tiles2[1]);
		} else if (tiles2.length > 2) {
			return null;
		}

		return repo;
	}

	public static String buildUrlFromRepository(Repository repo) {
		String tag = repo.getTag();
		if (tag != null && tag.equalsIgnoreCase("latest")) {
			// get latest release tag
			tag = getLatestReleaseFromRepository(repo);
			if (tag == null) {
				return null;
			}
		}

		String url = "https://api.github.com/repos/" + repo.getUser() + "/" + repo.getRepo() + "/zipball";
		if (tag != null) {
			url += "/" + tag;
		}
		return url;
	}

	public static String getLatestReleaseFromRepository(Repository repo) {

		String urlString = "https://api.github.com/repos/" + repo.getUser() + "/" + repo.getRepo() + "/releases/latest";

		HttpClient httpClient = new DefaultHttpClient();

		try {
			URL url = new URL(urlString);
			URLConnection request = url.openConnection();
			request.setRequestProperty("content-type", "application/json");
			request.connect();

			HttpRequest<?> req = HttpRequest.GET(url.toURI()).header(USER_AGENT, "Cloudgene").header(ACCEPT,
					"application/vnd.github.v3+json, application/json");
			String tag = Mono.from(httpClient.retrieve(req, Map.class)).block().get("tag_name").toString();
			httpClient.close();

			return tag;
		} catch (Exception e) {
			httpClient.close();
			return null;
		}

	}

	public static String readFullyAsString(InputStream inputStream, String encoding) throws IOException {
		return readFully(inputStream).toString(encoding);
	}

	private static ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos;
	}
}
