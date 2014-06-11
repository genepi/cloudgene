package cloudgene.mapred.database;

import genepi.io.FileUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.Main;

public class H2Connector {

	protected static final Log log = LogFactory.getLog(H2Connector.class);

	private Connection connection;

	private String path;
	private String user;
	private String password;
	private boolean multiuser = false;
	private boolean exists = false;

	private static H2Connector instance = null;

	public static H2Connector getInstance() {
		if (instance == null)
			instance = new H2Connector("data/mapred", "mapred", "mapred", false);
		return instance;
	}

	public H2Connector(String path, String user, String password,
			boolean multiuser) {
		this.path = path;
		this.user = user;
		this.password = password;
		this.multiuser = multiuser;
	}

	public boolean createBackup(String folder) {

		File file = new File(path + ".h2.db");
		boolean exists = file.exists();

		if (exists) {

			FileUtil.copyDirectory(file.getParent(), folder);

		}

		log.info("Created backup file " + folder);

		return true;

	}

	public void connect() throws SQLException {

		File file = new File(path + ".h2.db");
		exists = file.exists();

		log.debug("Establishing connection to " + user + "@" + path);

		try {

			Class.forName("org.h2.Driver");

		} catch (ClassNotFoundException e) {

			log.error("H2 Driver Class not found", e);

		}
		if (multiuser) {
			log.debug("Running database in multiuser-mode...");
			connection = DriverManager.getConnection("jdbc:h2:" + path
					+ ";AUTO_SERVER=TRUE", user, password);
		} else {
			log.debug("Running database in single-mode...");
			connection = DriverManager.getConnection("jdbc:h2:" + path, user,
					password);

		}
		connection.setAutoCommit(true);

		if (!exists) {
			createSchema();
		}

	}

	public void disconnect() throws SQLException {
		connection.close();
		connection = null;

	}

	public Connection getConnection() {
		return connection;
	}

	protected void createSchema() {
		try {
			log.debug("Creating tables...");
			executeSQL("/create-tables.sql");

			log.debug("Create schema succesfull");
		} catch (Exception e) {
			log.debug("Create schema failed", e);
		}
	}

	public boolean isNewDatabase() {
		return !exists;
	}

	public void executeSQL(String filename) throws SQLException, IOException,
			URISyntaxException {

		String sqlContent = readFileAsString(filename);

		PreparedStatement ps = connection.prepareStatement(sqlContent);
		ps.executeUpdate();
	}

	public static String readFileAsString(String filename)
			throws java.io.IOException, URISyntaxException {

		InputStream is = Main.class.getResourceAsStream(filename);

		DataInputStream in = new DataInputStream(is);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuilder builder = new StringBuilder();
		while ((strLine = br.readLine()) != null) {
			builder.append("\n");
			builder.append(strLine);
		}

		in.close();

		return builder.toString();
	}

}
