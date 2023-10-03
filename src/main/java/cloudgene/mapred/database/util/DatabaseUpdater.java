/*******************************************************************************
 * Copyright (C) 2009-2016 Lukas Forer and Sebastian Schönherr
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package cloudgene.mapred.database.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import genepi.io.FileUtil;

public class DatabaseUpdater {

	protected static final Log log = LogFactory.getLog(DatabaseUpdater.class);

	private DatabaseConnector connector;

	private Database database;

	private String oldVersion;

	private String currentVersion;

	private String filename;

	private InputStream updateFileAsStream;

	private boolean needUpdate = false;

	private Map<String, IUpdateListener> listeners = new HashMap<String, IUpdateListener>();

	public DatabaseUpdater(Database database, String filename, InputStream updateFileAsStream, String currentVersion) {

		this.filename = filename;
		this.database = database;
		this.connector = database.getConnector();
		this.updateFileAsStream = updateFileAsStream;
		this.currentVersion = currentVersion;

		if (isVersionTableAvailable(database)) {

			oldVersion = readVersionDB();
			log.info("Read current DB version: " + oldVersion);

			// should not happen, since an entry is created when metadata table
			// exists
			if (oldVersion == null) {
				oldVersion = readVersion(filename);
				log.info("Read curent version from DB was not successful, read it from file: " + oldVersion);
			}

		} else {
			// check also file for backwards compatibility
			oldVersion = readVersion(filename);
			log.info("Read current version from file: " + oldVersion);
		}
		log.info("Current app version: " + currentVersion);
		needUpdate = (compareVersion(currentVersion, oldVersion) > 0);

	}

	public void addUpdate(String version, IUpdateListener listener) {
		listeners.put(version, listener);
	}

	public boolean updateDB() {

		if (needUpdate()) {
			log.info("Database needs update...");
			if (!update()) {
				log.error("Updating database failed.");
				try {
					database.disconnect();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
			log.info("Update database done.");
		} else {
			log.info("Database is already up-to-date.");
			if (!isVersionTableAvailable(database)) {
				writeVersion(currentVersion);
			}
		}

		String dbVersion = readVersionDB();
		if (!dbVersion.equals(currentVersion)) {
			log.error("App version (v" + currentVersion + ") and DB version (v" + dbVersion
					+ ") does not match. Update Application to latest version.");
			return false;
		}

		return true;
	}

	public boolean update() {
		if (needUpdate) {

			log.info("Updating database from " + oldVersion + " to " + currentVersion + "...");

			try {
				readAndPrepareSqlClasspath(updateFileAsStream, oldVersion, currentVersion);
			} catch (IOException | URISyntaxException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// check if DB version match with Main version
			if (isVersionTableAvailable(database)) {
				String currentDBVersion = readVersionDB();
				if ((compareVersion(currentVersion, currentDBVersion) > 0)) {
					writeVersion(currentVersion);
				}
			} else {
				writeVersion(currentVersion);
			}

			log.info("Updating database was successful.");

		}

		return true;

	}

	public boolean needUpdate() {
		return needUpdate;
	}

	public void writeVersion(String newVersion) {

		try {

			if (!isVersionTableAvailable(database)) {
				createVersionTable(database);
			}

			Connection connection = connector.getDataSource().getConnection();
			PreparedStatement ps = connection.prepareStatement("INSERT INTO database_versions (version) VALUES (?)");
			ps.setString(1, newVersion);
			ps.executeUpdate();
			log.info("Version in DB updated to: " + newVersion);

			if (new File(filename).exists()) {
				FileUtil.deleteFile(filename);
				log.info("Deleted version.txt on file system.");
			}

			connection.close();

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public String readVersion(String versionFile) {

		File file = new File(versionFile);

		if (file.exists()) {

			try {

				return readFileAsString(versionFile);

			} catch (Exception e) {

				return "0.0.0";

			}

		} else {

			return "0.0.0";

		}

	}

	public String readVersionDB() {

		String version = null;

		try {
			Connection connection = connector.getDataSource().getConnection();
			PreparedStatement ps = connection.prepareStatement(
					"select version from database_versions where updated_on = (SELECT MAX(updated_on) from database_versions) "
							+ "order by updated_on, id DESC");
			ResultSet result = ps.executeQuery();

			if (result.next()) {
				version = result.getString(1);
			}

			connection.close();

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return version;
	}

	public static String readFileAsString(String filename) throws java.io.IOException, URISyntaxException {

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

	public String readAndPrepareSqlClasspath(InputStream filestream, String minVersion, String maxVersion)
			throws java.io.IOException, URISyntaxException, SQLException {

		DataInputStream in = new DataInputStream(filestream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuilder builder = new StringBuilder();
		boolean reading = false;
		String version = null;

		while ((strLine = br.readLine()) != null) {

			if (strLine.startsWith("--")) {

				if (builder.length() > 0) {
					executeSQLFile(builder.toString(), version);
					builder.setLength(0);
					IUpdateListener listener = listeners.get(version);
					if (listener != null) {
						listener.afterUpdate(database);
					}
				}

				version = strLine.replace("--", "").trim();
				reading = (compareVersion(version, minVersion) > 0 && compareVersion(version, maxVersion) <= 0);
				if (reading) {
					log.info("Loading SQL update for version " + version);
					IUpdateListener listener = listeners.get(version);
					if (listener != null) {
						listener.beforeUpdate(database);
					}
				}

			} else if (reading && !strLine.trim().isEmpty()) {
				builder.append("\n");
				builder.append(strLine);
			}
		}

		// last block
		executeSQLFile(builder.toString(), version);

		in.close();

		return builder.toString();

	}

	public void executeSQLFile(String sqlContent, String version) throws SQLException {

		if (sqlContent.length() > 0) {
			Connection connection;
			connection = connector.getDataSource().getConnection();
			PreparedStatement ps = connection.prepareStatement(sqlContent);
			ps.executeUpdate();
			connection.close();
			log.info("DB SQL Update " + version + " finished");
			writeVersion(version);
		}

	}

	public static int compareVersion(String version1, String version2) {

		String parts1[] = version1.split("-", 2);
		String parts2[] = version2.split("-", 2);

		String tiles1[] = parts1[0].split("\\.");
		String tiles2[] = parts2[0].split("\\.");

		for (int i = 0; i < tiles1.length; i++) {
			int number1 = Integer.parseInt(tiles1[i].trim());
			int number2 = Integer.parseInt(tiles2[i].trim());

			if (number1 != number2) {

				return number1 > number2 ? 1 : -1;

			}

		}

		if (parts1.length > 1) {
			if (parts2.length > 1) {
				return parts1[1].compareTo(parts2[1]);
			} else {
				return -1;
			}
		} else {
			if (parts2.length > 1) {
				return 1;
			}
		}

		return 0;

	}

	public boolean isVersionTableAvailable(Database database) {
		try {
			return database.getConnector().existsTable("database_versions");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public void createVersionTable(Database database) {
		try {
			Connection connection = connector.getDataSource().getConnection();
			String statement = "create table database_versions ( \r\n"
					+ "	id          integer not null auto_increment primary key,\r\n"
					+ "	version	varchar(255) not null,\r\n"
					+ "    updated_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP \r\n" + ")";
			PreparedStatement ps = connection.prepareStatement(statement);
			ps.executeUpdate();
			connection.close();
			log.info("Table database_versions created.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
