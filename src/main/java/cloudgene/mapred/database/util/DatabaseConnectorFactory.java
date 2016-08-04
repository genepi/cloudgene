package cloudgene.mapred.database.util;

import genepi.db.DatabaseConnector;
import genepi.db.h2.H2Connector;
import genepi.db.mysql.MySqlConnector;

import java.util.Map;

public class DatabaseConnectorFactory {

	public static DatabaseConnector createConnector(Map<String, String> settings) {

		String driver = settings.get("driver");

		if (driver == null){
			return null;
		}
		
		if (driver.equals("h2")) {

			String database = settings.get("database");
			String user = settings.get("user");
			String password = settings.get("password");

			return new H2Connector(database, user, password, false);

		} else if (driver.equals("mysql")) {

			String host = settings.get("host");
			String port = settings.get("port");
			String database = settings.get("database");
			String user = settings.get("user");
			String password = settings.get("password");

			return new MySqlConnector(host, port, database, user, password);

		} else {

			return null;

		}
	}

}
