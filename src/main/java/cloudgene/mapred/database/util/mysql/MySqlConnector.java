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

package cloudgene.mapred.database.util.mysql;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.database.util.AbstractDatabaseConnector;

public class MySqlConnector extends AbstractDatabaseConnector {

	private static final Log log = LogFactory.getLog(MySqlConnector.class);

	private BasicDataSource dataSource;

	private String host;
	private String port;
	private String database;
	private String user;
	private String password;

	public MySqlConnector(String host, String port, String database, String user, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.user = user;
		this.password = password;
	}

	@Override
	public void connect() throws SQLException {

		log.debug("Establishing connection to " + user + "@" + host + ":" + port);

		if (DbUtils.loadDriver("com.mysql.jdbc.Driver")) {
			try {
				dataSource = createDataSource();

				dataSource.setDriverClassName("com.mysql.jdbc.Driver");
				dataSource.setUrl("jdbc:mysql://" + host + "/" + database
						+ "?autoReconnect=true&allowMultiQueries=true&rewriteBatchedStatements=true");
				dataSource.setUsername(user);
				dataSource.setPassword(password);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("MySQL Driver class not found.");
		}

	}

	@Override
	public void disconnect() throws SQLException {
		dataSource.close();

	}

	@Override
	public BasicDataSource getDataSource() {
		return dataSource;
	}

	public void executeSQL(InputStream is) throws SQLException, IOException, URISyntaxException {

		String sqlContent = readFileAsString(is);
		if (!sqlContent.isEmpty()) {
			Connection connection = dataSource.getConnection();
			PreparedStatement ps = connection.prepareStatement(sqlContent);
			ps.executeUpdate();
			connection.close();
		}
	}

	public static String readFileAsString(InputStream is) throws java.io.IOException, URISyntaxException {

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

	@Override
	public String getSchema() {
		return database;
	}

	@Override
	public boolean existsTable(String table) throws SQLException {
		Connection connection = dataSource.getConnection();
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet res = meta.getTables(database, null, table, new String[] { "TABLE" });
		boolean exists = res.next();
		res.close();
		connection.close();
		if (!exists) {
			log.warn("Table '" + table + "' not found in schema '" + database + "'");
		}
		return exists;
	}

}
