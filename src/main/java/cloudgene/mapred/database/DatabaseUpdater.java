package cloudgene.mapred.database;

import genepi.io.FileUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.Main;

public class DatabaseUpdater {

	protected static final Log log = LogFactory.getLog(DatabaseUpdater.class);

	private H2Connector connector;

	private String oldVersion;

	private String currentVersion;

	private String filename;

	private String updateFile;

	private boolean needUpdate = false;

	private boolean localFile = false;

	public DatabaseUpdater(H2Connector connector, String filename,
			String updateFile, String currentVersion, boolean localFile) {

		this.connector = connector;
		this.filename = filename;
		this.updateFile = updateFile;
		this.currentVersion = currentVersion;
		this.localFile = localFile;

		oldVersion = readVersion(filename);

		needUpdate = (compareVersion(currentVersion, oldVersion) > 0);

	}

	public boolean update() {

		if (needUpdate) {

			try {

				log.info("Updating database from " + oldVersion + " to "
						+ currentVersion + "....");

				if (localFile) {
					if (new File(updateFile).exists()) {
						executeSQLFile(updateFile, oldVersion, currentVersion);
					}else{
						log.error("File not found " + updateFile + "....");
					
					}
				} else {
					executeSQLClasspath(updateFile, oldVersion, currentVersion);
				}

				log.info("Updating database successful.");

				FileUtil.writeStringBufferToFile(filename, new StringBuffer(
						currentVersion));

			} catch (SQLException e) {

				log.error("Updating database failed.", e);
				return false;

			} catch (IOException e) {

				log.error("Updating database failed.", e);
				return false;

			} catch (URISyntaxException e) {

				log.error("Updating database failed.", e);
				return false;

			}

		}

		return true;

	}

	public boolean needUpdate() {
		return needUpdate;
	}

	public String readVersion(String versionFile) {

		File file = new File(versionFile);

		if (file.exists()) {

			try {

				return readFileAsString(versionFile);

			} catch (Exception e) {

				return "1.0.0";

			}

		} else {

			return "1.0.0";

		}

	}

	public static String readFileAsString(String filename)
			throws java.io.IOException, URISyntaxException {

		InputStream is = new FileInputStream(filename);

		DataInputStream in = new DataInputStream(is);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuilder builder = new StringBuilder();
		while ((strLine = br.readLine()) != null) {
			// builder.append("\n");
			builder.append(strLine);
		}

		in.close();

		return builder.toString();
	}

	public void executeSQLFile(String filename, String minVersion,
			String maxVersion) throws SQLException, IOException,
			URISyntaxException {

		String sqlContent = readFileAsStringFile(filename, minVersion,
				maxVersion);

		PreparedStatement ps = connector.getConnection().prepareStatement(
				sqlContent);
		ps.executeUpdate();
	}

	public static String readFileAsStringFile(String filename,
			String minVersion, String maxVersion) throws java.io.IOException,
			URISyntaxException {

		InputStream is = new FileInputStream(filename);

		DataInputStream in = new DataInputStream(is);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuilder builder = new StringBuilder();
		boolean reading = false;
		while ((strLine = br.readLine()) != null) {

			if (strLine.startsWith("--")) {

				String version = strLine.replace("--", "");
				reading = (compareVersion(version, minVersion) > 0 && compareVersion(
						version, maxVersion) <= 0);
				if (reading) {
					log.info("Loading update for version " + version);
				}

			}

			if (reading) {
				builder.append("\n");
				builder.append(strLine);
			}
		}

		in.close();

		return builder.toString();
	}

	public void executeSQLClasspath(String filename, String minVersion,
			String maxVersion) throws SQLException, IOException,
			URISyntaxException {

		String sqlContent = readFileAsStringClasspath(filename, minVersion,
				maxVersion);

		PreparedStatement ps = connector.getConnection().prepareStatement(
				sqlContent);
		ps.executeUpdate();
	}

	public static String readFileAsStringClasspath(String filename,
			String minVersion, String maxVersion) throws java.io.IOException,
			URISyntaxException {

		InputStream is = Main.class.getResourceAsStream(filename);

		DataInputStream in = new DataInputStream(is);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuilder builder = new StringBuilder();
		boolean reading = false;
		while ((strLine = br.readLine()) != null) {

			if (strLine.startsWith("--")) {

				String version = strLine.replace("--", "");
				reading = (compareVersion(version, minVersion) > 0 && compareVersion(
						version, maxVersion) <= 0);

				if (reading) {
					log.info("Loading update for version " + version);
				}

			}

			if (reading) {
				builder.append("\n");
				builder.append(strLine);
			}
		}

		in.close();

		return builder.toString();
	}

	public static int compareVersion(String version1, String version2) {

		String tiles1[] = version1.split("\\.");
		String tiles2[] = version2.split("\\.");

		for (int i = 0; i < tiles1.length; i++) {
			int number1 = Integer.parseInt(tiles1[i]);
			int number2 = Integer.parseInt(tiles2[i]);

			if (number1 != number2) {

				return number1 > number2 ? 1 : -1;

			}

		}

		return 0;

	}

}
