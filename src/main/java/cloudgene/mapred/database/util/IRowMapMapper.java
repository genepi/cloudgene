package cloudgene.mapred.database.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IRowMapMapper {

	public Object getRowKey(ResultSet rs, int row) throws SQLException;

	public Object getRowValue(ResultSet rs, int row) throws SQLException;

}
