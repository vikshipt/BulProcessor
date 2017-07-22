/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.rmi;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import com.hti.database.DatabaseUtil;
import com.hti.hlr.BulkProcessThread;
import com.hti.proc.ProcessingThread;
import com.hti.util.GlobalVar;
import com.hti.util.IConstants;

/**
 * @author Administrator
 */
public class BulkProcessImpl extends UnicastRemoteObject implements BulkProcess {
	private Logger logger = Logger.getLogger(BulkProcessImpl.class);

	public BulkProcessImpl() throws RemoteException {
	}

	@Override
	public void process(QueueBackup queueBackup) throws RemoteException {
		logger.info(queueBackup.getUser() + " Queue Received. Size: " + queueBackup.noList.size());
		startProcessThread(queueBackup);
	}

	private void startProcessThread(QueueBackup queueBackup) {
		logger.debug(queueBackup.getUser() + " startProcessThread() QueueSize: " + queueBackup.noList.size());
		int id = 0;
		try {
			ProcessingThread procth = new ProcessingThread(queueBackup);
			id = procth.startProcessThread();
			new Thread(procth).start();
			GlobalVar.processmap.put(id, procth);
		} catch (Exception ex) {
			logger.error(queueBackup.getUser() + " Process Start Error.Size: " + queueBackup.noList.size(), ex);
		}
		logger.debug(queueBackup.getUser() + " startProcessThread() exit: " + id);
	}

	@Override
	public void stopProcess(int id) throws RemoteException {
		logger.info(" Request To Stop Process: " + id);
		if (GlobalVar.processmap.containsKey(id)) {
			((ProcessingThread) GlobalVar.processmap.get(id)).remove();
		} else if (GlobalVar.pausedBatch.containsKey(id)) {
			new DatabaseUtil().deleteFileBackupEntry(id);
			QueueBackup queueBackup = (QueueBackup) GlobalVar.pausedBatch.remove(id);
			if (queueBackup != null) {
				logger.info(" Deleting Object File: " + queueBackup.getFileName());
				File file = new File(IConstants.OBJECT_FILE_PATH + queueBackup.getFileName() + ".ser");
				if (file.exists()) {
					file.delete();
				}
				file = null;
			}
		}
		logger.debug("stopProcess(" + id + ") exit");
	}

	@Override
	public void resumeProcess(int id) throws RemoteException {
		logger.info(" Request To Resume Process: " + id);
		QueueBackup queueBackup = null;
		if (GlobalVar.pausedBatch.containsKey(id)) {
			queueBackup = (QueueBackup) GlobalVar.pausedBatch.remove(id);
			startProcessThread(queueBackup);
		}
		logger.debug("resumeProcess(" + id + ") exit");
	}

	@Override
	public QueueBackup editBatch(int id) throws RemoteException {
		logger.info(" Request To Edit Process: " + id);
		QueueBackup queueBackup = null;
		if (GlobalVar.processmap.containsKey(id)) {
			ProcessingThread procth = (ProcessingThread) GlobalVar.processmap.get(id);
			procth.setActive(false);
			queueBackup = procth.getBackupObject();
			logger.debug("editBatch(" + id + "):" + queueBackup.noList.size());
		} else if (GlobalVar.pausedBatch.containsKey(id)) {
			logger.info(" Requested Batch is Paused: " + id);
			queueBackup = (QueueBackup) GlobalVar.pausedBatch.get(id);
		}
		logger.info("editBatch(" + id + ") exit :" + queueBackup.noList.size());
		return queueBackup;
	}

	@Override
	public int getProcessedCount(int id) throws RemoteException {
		int count = 0;
		if (GlobalVar.processmap.containsKey(id)) {
			count = (int) ((ProcessingThread) GlobalVar.processmap.get(id)).getCounter();
		} else if (GlobalVar.pausedBatch.containsKey(id)) {
			count = ((QueueBackup) GlobalVar.pausedBatch.get(id)).getCount();
		}
		logger.info(" Count Requested For Process : " + id + " Count: " + count);
		return count;
	}

	@Override
	public QueueBackup getPausedBatch(int id) throws RemoteException {
		logger.info(" Paused Batch Requested : " + id);
		QueueBackup queueBackup = null;
		if (GlobalVar.pausedBatch.containsKey(id)) {
			queueBackup = (QueueBackup) GlobalVar.pausedBatch.remove(id);
		}
		logger.info("getPausedBatch(" + id + ") exit");
		return queueBackup;
	}

	@Override
	public void processHLR(LookupObject lookupObject) throws RemoteException {
		logger.info(lookupObject.getSystemid() + " Lookup process Queue: " + lookupObject.getList().size());
		new Thread(new BulkProcessThread(lookupObject)).start();
	}
}
