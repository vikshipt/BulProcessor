/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.proc;

import com.hti.user.UserDTO;
import com.hti.util.GlobalVar;
import com.hti.util.IConstants;
import com.logica.smpp.Session;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.UnbindResp;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class EnquireThread implements Runnable {

    String username = null;
    Session session = null;
    private boolean stop = false;
    private boolean enquire = true;
    private long duration = 0;
    private long timeout = 15;
    private Logger logger = Logger.getLogger(EnquireThread.class);

    public EnquireThread(UserDTO user, Session session) {
        this.username = user.getSystemid();
        timeout = user.getTimeout();
        this.session = session;
        duration = System.currentTimeMillis() + (60000 * IConstants.SESSION_ALIVE);
        logger.info("Enquire Thread Starting : " + username + " Timeout : " + timeout);
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                logger.info("Enquiring Session : " + username);
                session.enquireLink();
            } catch (Exception ex) {
                logger.info("Enquire Session Error : " + ex);
                closeSession();
                break;
            }
            if (System.currentTimeMillis() > duration) {
                enquire = false;
            }
            if (!enquire) {
                logger.info("Removing Enquire Session : " + username);
                GlobalVar.UserSession.remove(username);
                stopSession();
                break;
            }
            try {
                Thread.sleep(timeout * 1000);
            } catch (InterruptedException ex) {
            }
        }
        logger.info("Enquire Thread Stopped : " + username);
    }

    private void stopSession() {
        logger.info(username + " Enquire Session Stopping.");
        try {
            UnbindResp unbind = session.unbind();
            if (unbind != null) {
                logger.info(username + "Enquire Session Closed." + unbind.debugString());
            } else {
                logger.info(username + " Unbind Response Not Received");
                closeSession();
            }
        } catch (Exception ex) {
            logger.error(username + "Enquire Stop Session Error: " + ex);
        }
    }

    private void closeSession() {
        logger.info(username + " Breaked Enquire Session Closing.");
        try {
            session.close();
            logger.info(username + " Breaked Enquire Session Closed");
        } catch (IOException inex) {
            logger.error("Closing Session Error(1): " + inex);
        } catch (WrongSessionStateException inex) {
            logger.error("Closing Session Error(2): " + inex);
        }
    }

    public void stop() {
        logger.info("Enquire Thread Stopping : " + username);
        stop = true;
    }
}
