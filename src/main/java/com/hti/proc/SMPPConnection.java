/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.proc;

import com.hti.util.IConstants;
import com.hti.util.PDUEventListenerImpl;
import com.logica.smpp.Connection;
import com.logica.smpp.Data;
import com.logica.smpp.ServerPDUEventListener;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.ValueNotSetException;
import com.logica.smpp.pdu.WrongLengthOfStringException;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class SMPPConnection {

    private Session session = null;
    ServerPDUEventListener pduListener = null;
    Response response = null;
    private Logger logger = Logger.getLogger(SMPPConnection.class);

    public Session transmitter(String username, String password, Map sequencemap, boolean asynchronous) throws
            WrongLengthOfStringException,
            ValueNotSetException,
            IOException,
            WrongSessionStateException,
            TimeoutException,
            PDUException {
        logger.debug(username + " transmitter()");
        Connection connection = new TCPIPConnection(IConstants.SMPP_IP, IConstants.SMPP_PORT);
        session = new Session(connection);
        BindRequest breq = new BindTransmitter();
        breq.setSystemId(username);
        breq.setPassword(password);
        breq.setInterfaceVersion(Data.SMPP_V34);
        if (asynchronous) {
            pduListener = new PDUEventListenerImpl(sequencemap,username);
            response = session.bind(breq, pduListener);
        } else {
            response = session.bind(breq);
        }
        logger.debug(username + " " + response.debugString());
        if (response.getCommandStatus() == Data.ESME_ROK) {
            logger.info(username + " Connected Transmitter : " + response.debugString());
        } else if (response.getCommandStatus() == 1035) {
            session = null;
            logger.error(username + " Insufficient balance : " + response.debugString());
        } else if (response.getCommandStatus() == Data.ESME_RBINDFAIL) {
            session = null;
            logger.error(username + " Bind Failed : " + response.debugString());
        } else {
            session = null;
            logger.error(username + " Connection Failed : " + response.debugString());
        }
        logger.debug(username + " transmitter() exit");
        return session;
    }
}
