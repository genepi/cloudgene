/*******************************************************************************
 * CONAN: Copy Number Variation Analysis Software for
 * Next Generation Genome-Wide Association Studies
 * 
 * Copyright (C) 2009, 2010 Lukas Forer
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Database
 * 
 * @author Lukas Forer
 * 
 */
public class Database {

	private static final Log log = LogFactory.getLog(Database.class);

	private DatabaseConnector connector;

	public Database() {
	}

	public void connect(DatabaseConnector connector) throws SQLException {
		this.connector = connector;
		try {

			connector.connect();

			log.debug("Establish connection successful");

			fireChangeEvent(DatabaseListener.AFTER_CONNECTION);
		} catch (SQLException e) {
			log.error("Establish connection failed", e);
			throw e;
		}
	}

	public void disconnect() throws SQLException {

		if (connector != null) {

			if (connector.getConnection() != null) {

				log.debug("Disconnecting");

				fireChangeEvent(DatabaseListener.BEFORE_DISCONNECTION);
				try {

					connector.disconnect();

					log.debug("Disconnection successful");
					fireChangeEvent(DatabaseListener.AFTER_DISCONNECTION);
				} catch (SQLException e) {
					log.error("Disconnection failed", e);
					throw e;
				}
			}

		}
	}

	public boolean isConnected() {
		if (connector != null) {
			return connector.getConnection() != null;
		} else {
			return false;
		}
	}

	public Connection getConnection() {
		return connector.getConnection();
	}

	/*
	 * ListenerSupport
	 */

	private Vector<DatabaseListener> listeners = new Vector<DatabaseListener>();

	public void addDatabaseListener(DatabaseListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeDatabaseListener(DatabaseListener listener) {
		listeners.remove(listener);
	}

	private void fireChangeEvent(int event) {
		Iterator<DatabaseListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			(iter.next()).onDatabaseEvent(event);
		}
	}

}
