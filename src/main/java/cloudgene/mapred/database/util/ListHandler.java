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
import java.util.List;
import java.util.Vector;

import org.apache.commons.dbutils.ResultSetHandler;

public class ListHandler implements ResultSetHandler<Object> {

	private IRowMapper mapper;

	public ListHandler(IRowMapper rowMapper) {
		this.mapper = rowMapper;
	}

	public List<Object> toBeanList(ResultSet rs) throws SQLException {
		List<Object> result = new Vector<Object>();

		int row = 0;
		while (rs.next()) {
			Object value = mapper.mapRow(rs, row);
			if (value != null) {
				result.add(value);
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
