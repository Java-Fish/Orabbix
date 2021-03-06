/*
 * Copyright (C) 2010 Andrea Dalle Vacche.
 * 
 * This file is part of orabbix.
 *
 * orabbix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * orabbix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * orabbix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.smartmarmot.common.db;
public class DBConn {
	private DBConnManager dbcm;
	private String dbname;
	public DBConn(DBConnManager _dbcm, String _name) {
		if (_dbcm == null)
			throw new RuntimeException("empty datasource");
		this.dbcm = _dbcm;
		if (_name != null && _name.length() > 0)
			this.dbname = _name;
		else
			this.dbname = "undefined";
	}


	public String getName() {
		return this.dbname;
	}
	
	

	public DBConnManager getDBCM() {
		return this.dbcm;
	}

}