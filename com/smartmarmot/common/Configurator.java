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


package com.smartmarmot.common;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang3.ArrayUtils;
import java.util.logging.Level;

import com.smartmarmot.common.db.DBConn;
import com.smartmarmot.common.db.DBConnManager;
import com.smartmarmot.common.db.DBcfg;
import com.smartmarmot.common.enquiry.Query;
import com.smartmarmot.common.enquiry.Querybox;

public class Configurator {
	private static Properties _props;

	private static void verifyConfig() {
		if (_props == null ) {
			throw new IllegalArgumentException("empty properties");
		}

	}


	public static Properties getPropsFromFile(String _filename) {
		try {
			verifyConfig();
			Properties propsq = new Properties();
			FileInputStream fisq;
			if (_filename != "" && _filename != null) {
				File queryFile = new File(_filename);
				fisq = new FileInputStream(new java.io.File(queryFile
						.getAbsoluteFile().getCanonicalPath()));
				queryFile = null;
				SmartLogger.logThis(Level.CONFIG, "Loaded the properties from " + _filename);
				propsq.load(fisq);
				fisq.close();
				return propsq;
			}
		} catch (Exception e) {
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator reading properties file getPropsFromFile("
					+ _filename + ") " + e.getMessage());
			return null;
		}
		return null;
	}

	public static Query[] getQueries(Properties _propsq) throws Exception {
		return getQueries(Constants.QUERY_LIST, _propsq);
	}

	
	private static Query[] getQueries(String parameter, Properties _propsq)
			throws Exception {
		try {
			StringTokenizer stq = new StringTokenizer(_propsq
					.getProperty(parameter), Constants.DELIMITER);
			String[] QueryLists = new String[stq.countTokens()];
			int count = 0;
			while (stq.hasMoreTokens()) {
				String token = stq.nextToken().toString().trim();
				QueryLists[count] = token;
				count++;
			}

			Collection<Query> Queries = new ArrayList<Query>();
			for (int i = 0; i < QueryLists.length; i++) {
				try {
					Query q = getQueryProperties(_propsq, QueryLists[i]);
					Queries.add(q);
				}catch (Exception e1){
					SmartLogger.logThis(Level.SEVERE, "Error on Configurator on reading query "+QueryLists[i] +e1);
					SmartLogger.logThis(Level.INFO, "Query "+QueryLists[i] +" skipped due to error " +e1);
					
				}
			}
			Query[] queries = (Query[]) Queries.toArray(new Query[0]);
			return queries;
		} catch (Exception ex) {

			SmartLogger.logThis(Level.SEVERE, "Error on Configurator on reading properties file "+ _propsq.toString() +" getQueries("
					+ parameter + "," + _propsq.toString() + ") "
					+ ex.getMessage());
			return null;
		}

	}

