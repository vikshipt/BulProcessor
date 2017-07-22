/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hti.rmi;

import com.logica.smpp.pdu.SubmitSM;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class QueueBackup implements Serializable {

    private int id;
    private int ston;
    private int snpi;
    private String user = null;
    private String password = null;
    private String header = null;
   // private SubmitSM msg = null;
    public ArrayList bulkMsg = new ArrayList();
    public boolean isBulkUpload = false;
    public ArrayList noList = new ArrayList();
    private String messageType = null;
    private String message = null;
    private String sender = null;
    private String greet;
    private String fileName = null;
    private String backupFileName = null;
    private int count = 0;
    public boolean boolvalue;
    private String complete;
    private String totalNumbers;
    private String firstNumber;
    private String queuedDate;
    private String queuedTime;
    private String from;
    private double delay;
    private String reqType;
    private String distinct = "no";
    public Hashtable bulkMap = new Hashtable();
    public boolean clearmap = false;
    private Hashtable mapTable;
    public Map processMap = new HashMap();
    private boolean isAlert;
    private String alertNumber;
    private String origMessage; 

    public String getOrigMessage() {
        return origMessage;
    }

    public void setOrigMessage(String origMessage) {
        this.origMessage = origMessage;
    }

    public String getAlertNumber() {
        return alertNumber;
    }

    public void setAlertNumber(String alertNumber) {
        this.alertNumber = alertNumber;
    }

    public boolean isIsAlert() {
        return isAlert;
    }

    public void setIsAlert(boolean isAlert) {
        this.isAlert = isAlert;
    }

    public QueueBackup() {
    }

    public QueueBackup(QueueBackup queueBackup) {
        this.user = queueBackup.getUser();
        this.password = queueBackup.getPassword();
        this.header = queueBackup.getHeader();
        this.noList = queueBackup.getNoList();
        this.messageType = queueBackup.getMessageType();
        this.message = queueBackup.getMessage();
        this.sender = queueBackup.getSender();
        this.greet = queueBackup.getGreet();
        this.fileName = queueBackup.getFileName();
        this.totalNumbers = queueBackup.getTotalNumbers();
        this.firstNumber = queueBackup.getFirstNumber();
        this.queuedDate = queueBackup.getQueuedDate();
        this.from = queueBackup.getFrom();
        this.ston = queueBackup.getSton();
        this.snpi = queueBackup.getSnpi();
        this.delay = queueBackup.getDelay();
        this.mapTable = queueBackup.getMapTable();
        this.distinct = queueBackup.getDistinct();
        this.processMap = queueBackup.getProcessMap();
        this.count = queueBackup.getCount();
        this.reqType = queueBackup.getReqType();
    }

    public Map getProcessMap() {
        return processMap;
    }

    public void setProcessMap(Map processMap) {
        this.processMap = processMap;
    }

    public Hashtable getMapTable() {
        return mapTable;
    }

    public void setMapTable(Hashtable mapTable) {
        this.mapTable = mapTable;
    }

    public Map getBulkMap() {
        return bulkMap;
    }

    public void setBulkMap(Hashtable bulkMap) {
        this.bulkMap = bulkMap;
    }

    public int getSnpi() {
        return snpi;
    }

    public void setSnpi(int snpi) {
        this.snpi = snpi;
    }

    public int getSton() {
        return ston;
    }

    public void setSton(int ston) {
        this.ston = ston;
    }

    public String getDistinct() {
        return distinct;
    }

    public void setDistinct(String distinct) {
        this.distinct = distinct;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQueuedDate() {
        return queuedDate;
    }

    public void setQueuedDate(String queuedDate) {
        this.queuedDate = queuedDate;
    }

    public String getQueuedTime() {
        return queuedTime;
    }

    public void setQueuedTime(String queuedTime) {
        this.queuedTime = queuedTime;
    }

    public String getFirstNumber() {
        return firstNumber;
    }

    public void setFirstNumber(String firstNumber) {
        this.firstNumber = firstNumber;
    }

    public String getTotalNumbers() {
        return totalNumbers;
    }

    public void setTotalNumbers(String totalNumbers) {
        this.totalNumbers = totalNumbers;
    }
    public boolean isFileUpload = false;

    public void setNoList(ArrayList noList) {
        this.noList = noList;
    }

    public void setIsBulkUpload(boolean isBulkUpload) {
        this.isBulkUpload = isBulkUpload;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBoolvalue(boolean boolvalue) {
        this.boolvalue = boolvalue;
    }

   /* public void setMsg(SubmitSM msg) {
        this.msg = msg;
    }*/

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setGreet(String greet) {
        this.greet = greet;

    }

    public void setFileName(String fileName) {
        this.fileName = fileName;

    }

    public void setBackupFileName(String backupFileName) {
        this.backupFileName = backupFileName;

    }

    public void setCount(int count) {
        this.count = count;

    }

    public void setComplete(String complete) {

        this.complete = complete;


    }
    // -----------------------------------getter------------------------ //

    public ArrayList getNoList() {
        return noList;
    }

    public boolean getIsBulkUpload() {
        return isBulkUpload;
    }

    public String getUser() {
        return user;
    }

    public boolean getBoolvalue() {

        return boolvalue;
    }

    public String getPassword() {
        return password;
    }

  /*  public SubmitSM getMsg() {
        return msg;
    }*/

    public ArrayList getBulkMsg() {
        return bulkMsg;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getComplete() {

        return complete;
    }

    public String getHeader() {
        return header;
    }

    public String getGreet() {
        return greet;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBackupFileName() {
        return backupFileName;
    }

    public int getCount() {
        return count;
    }
}


