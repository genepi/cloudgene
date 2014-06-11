package cloudgene.mapred.steps.importer;

import java.util.List;
import java.util.Vector;

public class ImporterFactory {

	public static boolean needsImport(String url) {
		return url.startsWith("sftp://") || url.startsWith("http://")
				|| url.startsWith("https://") || url.startsWith("ftp://");
	}

	public static IImporter createImporter(String url, String target) {

		if (url.startsWith("sftp://")) {
			return new ImporterSftp(url, target);
		}

		if (url.startsWith("http://") || url.startsWith("https://")) {
			return new ImporterHttp(url, target);
		}

		if (url.startsWith("ftp://")) {
			return new ImporterFtp(url, target);
		}

		return null;
	}

	public static List<String> parseImportString(String input) {

		List<String> results = new Vector<String>();

		String[] urlList = input.split(";")[0].split("\\s+");

		String username = "";
		if (input.split(";").length > 1) {
			username = input.split(";")[1];
		}

		String password = "";
		if (input.split(";").length > 2) {
			password = input.split(";")[2];
		}

		for (String url2 : urlList) {

			String url = url2 + ";" + username + ";" + password;
			results.add(url);
		}

		return results;

	}

}
