/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.util;

/**
 *
 * @author Administrator
 */
public class IConstants {

    public static int PORT = 1099;
    public static String HLR_SERVER_IP = "localhost";
    public static int HLR_SERVER_PORT = 2775;
    public static int HLR_THROUGHPUT = 50;
    public static int HLR_SLEEP = 2000;
    public static String ALERT_SENDER = "AMSALERT";
    public static String RESP_LOG_FILE = "log//Submit_Response.log";
    public static String HLR_RESP_LOG_FILE = "log//HLRSubmit_Response.log";
    public static String DLR_LOG_FILE = "log//Deliver_Response.log";
    public static String OBJECT_FILE_PATH = "files//";
    public static String LOOKUP_OBJECT_FILE_PATH = "files//hlr//";
    public static String SMPP_IP = "localhost";
    public static int SMPP_PORT = 8899;
    public static int THROUGHPUT = 50;
    public static int SLEEP = 2000;
    public static String CONNECTION_URL = "jdbc:mysql://localhost:3306/host_zubaidi";
    public static String HLR_DATABASE = "hlr_brd";
    public static String USERNAME = "root";
    public static String PASSWORD = "root";
    public static int SESSION_ALIVE = 5;
    public static final String LOG4J_CFG_FILE = "config//log4j.properties";
}
