/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.hti.database.DBConnection;
import com.hti.database.DatabaseUtil;
import com.hti.hlr.BulkBackup;
import com.hti.hlr.LookupProcessing;
import com.hti.proc.WriteSubRespQueueThread;
import com.hti.rmi.BulkProcess;
import com.hti.rmi.BulkProcessImpl;
import com.hti.rmi.QueueBackup;
import com.hti.util.FileUtil;
import com.hti.util.GlobalVar;
import com.hti.util.IConstants;

/**
 * @author Administrator
 */
public class Starter implements Runnable {
	BulkProcess bulkProcess = null;
	WriteSubRespQueueThread writeSubmitResp;
	private static Logger logger = Logger.getLogger(Starter.class);
	public static DBConnection dbConnection;
	static {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(IConstants.LOG4J_CFG_FILE));
			PropertyConfigurator.configure(p);
		} catch (IOException e) {
			System.out.println(e + " : Logging Configuration ");
		}
	}

	public static void main(String[] args) {
		Starter main = new Starter();
		main.startServer();
		new Thread(main).start();
	}

	private void startServer() {
		logger.info("Starting System");
		try {
			bulkProcess = new BulkProcessImpl();
			initializeVar();
			startListen();
			checkBackupQueue();
			checkLookupBackupQueue();
			logger.info("System is Ready to Process Request");
		} catch (RemoteException ex) {
			logger.error(" Server Starting Error ", ex);
		}
	}

	private void startListen() throws RemoteException {
		logger.info("Starting Listener");
		// create on port 1099
		Registry registry = LocateRegistry.createRegistry(IConstants.PORT);
		// create a new service named myMessage
		registry.rebind("bulkprocess", bulkProcess);
		logger.info("Listener Started on Port: " + IConstants.PORT);
	}

	private void initializeVar() {
		logger.info("Initializing Variables");
		try {
			Properties properties = FileUtil.readProperties("config//config.file");
			IConstants.PORT = Integer.parseInt(properties.getProperty("PORT"));
			IConstants.SMPP_IP = properties.getProperty("SMPP_IP");
			IConstants.SMPP_PORT = Integer.parseInt(properties.getProperty("SMPP_PORT"));
			IConstants.THROUGHPUT = Integer.parseInt(properties.getProperty("THROUGHPUT"));
			IConstants.SLEEP = Integer.parseInt(properties.getProperty("SLEEP"));
			if (IConstants.THROUGHPUT >= 100 && IConstants.SLEEP <= 1000) {
				IConstants.SLEEP = IConstants.SLEEP + 500;
			}
			IConstants.ALERT_SENDER = properties.getProperty("ALERT_SENDER");
			IConstants.HLR_SERVER_IP = properties.getProperty("HLR_SERVER_IP");
			IConstants.HLR_SERVER_PORT = Integer.parseInt(properties.getProperty("HLR_SERVER_PORT"));
			IConstants.HLR_THROUGHPUT = Integer.parseInt(properties.getProperty("HLR_THROUGHPUT"));
			IConstants.HLR_SLEEP = Integer.parseInt(properties.getProperty("HLR_SLEEP"));
			IConstants.SESSION_ALIVE = Integer.parseInt(properties.getProperty("SESSION_ALIVE_TIME"));
			IConstants.HLR_DATABASE = properties.getProperty("HLR_DATABASE");
			IConstants.CONNECTION_URL = properties.getProperty("CONNECTION_URL");
			IConstants.USERNAME = properties.getProperty("USERNAME");
			IConstants.PASSWORD = properties.getProperty("PASSWORD");
			// ------------------- Starting Threads --------------------
			writeSubmitResp = new WriteSubRespQueueThread();
			writeSubmitResp.start();
		} catch (FileNotFoundException ex) {
			logger.error(" Configuration File Not Found ", ex);
		} catch (IOException ex) {
			logger.error(" Configuration File IOError ", ex);
		}
		dbConnection = new DBConnection();
		// GlobalVar.UserTimeout = new DatabaseUtil().getUsersTimeout();
		// GlobalVar.UserForceDelay = new DatabaseUtil().getForceDelay();
		// logger.info("Timeuot: " + GlobalVar.UserTimeout);
	}

	private void checkLookupBackupQueue() {
		logger.info("Checking Lookup Files Folder");
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String s) {
				if (s.endsWith(".txt")) {
					return true;
				}
				// others?
				return false;
			}

			public boolean accept(String name) {
				return true;
			}
		};
		File dir = new File(IConstants.LOOKUP_OBJECT_FILE_PATH);
		if (dir.exists()) {
			if (dir.isDirectory()) {
				String[] backupFiles = dir.list(filter);
				if (backupFiles == null) {
					logger.info("Lookup Files Folder Dir Not Found");
				} else {
					for (String backupFile : backupFiles) {
						try {
							logger.info("Reading Lookup Number File: " + backupFile);
							List list = FileUtil.readList(IConstants.LOOKUP_OBJECT_FILE_PATH + backupFile);
							if (!list.isEmpty()) {
								BulkBackup backup = new DatabaseUtil().getBackupEntry(backupFile);
								backup.setList(list);
								new Thread(new LookupProcessing(backup)).start();
							}
						} catch (FileNotFoundException ex) {
							logger.error(" Number File Error ", ex);
						} catch (IOException ex) {
							logger.error(" Number File IOError ", ex);
						}
					}
				}
			}
		}
		logger.info("Lookup Files Folder Check Finished");
	}

	private void checkBackupQueue() {
		logger.info("Checking Files Folder");
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String s) {
				if (s.endsWith(".ser")) {
					return true;
				}
				// others?
				return false;
			}

			public boolean accept(String name) {
				return true;
			}
		};
		File dir = new File(IConstants.OBJECT_FILE_PATH);
		if (dir.exists()) {
			if (dir.isDirectory()) {
				String[] backupFiles = dir.list(filter);
				if (backupFiles == null) {
					logger.info("Files Folder Dir Not Found");
				} else {
					for (int i = 0; i < backupFiles.length; i++) {
						try {
							String filename = backupFiles[i];
							logger.info("Reading Object File: " + filename);
							QueueBackup queueObject = (QueueBackup) FileUtil
									.readObject(IConstants.OBJECT_FILE_PATH + filename, false);
							int id = queueObject.getId();
							if (new DatabaseUtil().getBatchStatus(id)) {
								bulkProcess.process(queueObject);
							} else {
								GlobalVar.pausedBatch.put(id, queueObject);
							}
						} catch (FileNotFoundException ex) {
							logger.error(" Object File Error ", ex);
						} catch (IOException ex) {
							logger.error(" Object File IOError ", ex);
						}
					}
				}
			}
		}
		logger.info("Files Folder Check Finished");
	}

	@Override
	public void run() {
		int loop_counter = 0;
		while (true) {
			if (++loop_counter > 6) {
				checkMemoryUsage();
				if (!GlobalVar.pausedBatch.isEmpty()) {
					logger.info("Paused Bacth Count: " + GlobalVar.pausedBatch.size());
				}
				loop_counter = 0;
			}
			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
			}
		}
	}

	private void checkMemoryUsage() {
		long mb = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		long used_memory = (runtime.totalMemory() - runtime.freeMemory()) / mb;
		long max_memory = (runtime.maxMemory() / mb);
		long gc_limit = (max_memory * 5) / 10; // if 50% memeory Used
		logger.info("Memory Used:---> " + used_memory + " MB. Max Available: " + max_memory + " MB");
		if (used_memory > gc_limit) {
			logger.warn("Excuting Garbage Collector. Used Memory: " + used_memory + " MB"); // Print used memory
			System.gc();
			logger.warn("After Garbage Collection. Used Memory: "
					+ ((runtime.totalMemory() - runtime.freeMemory()) / mb) + " MB"); // Print used memory
			logger.warn("Free Memory:" + (runtime.freeMemory() / mb) + " MB"); // Print free memory
			logger.warn("Max Available Memory:" + max_memory + " MB"); // Print Maximum available memory
		}
	}
}
