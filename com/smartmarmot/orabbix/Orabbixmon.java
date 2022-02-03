/*
 * Copyright (C) 2010 Andrea Dalle Vacche.
 * 
 * This file is part of DBforBIX.
 *
 * Orabbix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Orabbix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Orabbix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.smartmarmot.orabbix;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.smartmarmot.common.db.DBConn;
import com.smartmarmot.common.db.DBJob;

public class Orabbixmon implements Runnable {

	private boolean running = true;
	private boolean stopped = false;

	private static String configFile;

	private static final Logger LOGGER = Logger.getLogger(Orabbixmon.class.getName());

	public Orabbixmon(String _cfgfile) {
		try {
			configFile = _cfgfile;
			LOGGER.log(Level.INFO, "Starting " + Constants.BANNER);
		} catch (Exception ex) {
			ex.printStackTrace();
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

	@Override
	public void run() {
		try {
			Configurator cfg = null;
			try {
				cfg = new Configurator(configFile);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while creating configurator with " + configFile
				+ " " + e);
			}
			RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
			String pid = rmxb.getName();
			LOGGER.log(Level.INFO, Constants.PROJECT_NAME
			+ " started with pid:" + pid.split("@")[0].toString());
			String pidfile = cfg.getPidFile();
			try {
				Utility.writePid(pid.split("@")[0].toString(), pidfile);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while trying to write pidfile " + e);
			}

			Locale.setDefault(Locale.US);

			DBConn[] myDBConn = cfg.getConnections();

			if (myDBConn == null || myDBConn.length == 0) {
				LOGGER.log(Level.SEVERE, "ERROR on main - Connections is null or empty");
				throw new Exception("ERROR on main - Connections is null or empty");		
			}
			
			/**
			 * retrieve maxThread
			 */
			Integer maxThread = 0;
			try {
				maxThread = cfg.getMaxThread();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "MaxThread not defined calculated maxThread = "
				+ myDBConn.length * 3);
			}
			if (maxThread == null)
				maxThread = 0;
			if (maxThread == 0) {
				maxThread = myDBConn.length * 3;
			}

			ExecutorService executor = Executors.newFixedThreadPool(maxThread.intValue());
			/**
			 * populate qbox
			 */
			Hashtable<String, Querybox> qbox = new Hashtable<String, Querybox>();
			for (int i = 0; i < myDBConn.length; i++) {
				if (cfg.hasQueryFile(myDBConn[i].getName())) {
					String queryFile = cfg.getQueryFile(myDBConn[i].getName());
					Querybox qboxtmp = new Querybox(myDBConn[i].getName(),
							queryFile);
					qbox.put(myDBConn[i].getName(), qboxtmp);
				} else {
					String queryFile = cfg.getQueryFile();
					Querybox qboxtmp = new Querybox(myDBConn[i].getName(),
							queryFile);
					qbox.put(myDBConn[i].getName(), qboxtmp);
				}
			}// for (int i = 0; i < myDBConn.length; i++) {

			cfg = null;
			/**
			 * daemon begin here
			 */
			while (running) {
				/**
				 * istantiate a new configurator
				 */
				Configurator c = new Configurator(configFile);

				/*
				 * here i rebuild DB's List
				 */
				if (!c.isEqualsDBList(myDBConn)) {

					// rebuild connections DBConn[]

					myDBConn = c.rebuildDBList(myDBConn);
					for (int i = 1; i < myDBConn.length; i++) {
						if (!qbox.containsKey(myDBConn[i].getName())) {
							if (c.hasQueryFile(myDBConn[i].getName())) {
								String queryFile = c.getQueryFile(myDBConn[i]
										.getName());
								Querybox qboxtmp = new Querybox(
										myDBConn[i].getName(), queryFile);
								qbox.put(myDBConn[i].getName(), qboxtmp);
							} else {
								String queryFile = c.getQueryFile();
								Querybox qboxtmp = new Querybox(
										myDBConn[i].getName(), queryFile);
								qbox.put(myDBConn[i].getName(), qboxtmp);
							}
						}
					}
				}// if (!c.isEqualsDBList(myDBConn)) {

				/*
				 * ready to run query
				 */

				for (int i = 0; i < myDBConn.length; i++) {
					Querybox actqb = qbox.get(myDBConn[i].getName());
					actqb.refresh();
					Query[] q = actqb.getQueries();

					SharedPoolDataSource spds = myDBConn[i].getSPDS();

					Hashtable<String, Integer> zabbixServers = c
							.getZabbixServers();
					LOGGER.log(Level.CONFIG, "Ready to run DBJob for dbname ->"
					+ myDBConn[i].getName());
					Runnable runner = new DBJob(spds, q, Constants.QUERY_LIST,
							zabbixServers, myDBConn[i].getName());
					executor.execute(runner);

				}// for (int i = 0; i < myDBConn.length; i++) {
				Thread.sleep(60 * 1000);
				LOGGER.log(Level.CONFIG, "Waking up Goood Morning");
			}
		} catch (Exception e1) {
			System.out.println("Stopping");
			e1.printStackTrace();
			stopped = true;
		}

	}

}