/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.proc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.hti.database.DatabaseUtil;
import com.hti.rmi.QueueBackup;
import com.hti.user.UserDTO;
import com.hti.util.FileUtil;
import com.hti.util.GlobalVar;
import com.hti.util.IConstants;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.UnbindResp;
import com.logica.smpp.util.ByteBuffer;

/**
 * @author Administrator
 */
public class ProcessingThread implements Runnable {
	private Logger logger = Logger.getLogger(ProcessingThread.class);
	QueueBackup backupObject = null;
	// private boolean stop;
	// private boolean edit;
	private long counter = 0;
	private int id = 0;
	private double delay = 0;
	private Session session = null;
	private String filename = null;
	private DatabaseUtil databaseUtil = null;
	private String user = null;
	private String password = null;
	private String startDate = null;
	private String startTime = null;
	private Map sequencemap = null;
	private double force_delay = 0;
	private UserDTO userDTO;
	private boolean active = true;
	private boolean remove = false;
	boolean connect = false;

	public long getCounter() {
		logger.debug(id + " Processed Count : " + counter);
		return counter;
	}

	public QueueBackup getBackupObject() {
		logger.debug(id + " getBackupObject()" + counter);
		return backupObject;
	}

	public void setActive(boolean active) {
		logger.debug(id + " setActive(" + active + ")" + counter);
		this.active = active;
	}

	public void remove() {
		logger.debug(id + " remove()" + counter);
		this.active = false;
		this.remove = true;
	}

	public ProcessingThread(QueueBackup backupObject) {
		logger.debug(backupObject.getUser() + " ProcessingThread()");
		this.backupObject = backupObject;
	}

	public int startProcessThread() throws Exception {
		logger.info(backupObject.getUser() + " Starting Process. Queue Size: " + backupObject.noList.size());
		databaseUtil = new DatabaseUtil();
		userDTO = databaseUtil.getUserObject(backupObject.getUser());
		if (userDTO == null) {
			throw new Exception("Invalid User Found");
		}
		filename = IConstants.OBJECT_FILE_PATH + backupObject.getFileName() + ".ser";
		FileUtil.writeObject(filename, backupObject);
		startDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		startTime = new SimpleDateFormat("HH:mm").format(new Date());
		id = databaseUtil.addFileBackupEntry(backupObject.getUser(), backupObject.getFileName(),
				backupObject.getSender(), startDate, startTime, backupObject.getTotalNumbers(),
				backupObject.getFirstNumber(), backupObject.getDelay(), backupObject.getReqType());
		backupObject.setId(id);
		if (GlobalVar.UserSequence.containsKey(backupObject.getUser())) {
			sequencemap = (Map) GlobalVar.UserSequence.get(backupObject.getUser());
		} else {
			sequencemap = new ConcurrentHashMap();
			GlobalVar.UserSequence.put(backupObject.getUser(), sequencemap);
		}
		logger.debug(backupObject.getUser() + " startProcessThread() exit. Id: " + id + " & Queue Size: "
				+ backupObject.noList.size());
		return id;
	}

