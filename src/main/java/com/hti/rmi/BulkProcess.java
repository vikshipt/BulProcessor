/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Administrator
 */
public interface BulkProcess extends Remote {
	void process(QueueBackup queueBackup) throws RemoteException;

	void stopProcess(int id) throws RemoteException;

	void resumeProcess(int id) throws RemoteException;

	QueueBackup editBatch(int id) throws RemoteException;

	int getProcessedCount(int id) throws RemoteException;

	QueueBackup getPausedBatch(int id) throws RemoteException;

	void processHLR(LookupObject lookupObject) throws RemoteException;
}
