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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

public class MapHandler implements ResultSetHandler<Object> {

	private IRowMapMapper mapper;

	public MapHandler(IRowMapMapper rowMapper) {
		this.mapper = rowMapper;
	}

	public Map<Object, Object> toBeanList(ResultSet rs) throws SQLException {
		Map<Object, Object> result = new HashMap<Object, Object>();

		int row = 0;
		while (rs.next()) {
			Object key = mapper.getRowKey(rs, row);
			Object value = mapper.getRowValue(rs, row);
			if (value != null) {
				result.put(key, value);
			}
			row++;
		}

		return result;
	}

	@Override
	public Object handle(ResultSet rs) throws SQLException {
		return toBeanList(rs);
	}

}
