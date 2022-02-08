package com.smartmarmot.common.db;

import java.util.Properties;

import java.util.logging.Level;

import com.smartmarmot.common.Constants;
import com.smartmarmot.common.SmartLogger;


public class DBcfg {
	
	private String dbUser = null;
	private String dbPassword = null;
	private String dbURI = null;
	private int dbPoolMaxActive = 5;
	private int dbPoolMaxIdle = 1;
	private int dbPoolMaxWait = 100;
	private DBType dbtype;


	public DBType getDbtype() {
		return dbtype;
	}
	public int getDbPoolMaxWait() {
		return dbPoolMaxWait;
	}
	public int getDbPoolMaxIdle() {
		return dbPoolMaxIdle;
	}
	public int getDbPoolMaxActive() {
		return dbPoolMaxActive;
	}
	public String getDbURI() {
		return dbURI;
	}
	public String getDbUser() {
		return dbUser;
	}
	public String getDbPassword() {
		return dbPassword;
	}

	public  DBcfg (Properties _props,String _dbName) {
		
		
		try {
			dbURI = new String(_props.getProperty(_dbName + "."
					+ Constants.CONN_URL));
		} catch (Exception ex) {
			SmartLogger.logThis(Level.SEVERE,
					"Error on DBcfg while getting "
							+ _dbName + "." + Constants.CONN_URL + " "
							+ ex.getMessage());
		}

		
		try {
			dbUser = new String(_props.getProperty(_dbName + "."
					+ Constants.CONN_USERNAME));
		} catch (Exception ex) {
			try {
				SmartLogger.logThis(Level.CONFIG,
						"Error on DBCfg while getting "
								+ _dbName + "."
								+ Constants.CONN_USERNAME + " "
								+ ex.getMessage());
				
				dbUser = new String(_props.getProperty(
						Constants.CONN_DEFAULT_USERNAME));
				} catch (Exception ex1){
				SmartLogger.logThis(Level.SEVERE,
						"Error on DBCfg while getting "
								+ Constants.CONN_DEFAULT_USERNAME + " "
								+ ex1.getMessage());
					}
		}
		
		try {
			this.dbPassword = new String(_props.getProperty(_dbName + "."
					+ Constants.CONN_PASSWORD));
		} catch (Exception ex) {
			try{
				SmartLogger.logThis(Level.CONFIG,
						"Error on DBcfg while getting "
								+ _dbName + "."
								+ Constants.CONN_PASSWORD + " "
								+ ex.getMessage());
				this.dbPassword = new String(_props.getProperty(
						Constants.CONN_DEFAULT_PASSWORD));
			} catch (Exception ex1){
			SmartLogger.logThis(Level.SEVERE,
					"Error on DBcfg while getting "
							+ _dbName + "." + Constants.CONN_PASSWORD + " "
							+ ex.getMessage());
			}
		}
		this.dbtype = new DBType(Constants.DEFAULT_DBTYPE);
		//try {
		//	this.dbtype = new DBType(new String(_props.getProperty(_dbName  + "." + Constants.DATABASES_TYPE)));
		//		} catch (Exception ex) {
		//	SmartLogger.logThis(Level.SEVERE,
		//			"Error on DBcfg while getting "
		//					+ _dbName + "." + Constants.DATABASES_TYPE + " "
		//					+ ex.getMessage());
		//}
		Integer maxActive = new Integer(5);
		try {
			maxActive = new Integer(_props.getProperty(_dbName + "."
					+ Constants.CONN_MAX_ACTIVE));
		} catch (Exception ex) {
			SmartLogger.logThis(Level.CONFIG, "Note: " + _dbName + "."
					+ Constants.CONN_MAX_ACTIVE + " " + ex.getMessage());
			try {
				maxActive = new Integer(_props
						.getProperty(Constants.DATABASES_LIST + "."
								+ Constants.CONN_MAX_ACTIVE));
			} catch (Exception e) {
				SmartLogger.logThis(Level.WARNING, "Note: "
						+ Constants.DATABASES_LIST + "."
						+ Constants.CONN_MAX_ACTIVE + " " + e.getMessage());
				SmartLogger.logThis(Level.WARNING, "Warning I will use default value "
						+ this.dbPoolMaxActive);
			}
		}
		this.dbPoolMaxActive=maxActive.intValue();
		
		Integer maxIdle = new Integer(1);
		try {
			maxIdle = new Integer(_props.getProperty(_dbName + "."
					+ Constants.CONN_MAX_IDLE));
		} catch (Exception ex) {
			SmartLogger.logThis(Level.CONFIG, "Note: " + _dbName + "."
					+ Constants.CONN_MAX_IDLE + " " + ex.getMessage());
			try {
				maxIdle = new Integer(_props
						.getProperty(Constants.DATABASES_LIST + "."
								+ Constants.CONN_MAX_IDLE));
			} catch (Exception e) {
				SmartLogger.logThis(Level.WARNING, "Note: "
						+ Constants.DATABASES_LIST + "."
						+ Constants.CONN_MAX_IDLE + " " + e.getMessage());
				SmartLogger.logThis(Level.WARNING, "Warning I will use default value "
						+ maxIdle);
			}
		this.dbPoolMaxIdle=maxIdle.intValue();
		
		Integer maxWait = new Integer(100);
		try {
			maxWait = new Integer(_props.getProperty(_dbName + "."
					+ Constants.CONN_MAX_WAIT));
		} catch (Exception exx) {
			SmartLogger.logThis(Level.CONFIG, "Note: " + _dbName + "."
					+ Constants.CONN_MAX_WAIT + " " + exx.getMessage());
			try {
				maxWait = new Integer(_props
						.getProperty(Constants.DATABASES_LIST + "."
								+ Constants.CONN_MAX_WAIT));
			} catch (Exception e) {
				SmartLogger.logThis(Level.WARNING, "Note: "
						+ Constants.DATABASES_LIST + "."
						+ Constants.CONN_MAX_WAIT + " " + e.getMessage());
				SmartLogger.logThis(Level.WARNING, "Warning I will use default value "
						+ maxWait);
			}
		}
		
		this.dbPoolMaxWait=maxWait.intValue();
		
		}
		
		
		
		

	}
	public DBType geDBType() {
		// TODO Auto-generated method stub
		return this.dbtype;
	}
}









