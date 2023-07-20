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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public interface DatabaseConnector {

	public void connect() throws SQLException;

	public void disconnect() throws SQLException;

	public BasicDataSource getDataSource();

	public void executeSQL(InputStream is) throws SQLException, IOException,
			URISyntaxException;

	public String getSchema();

	boolean existsTable(String table) throws SQLException;
}

