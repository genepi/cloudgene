package cloudgene.mapred.database.util;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseConnector {

	public void connect() throws SQLException;

	public void disconnect() throws SQLException;

	public Connection getConnection();

}