	@Override
	public void run() {
		counter = backupObject.getCount();
		user = backupObject.getUser();
		password = userDTO.getPassword();
		String reqType = backupObject.getReqType();
		Hashtable mapTable = backupObject.getMapTable();
		String cont_msg = backupObject.getMessage();
		String message1 = backupObject.getMessage();
		String dest_name = "";
		String destination_no = null;
		String greet = backupObject.getGreet();
		String messageType = backupObject.getMessageType();
		String sender = backupObject.getSender();
		// String header = backupObject.getHeader();
		int ston = backupObject.getSton();
		int snpi = backupObject.getSnpi();
		delay = backupObject.getDelay();
		force_delay = userDTO.getForceDelay();
		// System.out.println("Map Table: "+mapTable);
		logger.info(user + " Processing Started .Id: " + id + " & Delay: " + delay + "& ForceDelay: " + force_delay
				+ " & Queue Size: " + backupObject.noList.size());
		try {
			// System.out.println(user + " <-- creating connection -->");
			createConnection();
			int msgCounter = 0;
			while (active) {
				List msglist = new ArrayList();
				if (backupObject.noList.size() > 0) {
					destination_no = ((String) backupObject.noList.remove(0)).trim();
					counter++;
					List list = null;
					if (reqType.equalsIgnoreCase("groupDatabulk")) {
						list = (List) mapTable.remove(destination_no);
						// System.out.println(destination_no + " Message List: " + list);
					} else {
						list = new ArrayList();
						if (greet == null) {
							if (destination_no.length() == 0) {
								break;
							}
						} else {
							if (destination_no.contains(";")) {
								dest_name = destination_no.substring(0, destination_no.indexOf(";"));
								destination_no = destination_no.substring(destination_no.indexOf(";") + 1,
										destination_no.length());
							}
							if (messageType.equalsIgnoreCase("Unicode")) {
								String perso_message = Converter.getUTF8toHexDig(greet + " " + dest_name + " ");
								cont_msg = perso_message + message1;
							} else {
								cont_msg = greet + " " + dest_name + " " + message1;
							}
						}
						list.add(cont_msg);
					}
					if (list != null) {
						while (!list.isEmpty()) {
							String message = (String) list.remove(0);
							// System.out.println("Message => "+message);
							// System.out.println("Message Length => "+message.length());
							if (messageType.equalsIgnoreCase("Unicode") && message.length() > 280) {
								msglist = callUnicodeMessage(message, destination_no, sender, ston, snpi);
								// System.out.println("Message_list ==> "+msglist.size());
							} else if (messageType.equals("SpecialChar") && (message.length() > 160)) {
								msglist = callSpecialMessage(message, destination_no, sender, ston, snpi);
							} else {
								SubmitSM msg = new SubmitSM();
								if (messageType.equalsIgnoreCase("Unicode")) {
									msg.setShortMessage(getUnicode(message.toCharArray()), Data.ENC_UTF16_BE);
									msg.setDataCoding((byte) 8);
									msg.setRegisteredDelivery((byte) 1);
								} else if (messageType.equalsIgnoreCase("SpecialChar")) {
									msg.setShortMessage(message, "ISO8859_1");
									msg.setDataCoding((byte) 0);
									msg.setEsmClass((byte) 0);
									msg.setRegisteredDelivery((byte) 1);
								}
								msg.setSourceAddr((byte) ston, (byte) snpi, sender);
								msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
								msglist.add(msg);
							}
							while (msglist.size() > 0) {
								SubmitSM msg = null;
								try {
									msg = (SubmitSM) msglist.remove(0);
									session.submit(msg);
									sequencemap.put(msg.getSequenceNumber(), id);
									if (delay > 0) {
										try {
											Thread.sleep((long) ((double) delay * 1000));
										} catch (InterruptedException ie) {
										}
									} else if (force_delay > 0) {
										try {
											Thread.sleep((long) ((double) force_delay * 1000));
										} catch (InterruptedException ie) {
										}
									}
									msgCounter++;
								} catch (Exception ex) {
									FileUtil.writeObject(filename, backupObject);
									closeSession();
									logger.error(user + " Connection Error(2). Id: " + id + " & Queue Size: "
											+ backupObject.noList.size() + " Error: " + ex.getMessage());
									connect = false;
									int connect_attempt = 0;
									while (active) {
										Thread.sleep(10 * 1000);
										try {
											session = new SMPPConnection().transmitter(user, password, sequencemap,
													false);
											if (session != null) {
												com.logica.smpp.pdu.SubmitSMResp resp = session.submit(msg);
												if (resp.getCommandStatus() == Data.ESME_ROK) {
													System.out.println("<== " + user + " Message Submitted.Status ==> "
															+ resp.getCommandStatus());
													msgCounter++;
													connect = true;
												} else {
													logger.error(user + " Submit Error.Id: " + id + " & Queue Size: "
															+ backupObject.noList.size() + " & Command Status: "
															+ resp.getCommandStatus());
												}
												stopSession();
												if (connect) {
													createConnection();
													break;
												}
											} else {
												if (++connect_attempt > 3) {
													// put to waiting Queue
													active = false;
													logger.info("Connection Attempt limit Exceed. Paused Batch: " + id);
												}
											}
										} catch (Exception inex) {
											logger.error(user + " Connection Error(3).Id: " + id + " & Queue Size: "
													+ backupObject.noList.size() + " Error: " + inex.getMessage());
										}
									}
								}
							}
							backupObject.setCount((int) counter);
							if (msgCounter % IConstants.THROUGHPUT == 0 || counter % IConstants.THROUGHPUT == 0) {
								// System.out.println("Message Counter: " + msgCounter+" & Number Counter: "+counter);
								logger.info("<" + id + ">" + "< " + user + " >< " + backupObject.getSender()
										+ "> Submitted: " + counter + " & Remained: " + backupObject.noList.size());
								FileUtil.writeObject(filename, backupObject);
								Thread.sleep(IConstants.SLEEP);
							}
						}
					}
				} else {
					logger.info(
							user + " Processing Complete.Id: " + id + " & Queue Size: " + backupObject.noList.size());
					break;
				}
			}
			if (active) {
				if (backupObject.isIsAlert() && backupObject.getAlertNumber() != null) {
					String alertMsg = "Dear User " + user + ",\n";
					alertMsg += "Your campaign with Sender " + sender + " is done.\n";
					alertMsg += "Started date: " + startDate + " " + startTime + "\n";
					alertMsg += "Done date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "\n";
					alertMsg += "Thank you for using our SMS Services.";
					try {
						long number = Long.parseLong(backupObject.getAlertNumber());
						SubmitSM msg = new SubmitSM();
						msg.setShortMessage(alertMsg, "ISO8859_1");
						msg.setDataCoding((byte) 0);
						msg.setEsmClass((byte) 0);
						msg.setRegisteredDelivery((byte) 1);
						msg.setSourceAddr((byte) 5, (byte) 0, IConstants.ALERT_SENDER);
						msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, number + "");
						session.submit(msg);
						logger.info("< Alert Message Sent > " + number);
					} catch (NumberFormatException ne) {
						logger.info(user + " Alert Message Sending Error.Id: " + id + " & Queue Size: "
								+ backupObject.noList.size() + " Error: " + ne);
					} catch (Exception ex) {
						logger.info(user + " Alert Message Sending Error.Id: " + id + " & Queue Size: "
								+ backupObject.noList.size() + " Error: " + ex);
					}
				}
			} else {
				if (!remove) {
					FileUtil.writeObject(filename, backupObject);
					GlobalVar.pausedBatch.put(id, backupObject);
				}
			}
			logger.info(
					user + " Stopping Processing Thread.Id: " + id + " & Queue Size: " + backupObject.noList.size());
			stop();
		} catch (Exception e) {
			logger.error(user + " Processing Error: ", e);
		}
	}

	private void createConnection() {
		logger.debug(user + " createConnection()");
		int connect_attempt = 0;
		if (GlobalVar.UserSession.containsKey(user)) {
			EnquireThread enquireThread = (EnquireThread) GlobalVar.UserSession.remove(user);
			session = enquireThread.getSession();
			enquireThread.stop();
			connect = true;
		} else {
			connect = false;
			while (!connect) {
				logger.info(user + " Trying to connect<" + connect_attempt + ">");
				try {
					session = new SMPPConnection().transmitter(user, password, sequencemap, true);
					if (session != null) {
						connect = true;
					} else {
						// check for 3 attempt & put to waitingQueue
						if (++connect_attempt > 3) {
							logger.info("Connection Attempt limit Exceed. Paused Batch: " + id);
							active = false;
							break;
						}
					}
				} catch (Exception ex) {
					logger.error(user + " Connection Error(1).Id: " + id + " & Queue Size: "
							+ backupObject.noList.size() + " Error: " + ex.getMessage());
				}
				if (!active) {
					break;
				} else {
					if (!connect) {
						try {
							Thread.sleep(10 * 1000);
						} catch (InterruptedException ie) {
						}
					}
				}
			}
		}
		logger.info(user + " createConnection(" + connect_attempt + ") exit");
	}

	private void closeSession() {
		try {
			session.close();
			logger.info(user + " Breaked Session Closed.Process Id: " + id + " & Queue Size: "
					+ backupObject.noList.size());
		} catch (IOException inex) {
			logger.error("Closing Session Error(1): " + inex);
		} catch (WrongSessionStateException inex) {
			logger.error("Closing Session Error(2): " + inex);
		}
	}

	private void stopSession() {
		logger.debug(user + " stopSession()");
		try {
			UnbindResp unbind = session.unbind();
			logger.info(user + " -> " + unbind.debugString());
			logger.info(user + " Session Stopped.Process Id: " + id + " & Queue Size: " + backupObject.noList.size());
		} catch (IOException inex) {
			logger.error("Stop Session Error(1): " + inex);
		} catch (WrongSessionStateException inex) {
			logger.error("Stop Session Error(2): " + inex);
		} catch (TimeoutException | PDUException ex) {
			logger.error("Stop Session Error(3): " + ex);
		}
		logger.debug(user + " stopSession() exit");
	}

	private void deleteBackupFile() {
		logger.debug(user + " deleteBackupFile()");
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
		}
		file = null;
		logger.info(user + " Object File Deleted.Id: " + id + " & Queue Size: " + backupObject.noList.size());
	}

	private void stop() {
		logger.info(user + " Sequence Cache Size: " + sequencemap.size());
		GlobalVar.processmap.remove(id);
		if (active) {
			deleteBackupFile();
			databaseUtil.deleteFileBackupEntry(id);
		} else {
			if (remove) {
				deleteBackupFile();
				databaseUtil.deleteFileBackupEntry(id);
			} else {
				// update status as paused
				databaseUtil.setBatchStatus(id, false);
			}
		}
		try {
			Thread.sleep(15 * 1000);
		} catch (InterruptedException ex) {
		}
		if (GlobalVar.UserSession.containsKey(user)) {
			stopSession();
		} else {
			if (connect) {
				EnquireThread enquireThread = new EnquireThread(userDTO, session);
				new Thread(enquireThread).start();
				GlobalVar.UserSession.put(user, enquireThread);
			}
		}
		logger.info(user + " Processing Thread Stopped.Id: " + id + " & Queue Size: " + backupObject.noList.size());
	}

	private List callUnicodeMessage(String first, String destination_no, String sender, int ston, int snpi) {
		List list = new ArrayList();
		try {
			// int rn = 100;
			List parts = Body.getUnicodeno(first);
			// StringTokenizer stkmsg = new StringTokenizer(first1, "##");
			int nofmessage = parts.size();
			int i = 1;
			while (!parts.isEmpty()) {
				int rn = 100;
				String msg = (String) parts.remove(0);
				msg = Converter.getUnicode(msg.toCharArray());
				ByteBuffer byteMessage = new ByteBuffer();
				byteMessage.appendByte((byte) 0x05);
				byteMessage.appendByte((byte) 0x00);
				byteMessage.appendByte((byte) 0x03);
				byteMessage.appendByte((byte) rn);
				byteMessage.appendByte((byte) nofmessage);
				byteMessage.appendByte((byte) i);
				byteMessage.appendString(msg, Data.ENC_UTF16_BE);
				SubmitSM sm_msg = new SubmitSM();
				sm_msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
				sm_msg.setSourceAddr((byte) ston, (byte) snpi, sender);
				sm_msg.setShortMessage(byteMessage);
				sm_msg.setEsmClass((byte) 0x40);
				sm_msg.setDataCoding((byte) 8);
				/*
				 * sm_msg.setSarMsgRefNum((short) rn); sm_msg.setSarSegmentSeqnum((short) i); sm_msg.setSarTotalSegments((short) nofmessage);
				 */
				list.add(sm_msg);
				i++;
			}
		} catch (Exception e) {
			logger.error("<---- Error in Concat Unicode Message -----> " + e);
		}
		return list;
	}

	private static String getUnicode(char[] buffer) {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 4];
		try {
			for (int i = 0; i < buffer.length; i += 4) {
				code += Character.digit(buffer[i], 16) * 4096;
				code += Character.digit(buffer[i + 1], 16) * 256;
				code += Character.digit(buffer[i + 2], 16) * 16;
				code += Character.digit(buffer[i + 3], 16);
				unibuffer[j++] = (char) code;
				code = 0;
			}
			unicode = new String(unibuffer);
		} catch (Exception e) {
			System.out.println("Excepiton in getUnicode3333333 " + e);
		}
		return unicode;
	}

	private List callSpecialMessage(String first, String destination_no, String sender, int ston, int snpi) {
		List list = new ArrayList();
		try {
			List parts = Body.getEnglishno(first);
			// StringTokenizer stkmsg = new StringTokenizer(first, "##");
			int nofmessage = parts.size();
			int i = 1;
			while (!parts.isEmpty()) {
				int rn = 100;
				int length1 = 0;
				first = (String) parts.remove(0);
				length1 = (first.length() + 6);
				ByteBuffer bf = new ByteBuffer();
				bf.appendBuffer(Body.getHead());
				bf.appendByte((byte) length1);
				bf.appendBuffer(Body.getHeader(rn, nofmessage, i));
				bf.appendString(first);
				SubmitSM msg = new SubmitSM();
				msg.setBody(bf);
				msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
				msg.setSourceAddr((byte) ston, (byte) snpi, sender);
				msg.setDataCoding((byte) 0);
				msg.setEsmClass((byte) 0x40);
				/*
				 * msg.setSarMsgRefNum((short) rn); msg.setSarSegmentSeqnum((short) i); msg.setSarTotalSegments((short) nofmessage);
				 */
				// processQueue.enqueue(msg);
				list.add(msg);
				i++;
			}
		} catch (Exception e) {
			logger.error("<--- Error in Concat Unicode Message --->" + e);
		}
		return list;
	}
}
