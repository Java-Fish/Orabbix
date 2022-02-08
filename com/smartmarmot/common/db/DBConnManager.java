package com.smartmarmot.common.db;

import java.time.Duration;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import java.util.logging.Level;

import com.smartmarmot.common.SmartLogger;

public class DBConnManager {

	private static DataSource ds = null;
	private static GenericObjectPool _pool = null;
	private static DBType dbType;

	/**
	 * @param config
	 *            configuration from an XML file.
	 */
	public DBConnManager(DBcfg config) {
		try {
			this.setDBType(config.geDBType());
			connectToDB(config);
		} catch (Exception e) {
			SmartLogger.logThis(Level.SEVERE,
					"Failed to construct ConnectionManager" + e);
		}
	}

	/**
	 * destructor
	 */
	protected void finalize() {
		SmartLogger.logThis(Level.CONFIG,"Finalizing ConnectionManager");
		try {
			super.finalize();
		} catch (Throwable ex) {
			SmartLogger.logThis(Level.SEVERE,
					"ConnectionManager finalize failed to disconnect from "+getDbType().getDBTypeString()
					+ ex);
		}
	}

	/**
	 * connectToDB - Connect to the MySql DB!
	 */
	private void connectToDB(DBcfg dbconfig) {

		try {
			java.lang.Class.forName(dbconfig.getDbtype().getJDBCDriverClass()).newInstance();
		} catch (Exception e) {
			SmartLogger.logThis(Level.SEVERE,
					"Error when attempting to obtain DB Driver: "
					+ dbconfig.getDbtype().getJDBCDriverClass() + " on "
					+ new Date().toString()+ e);
		}

		SmartLogger.logThis(Level.CONFIG,"Trying to connect to database...");
		try {
			DBConnManager.setDs(setupDataSource(dbconfig.getDbURI(),
					dbconfig.getDbUser(), dbconfig.getDbPassword(),
					dbconfig.getDbPoolMaxIdle(), dbconfig.getDbPoolMaxActive(),
					 dbconfig.getDbPoolMaxWait()));

			SmartLogger.logThis(Level.CONFIG,"Connection attempt to database succeeded.");
		} catch (Exception e) {
			SmartLogger.logThis(Level.SEVERE,"Error when attempting to connect to DB "+ e);
		}
	}

	/**
	 * 
	 * @param connectURI
	 *            - JDBC Connection URI
	 * @param username
	 *            - JDBC Connection username
	 * @param password
	 *            - JDBC Connection password
	 * @param minIdle
	 *            - Minimum number of idle connection in the connection pool
	 * @param maxActive
	 *            - Connection Pool Maximum Capacity (Size)
	 * @throws Exception
	 */
	public static DataSource setupDataSource(String connectURI,
			String username, String password, int minIdle, int maxActive, int maxWait)
	throws Exception {
		//
		// First, we'll need a ObjectPool that serves as the
		// actual pool of connections.
		//
		// We'll use a GenericObjectPool instance, although
		// any ObjectPool implementation will suffice.
		//
		GenericObjectPool connectionPool = new GenericObjectPool(null);

		connectionPool.setMinIdle(minIdle);
		connectionPool.setMaxTotal(maxActive);
		connectionPool.setMaxWait(Duration.ofMillis(maxWait));
		
		connectionPool.setTestOnReturn(true);
		
		
		

		DBConnManager._pool = connectionPool;
		// we keep it for two reasons
		// #1 We need it for statistics/debugging
		// #2 PoolingDataSource does not have getPool()
		// method, for some obscure, weird reason.

		//
		// Next, we'll create a ConnectionFactory that the
		// pool will use to create Connections.
		// We'll use the DriverManagerConnectionFactory,
		// using the connect string from configuration
		//
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
				connectURI, username, password);
		
		
		//
		// Now we'll create the PoolableConnectionFactory, which wraps
		// the "real" Connections created by the ConnectionFactory with
		// the classes that implement the pooling functionality.
		//
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
		poolableConnectionFactory.setPool(connectionPool);
		poolableConnectionFactory.setDefaultReadOnly(false);
		poolableConnectionFactory.setDefaultAutoCommit(true);

		poolableConnectionFactory.setValidationQuery(getDbType().getValidationQuery());
		
		PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

		return dataSource;
	}

	public static void printDriverStats() throws Exception {
		ObjectPool connectionPool = DBConnManager._pool;
		SmartLogger.logThis(Level.INFO,
				"NumActive: " + connectionPool.getNumActive());
		SmartLogger.logThis(Level.INFO,
				"NumIdle: " + connectionPool.getNumIdle());
	}

	public static Integer getNumActive() throws Exception {
		ObjectPool connectionPool = DBConnManager._pool;
		return new Integer(connectionPool.getNumActive());
	}
	public static Integer getNumIdle() throws Exception {
		ObjectPool connectionPool = DBConnManager._pool;
		return new Integer(connectionPool.getNumIdle());
	}
	
	public DBType getDBType() {
		return getDbType();
	}
	private void setDBType(DBType _dbt) {
		this.setDbType(_dbt);
	}

	public DataSource getDs() {
		return ds;
	}

	public static void setDs(DataSource ds) {
		DBConnManager.ds = ds;
	}

	public static DBType getDbType() {
		return dbType;
	}

	public void setDbType(DBType dbType) {
		DBConnManager.dbType = dbType;
	}


}