	private static Query getQueryProperties(Properties _propsq,
			String _queryName) throws Exception {
		try {

			String query = "";
			try {
				query = new String(_propsq.getProperty(_queryName + "."
						+ Constants.QUERY_POSTFIX));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.SEVERE, "Error while getting " + _queryName + "."
						+ Constants.QUERY_POSTFIX + " " + ex.getMessage());
			}

			String noDataFound = "";
			try {
				noDataFound = new String(_propsq.getProperty(_queryName + "."
						+ Constants.QUERY_NO_DATA_FOUND));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName
						+ "." + Constants.QUERY_NO_DATA_FOUND
						+ " null or not present " + ex.getMessage());
			}
			String whenNotAlive = "";
			try {
				whenNotAlive = new String(_propsq.getProperty(_queryName + "."
						+ Constants.QUERY_WHEN_NOT_ALIVE));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName
						+ "." + Constants.QUERY_WHEN_NOT_ALIVE
						+ " null or not present " + ex.getMessage());
			}
			String raceCondQuery = "";
			try {
				raceCondQuery = new String(_propsq.getProperty(_queryName + "."
						+ Constants.RACE_CONDITION_QUERY));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName
						+ "." + Constants.RACE_CONDITION_QUERY
						+ " null or not present " + ex.getMessage());
			}
			String raceCondValue = "";
			try {
				raceCondValue = new String(_propsq.getProperty(_queryName + "."
						+ Constants.RACE_CONDITION_VALUE));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName
						+ "." + Constants.RACE_CONDITION_VALUE
						+ " null or not present " + ex.getMessage());
			}
			/**
			 * set Period if not defined period =2 min.
			 */
			int period = -1;
			try {
				period = new Integer(_propsq.getProperty(_queryName + "."
						+ Constants.QUERY_PERIOD));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName
						+ "." + Constants.QUERY_PERIOD
						+ " null or not present " + ex.getMessage());
				try {
					period = new Integer(_propsq.getProperty(Constants.QUERY_DEFAULT_PERIOD));
				}catch (Exception ex1) {
					SmartLogger.logThis(Level.CONFIG, "Note: " + Constants.QUERY_DEFAULT_PERIOD
							+ " null or not present using default values 2 min.");
					period = 2;
				}
			}
			
			Boolean active = true;
			try {
				String active_str = _propsq.getProperty(_queryName + "."
						+ Constants.QUERY_ACTIVE);
				if (active_str != null) {
					if (active_str.equalsIgnoreCase("false")) {
						active = false;
					}
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName
						+ "." + Constants.QUERY_ACTIVE
						+ " null or not present " + ex.getMessage());
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName + "."
						+ Constants.QUERY_ACTIVE
						+ " null or not present using default values TRUE");
			}

			Boolean trim = true;
			try {
				String trim_str = _propsq.getProperty(_queryName + "."
						+ Constants.QUERY_TRIM);
				if (trim_str != null) {
					if (trim_str.equalsIgnoreCase("false")) {
						trim = false;
					}
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName + "."
						+ Constants.QUERY_TRIM + " null or not present "
						+ ex.getMessage());
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName + "."
						+ Constants.QUERY_TRIM
						+ " null or not present using default values TRUE");
			}

			Boolean space = false;
			try {
				String space_str = _propsq.getProperty(_queryName + "."
						+ Constants.QUERY_SPACE);
				if (space_str != null) {
					if (space_str.equalsIgnoreCase("true")) {
						space = true;
					}
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName + "."
						+ Constants.QUERY_SPACE + " null or not present "
						+ ex.getMessage());
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName + "."
						+ Constants.QUERY_SPACE
						+ " null or not present using default values TRUE");
			}

			List<Integer> excludeColumns = new ArrayList<Integer>();
			try {
				String excludeColumnsList = new String(_propsq
						.getProperty(_queryName + "."
								+ Constants.QUERY_EXCLUDE_COLUMNS));

				StringTokenizer st = new StringTokenizer(excludeColumnsList,
						Constants.DELIMITER);
				while (st.hasMoreTokens()) {
					String token = st.nextToken().toString();
					Integer tmpInteger = new Integer(token);
					excludeColumns.add(tmpInteger);
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName + "."
						+ Constants.QUERY_EXCLUDE_COLUMNS + " error "
						+ ex.getMessage());
			}

			List<Integer> raceExcludeColumns = new ArrayList<Integer>();
			try {
				String excludeColumnsList = new String(_propsq
						.getProperty(_queryName + "."
								+ Constants.RACE_CONDITION_EXCLUDE_COLUMNS));

				StringTokenizer st = new StringTokenizer(excludeColumnsList,
						Constants.DELIMITER);
				while (st.hasMoreTokens()) {
					String token = st.nextToken().toString();
					Integer tmpInteger = new Integer(token);
					excludeColumns.add(tmpInteger);
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.CONFIG, "Note: " + _queryName + "."
						+ Constants.RACE_CONDITION_EXCLUDE_COLUMNS + " error "
						+ ex.getMessage());
			}
			
			
			
			Query q = new Query(query, _queryName, noDataFound,whenNotAlive , raceCondQuery,
					raceCondValue, period, active, trim, space, excludeColumns,
					raceExcludeColumns);

			return q;
		} catch (Exception ex) {

			SmartLogger.logThis(Level.SEVERE, "Error on Configurator on getQueryProperties("
					+ _propsq.toString() + ") " + ex.getMessage());
			return null;
		}
	}


	public Configurator(String _url) throws IOException {
		Properties props = new Properties();
		
		FileInputStream fis;
		try {
			try {
				File configFile = new File(_url);
				fis = new FileInputStream(new java.io.File(configFile
						.getAbsoluteFile().getCanonicalPath()));
				props.load(fis);
				fis.close();
			} catch (Exception e) {
				SmartLogger.logThis(Level.SEVERE,
						"Error on Configurator while retriving configuration file "
								+ _url + " " + e.getMessage());
			}
			_props = props;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator " + e.getMessage());
		}
	}

	private DBConn getDBConnManager(String dbName) throws Exception {
		try {
			verifyConfig();

			SmartLogger.logThis(Level.CONFIG, "getConnection for database " + dbName);
			
			DBcfg _dbcfg = new DBcfg(_props,dbName);
			
			DBConnManager dbcm= new DBConnManager(_dbcfg);
			
			
			SmartLogger.logThis( Level.INFO,"DB Pool created: " +dbName);
    		SmartLogger.logThis( Level.INFO, "URL=" + _dbcfg.getDbURI() );
    		SmartLogger.logThis( Level.INFO, "maxPoolSize=" + _dbcfg.getDbPoolMaxActive() );
    		SmartLogger.logThis( Level.INFO, "maxIdleSize=" + _dbcfg.getDbPoolMaxIdle());
    		SmartLogger.logThis( Level.INFO, "poolTimeout=" + _dbcfg.getDbPoolMaxWait() );
    		
			Connection con = null;
			con = dbcm.getDs().getConnection();
			PreparedStatement p_stmt = null;
			p_stmt = con.prepareStatement(_dbcfg.geDBType().getWhoAmIQuery());
			ResultSet rs = null;
			rs = p_stmt.executeQuery();
			String tempStr = new String("");
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			while (rs.next()) {
				for (int r = 1; r < numColumns + 1; r++) {
					tempStr = tempStr + rs.getObject(r).toString().trim();
				}
			}
			SmartLogger.logThis(Level.INFO, "Connected as " + tempStr);

			con.close();
			con = null;
			con = dbcm.getDs().getConnection();
			p_stmt = con.prepareStatement(_dbcfg.geDBType().getDbNameQuery());
			rs = p_stmt.executeQuery();
			rsmd = rs.getMetaData();
			numColumns = rsmd.getColumnCount();
			tempStr = "";
			while (rs.next()) {
				for (int r = 1; r < numColumns + 1; r++) {
					tempStr = tempStr + rs.getObject(r).toString().trim();
				}
			}
			SmartLogger.logThis(Level.INFO, "--------- on Database -> " + tempStr);
			con.close();
			con = null;
			DBConn mydbconn = new DBConn(dbcm, dbName.toString());
			return mydbconn;

		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator for database " + dbName
					+ " -->" + ex.getMessage());
			return null;
		}
	}

	public DBConn[] getConnections() throws Exception {
		try {
			verifyConfig();
			String[] DatabaseList = getDBList();
			Collection<DBConn> connections = new ArrayList<DBConn>();
			for (int i = 0; i < DatabaseList.length; i++) {
				DBConn dbc=this.getDBConnManager(DatabaseList[i]);
				if (dbc!=null){
					connections.add(dbc);
				}else {
	        		 SmartLogger.logThis(Level.INFO,"This Database "+DatabaseList[i]+" removed");       	
	        	}
			}
			// fis.close();
			DBConn[] connArray = (DBConn[]) connections.toArray(new DBConn[0]);
			return connArray;
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator getConnections "
					+ ex.getMessage());
			return null;
		}
	}

	public String[] getDBList() throws Exception {
		try {
			verifyConfig();
			String dblist = "";
			try {
				dblist = new String(_props
						.getProperty(Constants.DATABASES_LIST));
			} catch (Exception e) {
				SmartLogger.logThis(Level.SEVERE,
						"Error on Configurator while retriving the databases list "
								+ Constants.DATABASES_LIST + " " + e);
			}

			StringTokenizer st = new StringTokenizer(dblist,
					Constants.DELIMITER);
			String[] DatabaseList = new String[st.countTokens()];
			int count = 0;
			while (st.hasMoreTokens()) {
				String token = st.nextToken().toString();
				DatabaseList[count] = token;
				count++;
			}
			// fisdb.close();
			return DatabaseList;
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE,
					"Error on Configurator while retriving the databases list "
							+ Constants.DATABASES_LIST + " " + ex);
			return null;
		}
	}

	public Integer getMaxThread() throws Exception {
		try {
			verifyConfig();
			return new Integer(_props
					.getProperty(Constants.ORABBIX_DAEMON_THREAD));
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator while retriving the "
					+ Constants.ORABBIX_DAEMON_THREAD + " " + ex);
			return null;
		}
	}

	public String getPidFile() throws Exception {
		try {
			verifyConfig();
			String _pidfile = new String(_props
					.getProperty(Constants.ORABBIX_PIDFILE));
			SmartLogger.logThis(Level.INFO, "PidFile -> " + _pidfile);

			if (_pidfile == "") {
				SmartLogger.logThis(Level.SEVERE, "Error retrieving pidfile from " + _props);
			}
			return _pidfile;

		} catch (Exception ex) {

			SmartLogger.logThis(Level.SEVERE, "Error on Configurator getPidFile " + ex);
			return null;
		}
	}

	private static String getQueryFile() {
		String queryFile = new String(_props
				.getProperty(Constants.QUERY_LIST_FILE));
		return queryFile;
	}

	private static String getQueryFile(String dbName) {
		try {
			verifyConfig();
			if (_props.getProperty(dbName + "." + Constants.QUERY_LIST_FILE) != null) {
				return (_props.getProperty(dbName + "."
						+ Constants.QUERY_LIST_FILE));
			}
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator on getQueryFile("
					+ dbName + ") " + ex.getMessage());
			SmartLogger.logThis(Level.WARNING, "I'm going to return getQueryFile() ");
			return getQueryFile();
		}
		return null;
	}
	private static String getExtraQueryFile(String dbName) {
		try {
			verifyConfig();
			if (_props.getProperty(dbName + "." + Constants.EXTRA_QUERY_LIST_FILE) != null) {
				return (_props.getProperty(dbName + "."
						+ Constants.EXTRA_QUERY_LIST_FILE));
			}
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator on getExtraQueryFile("
					+ dbName + ") " + ex.getMessage());
		}
		return null;
	}

	public Properties getQueryProp() {
		verifyConfig();
		Properties propsq = new Properties();
		try {
			FileInputStream fisq;
			String fiqp = new String(_props
					.getProperty(Constants.QUERY_LIST_FILE));
			File queryFile = new File(fiqp);
			fisq = new FileInputStream(new java.io.File(queryFile
					.getAbsoluteFile().getCanonicalPath()));
			queryFile = null;
			propsq.load(fisq);
			fisq.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator  while getting "
					+ Constants.QUERY_LIST_FILE + " error -> " + e.getMessage());
			return null;
		}
		return propsq;
	}

	/*public Properties getQueryProp(String dbname) {
		try {
			verifyConfig();
			Properties propsq = new Properties();
			FileInputStream fisq;
			if (dbname != "" && dbname != null) {
				// logger.warn("Method called with null or empty string on Configurator "+dbname);
				File queryFile = new File(_props.getProperty(dbname + "."
						+ Constants.QUERY_LIST_FILE));
				fisq = new FileInputStream(new java.io.File(queryFile
						.getAbsoluteFile().getCanonicalPath()));
				queryFile = null;
				SmartLogger.logThis(Level.CONFIG, "Debug loaded the " + dbname + "."
						+ Constants.QUERY_LIST_FILE);
				// fisq = new FileInputStream (new java.io.File(
				// _props.getProperty(dbname+"."+Constants.QUERY_LIST_FILE)));
			} else {
				// fisq = new FileInputStream (new java.io.File(
				// _props.getProperty(Constants.QUERY_LIST_FILE)));
				SmartLogger.logThis(Level.CONFIG, "Debug I'm loading the default "
						+ Constants.QUERY_LIST_FILE + " " + dbname
						+ " don't have it's own");
				File queryFile = new File(_props
						.getProperty(Constants.QUERY_LIST_FILE));
				fisq = new FileInputStream(new java.io.File(queryFile
						.getAbsoluteFile().getCanonicalPath()));
				queryFile = null;
			}
			propsq.load(fisq);
			fisq.close();
			return propsq;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator getting "+Constants.QUERY_LIST_FILE +" " + dbname
					+ e.getMessage());
			return null;
		}
	}
*/
	public String getZabbixServer() throws Exception {
		try {
			verifyConfig();
			return _props.getProperty(Constants.ZABBIX_SERVER_HOST);

		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE,
					"Error on Configurator while retriving zabbix server host "
							+ Constants.ZABBIX_SERVER_HOST + " or port "
							+ Constants.ZABBIX_SERVER_PORT + " " + ex);
			return null;
		}
	}

	public int getZabbixServerPort() throws Exception {
		try {
			verifyConfig();
			Integer port = new Integer(_props
					.getProperty(Constants.ZABBIX_SERVER_PORT));
			return port.intValue();
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE,
					"Error on Configurator while retriving zabbix server port "
							+ Constants.ZABBIX_SERVER_PORT + " " + ex);
			SmartLogger.logThis(Level.WARNING, "I will use the default port "
					+ Constants.ZABBIX_SERVER_DEFAULT_PORT);
			return Constants.ZABBIX_SERVER_DEFAULT_PORT;
		}
	}

	public Hashtable<String, Integer> getZabbixServers() throws Exception {
		String zxblist = new String();
		try {
			zxblist = new String(_props
					.getProperty(Constants.ZABBIX_SERVER_LIST));
		} catch (Exception e) {
			SmartLogger.logThis(Level.SEVERE, "Error on getZabbixServers while getting "
					+ Constants.ZABBIX_SERVER_LIST + " " + e.getMessage());
		}
		StringTokenizer st = new StringTokenizer(zxblist, Constants.DELIMITER);
		Hashtable<String, Integer> ZabbixServers = new Hashtable<String, Integer>();
		//int count = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken().toString();
			String server = new String();
			try {
				server = new String(_props.getProperty(token + "."
						+ Constants.ZABBIX_SERVER_HOST));
			} catch (Exception e) {
				SmartLogger.logThis(Level.SEVERE, "Error on getZabbixServers while getting "
						+ token + "." + Constants.ZABBIX_SERVER_HOST + " "
						+ e.getMessage());
			}
			Integer port = new Integer(Constants.ZABBIX_SERVER_DEFAULT_PORT);
			try {
				port = new Integer(_props.getProperty(token + "."
						+ Constants.ZABBIX_SERVER_PORT));
			} catch (Exception e) {
				SmartLogger.logThis(Level.WARNING,
						"Warning on getZabbixServers while getting " + token
								+ "." + Constants.ZABBIX_SERVER_PORT + " "
								+ e.getMessage());
				SmartLogger.logThis(Level.WARNING, "Warning I will use the default port"
						+ port);
			}
			ZabbixServers.put(server, port);
			//count++;
		}
		// fisdb.close();
		return ZabbixServers;
	}

	public static boolean hasQueryFile(String dbName) {
		if (dbName == null) {
			return false;
		}
		try {
			verifyConfig();
			if (_props.getProperty(dbName + "." + Constants.QUERY_LIST_FILE) != null) {
				return true;
			}
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator getting"+ Constants.QUERY_LIST_FILE
					+ dbName + ex.getMessage());
			return false;
		}
		return false;
	}
	
	public static boolean hasExtraQueryFile(String dbName) {
		if (dbName == null) {
			return false;
		}
		try {
			verifyConfig();
			if (_props.getProperty(dbName + "." + Constants.EXTRA_QUERY_LIST_FILE) != null) {
				return true;
			}
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE, "Error on Configurator getting"+ Constants.EXTRA_QUERY_LIST_FILE
					+ dbName + ex.getMessage());
			return false;
		}
		return false;
	}

	public boolean isEqualsDBList(DBConn[] _dbc) throws Exception {
		try {
			verifyConfig();

			String[] localdblist = this.getDBList();
			String[] remotedblist = new String[_dbc.length];
			for (int i = 0; i < _dbc.length; i++) {
				remotedblist[i] = _dbc[i].getName();
			}
			return ArrayUtils.isEquals(localdblist, remotedblist);
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE,
					"Error on Configurator while comparing the databases lists on isEqualsDBList "
							+ ex);
			return false;
		}
	}

	public DBConn[] rebuildDBList(DBConn[] _dbc) {
		try {
			verifyConfig();
			String[] localdblist = this.getDBList();
			String[] remotedblist = new String[_dbc.length];
			for (int i = 0; i < _dbc.length; i++) {
				remotedblist[i] = _dbc[i].getName();
			}

			Collection<DBConn> connections = new ArrayList<DBConn>();
			for (int j = 0; j < localdblist.length; j++) {
				if (ArrayUtils.contains(remotedblist, localdblist[j])) {
					DBConn tmpDBConn;
					tmpDBConn = _dbc[ArrayUtils.indexOf(remotedblist,
							localdblist[j])];
					connections.add(tmpDBConn);
				}
				if (!ArrayUtils.contains(remotedblist, localdblist[j])) {
					/*
					 * adding database
					 */
					SmartLogger.logThis(Level.INFO, "New database founded! "
							+ localdblist[j]);
					DBConn tmpDBConn = this.getDBConnManager(localdblist[j]);
					if (tmpDBConn != null) {
						connections.add(tmpDBConn);
					}
				}
			}
			for (int x = 0; x < _dbc.length; x++) {
				if (!ArrayUtils.contains(localdblist, _dbc[x].getName())) {
					SmartLogger.logThis(Level.WARNING, "Database " + _dbc[x].getName()
							+ " removed from configuration file");
					/**
					 * removing database
					 */
					// _dbc[x].closeAll();
					
					SmartLogger.logThis(Level.WARNING, "Database " + _dbc[x].getName()
							+ " conections closed");
					_dbc[x] = null;
				}
			}
			DBConn[] connArray = (DBConn[]) connections.toArray(new DBConn[0]);
			return connArray;
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE,
					"Error on Configurator while retriving the databases list "
							+ Constants.DATABASES_LIST + " error:" + ex);
			return _dbc;
		}

	}
	/*
	public static Integer  getSleep() throws Exception {
		try{
			verifyConfig();
			Integer sleep = new Integer(5);
			try{
				sleep=new Integer(_props.getProperty(Constants.ORABBIX_DAEMON_SLEEP));
			}catch (Exception e){
				SmartLogger.logThis(Level.WARNING,"Warning while getting "+Constants.ORABBIX_DAEMON_SLEEP+" I will use the default "+sleep);
			}
			return sleep;

		} catch (Exception ex){

			SmartLogger.logThis(Level.SEVERE,"Error on Configurator while retriving "+Constants.ORABBIX_DAEMON_SLEEP+" "+ex);
			return null;
		}
	}
*/
	public static boolean propVerify(String _prop) {
		// TODO Auto-generated method stub
		
		if (_prop!= null){
			if (!_prop.isEmpty()&&!_prop.equals("")){
				Properties props = new Properties();
				FileInputStream fisq;
				File queryFile = new File(_prop);
				try {
				fisq = new FileInputStream(new java.io.File(queryFile
							.getAbsoluteFile().getCanonicalPath()));
				props.load(fisq);
				fisq.close();
				return true;
				}
				catch (Exception ex){
					SmartLogger.logThis(Level.CONFIG,"Error on Configurator while checking file "+_prop+" "+ex);
					return false;
					}
				}
			}
				return false;
	}

	public static Querybox buildQueryBoxbyDBName(String dbname){
		String queryFile =null;
		String extraQueryFile=null;
		
		if (hasQueryFile(dbname)) {
			queryFile = getQueryFile(dbname);
			} else {
				queryFile = getQueryFile();
		}
		
		if (hasExtraQueryFile(dbname)){
			extraQueryFile = getExtraQueryFile(dbname);
		}
		
		Querybox qboxtmp = new Querybox(dbname,
					queryFile,extraQueryFile);
		return qboxtmp;
	}
	
	
}
