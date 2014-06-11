package cloudgene.mapred.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Dao {

	protected Connection connection = H2Connector.getInstance().getConnection();

	protected int update(String sql, Object[] params) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);

		for (int i = 0; i < params.length; i++) {
			statement.setObject(i + 1, params[i]);
		}

		return statement.executeUpdate();
	}

	protected int updateAndGetKey(String sql, Object[] params)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql,
				PreparedStatement.RETURN_GENERATED_KEYS);

		for (int i = 0; i < params.length; i++) {
			statement.setObject(i + 1, params[i]);
		}

		int id = 0;

		statement.executeUpdate();

		ResultSet rs = statement.getGeneratedKeys();
		rs.next();
		id = (int) rs.getLong(1);

		rs.close();

		return id;
	}

	protected ResultSet query(String sql) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);

		return statement.executeQuery();
	}

	protected ResultSet query(String sql, Object[] params) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);

		for (int i = 0; i < params.length; i++) {
			statement.setObject(i + 1, params[i]);
		}
		return statement.executeQuery();
	}
}
