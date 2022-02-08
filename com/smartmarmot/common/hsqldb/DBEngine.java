package com.smartmarmot.common.hsqldb;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.Server;


public class DBEngine implements Runnable {

	private boolean running = false;
	private boolean stopped = false;
	
	public  boolean start() {

    // 'Server' is a class of HSQLDB representing
    // the database server
    Server hsqlServer = null;
     try {
        hsqlServer = new Server();

        // HSQLDB prints out a lot of informations when
        // starting and closing, which we don't need now.
        // Normally you should point the setLogWriter
        // to some Writer object that could store the logs.
        hsqlServer.setLogWriter(null);
        hsqlServer.setSilent(true);

        // The actual database will be named 'xdb' and its
        // settings and data will be stored in files
        // testdb.properties and testdb.script
        hsqlServer.setDatabaseName(0, "smarmotdb");
        hsqlServer.setDatabasePath(0, "file:./db/smarmotdb");

        // Start the database!
        hsqlServer.start();
        return true;
    } finally {
        // Closing the server
        if (hsqlServer != null) {
            hsqlServer.stop();
            return false;
        }
    }
    
}
	
	public void terminate() {
		running = false;
		while (!stopped)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		System.out.println("Stopped");
	}
	public static boolean stop(){
		
		return true;
	}

	@Override
	public void run() {
		while (!running){
				this.running=this.start();
		}		
	}
	
}