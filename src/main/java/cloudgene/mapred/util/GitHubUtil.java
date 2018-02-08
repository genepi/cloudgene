package cloudgene.mapred.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
		String url = "https://api.github.com/repos/" + repo.getUser() + "/" + repo.getRepo() + "/releases/latest";
		try {
	        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	        HttpGet request = new HttpGet(url);
	        request.addHeader("content-type", "application/json");
	        HttpResponse result = httpClient.execute(request);
	        String json = EntityUtils.toString(result.getEntity(), "UTF-8");
	        //"tag_name"
	        
	        JsonElement jelement = new JsonParser().parse(json);
	        JsonObject  jobject = jelement.getAsJsonObject();
	        return jobject.get("tag_name").getAsString();
	    } catch (IOException ex) {
	    	
	    	return null;
	    }
		
	}
}
