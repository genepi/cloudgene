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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

public abstract class JdbcDataAccessObject {

	protected QueryRunner runner = null;

	protected Database database;

	public JdbcDataAccessObject(Database database) {
		this.database = database;
		runner = new QueryRunner(database.getDataSource());
	}

	/*
	 * protected Connection getConnection() { return database.getConnection(); }
	 */

	public Object queryForObject(String sql, Object[] params, IRowMapper mapper)
			throws SQLException {
		return runner.query(sql, new ObjectHandler(mapper), params);
	}

	@SuppressWarnings("rawtypes")
	public List query(String sql, Object[] params, IRowMapper mapper)
			throws SQLException {
		return (List) runner.query(sql, new ListHandler(mapper), params);
	}

	public Object queryForObject(String sql, IRowMapper mapper)
			throws SQLException {
		return runner.query(sql, new ObjectHandler(mapper));
	}

	@SuppressWarnings("rawtypes")
	public Map queryForMap(String sql, IRowMapMapper mapper)
			throws SQLException {
		return (Map) runner.query(sql, new MapHandler(mapper));
	}

	@SuppressWarnings("rawtypes")
	public Map queryForMap(String sql, Object[] params, IRowMapMapper mapper)
			throws SQLException {
		return (Map) runner.query(sql, new MapHandler(mapper), params);
	}

	@SuppressWarnings("rawtypes")
	public Map queryForGroupedList(String sql, Object[] params,
			IRowMapMapper mapper) throws SQLException {
		return (Map) runner.query(sql, new GroupedListHandler(mapper), params);
	}

	@SuppressWarnings("rawtypes")
	public Map queryForGroupedList(String sql, IRowMapMapper mapper)
			throws SQLException {
		return (Map) runner.query(sql, new GroupedListHandler(mapper));
	}

	@SuppressWarnings("rawtypes")
	public List query(String sql, IRowMapper mapper) throws SQLException {
		return (List) runner.query(sql, new ListHandler(mapper));
	}

	public int update(String sql, Object[] params) throws SQLException {
		return runner.update(sql.toString(), params);
	}

	public int update(String sql) throws SQLException {
		return runner.update(sql.toString());
	}

	public int insert(String sql, Object[] params) throws SQLException {

		Connection connection = database.getDataSource().getConnection();

		try {
			PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

			runner.fillStatement(statement, params);

			statement.executeUpdate();

			ResultSet rs = statement.getGeneratedKeys();
			rs.beforeFirst();
			rs.next();
			int id = rs.getInt(1);
			connection.close();
			return id;
		} catch (Exception e) {
			throw e;
		} finally {
			connection.close();
		}
	}


	public int[] batch(String sql, Object[][] params) throws SQLException {

		return runner.batch(sql.toString(), params);
	}

	// DBUtils 1.6 method
	public List<Integer> batchGeneratedKeys(String sql, Object[][] params)
			throws SQLException {

		ResultSetHandler<List<Integer>> handler = new ResultSetHandler<List<Integer>>() {

			public List<Integer> handle(ResultSet rs) throws SQLException {
				List<Integer> identifiers = new ArrayList<Integer>();

				while (rs.next()) {
					identifiers.add(rs.getInt(1));
				}

				return identifiers;
			}
		};

		return runner.insertBatch(sql, handler, params);
	}

	public boolean callProcedure(String sql, Object[] params) throws SQLException {

		Connection connection = null;
		
		try {
			connection = database.getDataSource().getConnection();

			CallableStatement cstmt = connection.prepareCall(sql);
			runner.fillStatement(cstmt, params);
			boolean state = cstmt.execute();
			cstmt.close();
			connection.close();
			return state;
		} catch (SQLException e) {
			throw e;
		} finally {
			connection.close();
		}
	}


	public static class IntegerMapper implements IRowMapper {

		@Override
		public Object mapRow(ResultSet rs, int row) throws SQLException {
			return rs.getInt(1);
		}

	}

}
