package cloudgene.mapred.database.util;

import java.util.Map;

import cloudgene.mapred.database.util.h2.H2Connector;
import cloudgene.mapred.database.util.mysql.MySqlConnector;

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
