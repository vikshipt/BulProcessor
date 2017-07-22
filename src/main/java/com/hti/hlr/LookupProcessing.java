/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.hlr;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.hti.database.DatabaseUtil;
import com.hti.rmi.LookupObject;
import com.hti.util.FileUtil;
import com.hti.util.IConstants;
import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.UnbindResp;
import com.logica.smpp.pdu.ValueNotSetException;
import com.logica.smpp.pdu.WrongLengthOfStringException;

/**
 *
 * @author Administrator
 */
public class LookupProcessing implements Runnable {
	private Logger logger = Logger.getLogger(LookupProcessing.class);
	private boolean stop;
	private List list;
	private String systemid;
	private String password;
	private String batchid;
	private Session session;
	private int thread_count;
	private String filename;
	private int id;

	public LookupProcessing(BulkBackup backup) {
		this.systemid = backup.getSystemid();
		this.password = backup.getPassword();
		this.list = backup.getList();
		this.batchid = backup.getBatchid();
		this.filename = backup.getFilename();
		this.id = backup.getId();
		this.thread_count = backup.getThreadCount();
		logger.info(systemid + " (" + thread_count + ")Lookup Processing Thread Started.Queue: " + list.size());
	}

	public LookupProcessing(LookupObject lookup, int thread_count) throws Exception {
		this.systemid = lookup.getSystemid();
		this.password = lookup.getPassword();
		this.list = lookup.getList();
		this.batchid = lookup.getBatchid();
		this.thread_count = thread_count;
		createEntry();
		logger.info(systemid + " (" + thread_count + ")Lookup Processing Thread Started.Queue: " + list.size());
	}

	private void createEntry() throws Exception {
		logger.debug("Creating File & DB Entry For Batch: " + batchid);
		filename = systemid + "_" + batchid + "_" + thread_count + ".txt";
		writeList();
		logger.debug("Created File For Batch: " + batchid);
		BulkBackup backup = new BulkBackup();
		backup.setBatchid(batchid);
		backup.setSystemid(systemid);
		backup.setPassword(password);
		backup.setFilename(filename);
		backup.setThreadCount(thread_count);
		logger.debug("Adding Database Entry For Batch: " + batchid);
		id = new DatabaseUtil().addFileBackupEntry(backup);
	}

	private void writeList() throws IOException {
		Iterator itr = list.iterator();
		StringBuilder buffer = new StringBuilder();
		while (itr.hasNext()) {
			buffer.append((String) itr.next()).append("\n");
		}
		FileUtil.writeContent(IConstants.LOOKUP_OBJECT_FILE_PATH + filename, buffer.toString(), false);
	}

	@Override
	public void run() {
		int totalCount = 0;
		while (!stop) {
			int commandStatus = getConnection();
			if (commandStatus != Data.ESME_ROK) {
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException inex) {
				}
				continue;
			}
			SubmitSM msg = null;
			int counter = 0;
			while (!list.isEmpty()) {
				String destination = (String) list.remove(0);
				msg = new SubmitSM();
				try {
					msg.setSourceAddr((byte) 5, (byte) 0, "HLR");
					msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
					msg.setShortMessage(batchid);
					msg.setRegisteredDelivery((byte) 1);
					msg.setDataCoding((byte) 0);
				} catch (WrongLengthOfStringException ex) {
					logger.error(systemid + " -> SubmitSM Creation Error " + ex);
				}
				try {
					session.submit(msg);
					totalCount++;
					if (++counter >= IConstants.HLR_THROUGHPUT) {
						logger.info(systemid + " [" + thread_count + "] HLR -> Submit Count: " + totalCount
								+ " Remaining: " + list.size());
						counter = 0;
						try {
							Thread.sleep(IConstants.HLR_SLEEP);
						} catch (InterruptedException ex) {
						}
					}
				} catch (TimeoutException | PDUException | WrongSessionStateException ex) {
					logger.error(systemid + " (" + thread_count + ") onSubmit(HLR): " + ex);
				} catch (Exception ex) {
					logger.error(systemid + " <- (HLR)Session Disconnected -> ");
					list.add(destination);
					try {
						writeList();
					} catch (IOException ioe) {
						logger.error(filename + " (HLR)Write Object Error -> " + ioe);
					}
					break;
				}
			}
			if (list.isEmpty()) {
				clearEntry();
				stop = true;
			} else {
				stopSession();
			}
		}
		stop();
		logger.info(systemid + " (" + thread_count + ") Lookup Processing Thread Stopping. Queue: " + list.size());
	}

	private void clearEntry() {
		logger.info("Deleted Backup File[ " + filename + "]-> "
				+ new File(IConstants.LOOKUP_OBJECT_FILE_PATH + filename).delete());
		logger.info("Deleted DBBackupFile Entry [ " + filename + "]-> " + new DatabaseUtil().deleteLookupEntry(id));
	}

	private void stopSession() {
		if (session != null) {
			logger.info(systemid + "(" + thread_count + ") HLR Closing Session");
			try {
				UnbindResp unbind = session.unbind();
				// logger.info(systemid + "==> " + unbind.debugString());
				logger.info(systemid + " (" + thread_count + ") HLR Session Closed. Queue Size: " + list.size());
			} catch (IOException inex) {
				logger.error("HLR Closing Session Error(1): " + inex);
			} catch (WrongSessionStateException ex) {
				logger.error("HLR Closing Session Error(2): " + ex);
			} catch (TimeoutException | PDUException ex) {
				logger.error("HLR Closing Session Error(3): " + ex);
			}
		}
	}

	private int getConnection() {
		logger.debug(systemid + " getConnection()");
		int commandStatus = Data.ESME_RBINDFAIL;
		try {
			commandStatus = connect();
		} catch (Exception ex) {
			logger.error(systemid + " -> HLR Server Connection Error");
		}
		return commandStatus;
	}

	private int connect() throws WrongLengthOfStringException, ValueNotSetException, IOException,
			WrongSessionStateException, TimeoutException, PDUException {
		int commandStatus = Data.ESME_RBINDFAIL;
		logger.debug(systemid + " connect()");
		Connection connection = new TCPIPConnection(IConstants.HLR_SERVER_IP, IConstants.HLR_SERVER_PORT);
		session = new Session(connection);
		BindRequest breq = new BindTransmitter();
		breq.setSystemId(systemid);
		breq.setPassword(password);
		breq.setSystemType("BULK");
		breq.setInterfaceVersion(Data.SMPP_V34);
		Response response = session.bind(breq, new HlrPduEventListenerImpl(systemid));
		if (response != null) {
			commandStatus = response.getCommandStatus();
			logger.debug(systemid + " " + response.debugString());
			if (commandStatus == Data.ESME_ROK) {
				logger.info(systemid + " HLR Connected Transmitter : " + response.debugString());
			} else {
				logger.error(systemid + " HLR Connection Failed : " + response.debugString());
			}
		}
		logger.debug(systemid + " connect() exit");
		return commandStatus;
	}

	private void stop() {
		logger.debug(systemid + " stop()");
		stopSession();
		logger.debug(systemid + " exit stop()");
	}
}
