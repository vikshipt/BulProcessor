/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package com.hti.user;

import java.io.InterruptedIOException;
import java.io.IOException;

import com.logica.smpp.Connection;
import org.apache.log4j.Logger;

/**
 * This class accepts client connection on given port. When the connection is
 * accepted, the listener creates an instance of <code>SMSCSession</code>,
 * generates new <code>PDUProcessor</code> using object derived from
 * <code>PDUProcessorFactory</code>, passes the processor to the smsc session
 * and starts the session as a standalone thread.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version $Revision: 1.2 $
 * @see SMSCSession
 * @see PDUProcessor
 * @see PDUProcessorFactory
 */
public class UserListener implements Runnable {

    private Logger logger = Logger.getLogger(UserListener.class);
    private Connection serverConn = null;
    private int port;
    private long acceptTimeout = com.logica.smpp.Data.ACCEPT_TIMEOUT;
    private boolean keepReceiving = true;
    private boolean isReceiving = false;
    private boolean asynchronous = false;

    /**
     * Construct synchronous listener listening on the given port.
     *
     * @param port the port to listen on
     * @see #UserListenerImpl(int,boolean)
     */
    public UserListener(int port) {
        this.port = port;
    }

    /**
     * Constructor with control if the listener starts as a separate thread. If
     * <code>asynchronous</code> is true, then the listener is started as a
     * separate thread, i.e. the creating thread can continue after calling of
     * method <code>start</code>. If it's false, then the caller blocks while
     * the listener does it's work, i.e. listening.
     *
     * @param port the port to listen on
     * @param asynchronous if the listening will be performed as separate thread
     * @see #start()
     */
    public UserListener(int port, boolean asynchronous) {
        this.port = port;
        this.asynchronous = asynchronous;
    }

    /**
     * Starts the listening. If the listener is asynchronous (reccomended), then
     * new thread is created which listens on the port and the
     * <code>start</code> method returns to the caller. Otherwise the caller is
     * blocked in the start method.
     *
     * @see #stop()
     */
    public synchronized void start() throws IOException {
        logger.trace("going to start UserListener on port " + port);
        if (!isReceiving) {
            serverConn = new com.logica.smpp.TCPIPConnection(port);
            serverConn.setReceiveTimeout(getAcceptTimeout());
            serverConn.open();
            keepReceiving = true;
            if (asynchronous) {
                logger.trace("starting listener in separate thread.");
                Thread serverThread = new Thread(this);
                serverThread.start();
                logger.trace("listener started in separate thread.");
            } else {
                logger.trace("going to listen in the context of current thread.");
                run();
            }
        } else {
            logger.trace("already receiving, not starting the listener.");
        }
    }

    /**
     * Signals the listener that it should stop listening and wait until the
     * listener stops. Note that based on the timeout settings it can take some
     * time befor this method is finished -- the listener can be blocked on i/o
     * operation and only after exiting i/o it can detect that it should stop.
     *
     * @see #start()
     */
    public synchronized void stop() throws IOException {
        logger.trace("going to stop UserListener on port " + port);
        keepReceiving = false;
        while (isReceiving) {
            Thread.yield();
        }
        serverConn.close();
        logger.trace("UserListener stopped on port " + port);
    }

    /**
     * The actual listening code which is run either from the thread (for async
     * listener) or called from <code>start</code> method (for sync listener).
     * The method can be exited by calling of method <code>stop</code>.
     *
     * @see #start()
     * @see #stop()
     */
    public void run() {
        isReceiving = true;
        try {
            while (keepReceiving) {
                listen();
                Thread.yield();
            }
        } finally {
            isReceiving = false;
        }
    }

    /**
     * The "one" listen attempt called from <code>run</code> method. The
     * listening is atomicised to allow contoled stopping of the listening. The
     * length of the single listen attempt is defined by
     * <code>acceptTimeout</code>. If a connection is accepted, then new session
     * is created on this connection, new PDU processor is generated using PDU
     * processor factory and the new session is started in separate thread.
     *
     * @see #run()
     * @see com.logica.smpp.Connection
     * @see SMSCSession
     * @see PDUProcessor
     * @see PDUProcessorFactory
     */
    private void listen() {
        try {
            Connection connection = null;
            serverConn.setReceiveTimeout(getAcceptTimeout());
            connection = serverConn.accept();

            if (connection != null) {
                logger.trace("UserListener accepted a connection on port " + port);
                UserSession session = new UserSession(connection);
                PDUProcessor pduProcessor = new PDUProcessor(session);
                session.setPDUProcessor(pduProcessor);
                Thread thread = new Thread(session);
                thread.start();
                logger.trace("UserListener launched a session on the accepted connection.");
            } 
        } catch (InterruptedIOException e) {
            // thrown when the timeout expires => it's ok, we just didn't
            // receive anything
           // logger.trace("InterruptedIOException accepting, timeout? -> " + e);
        } catch (IOException e) {
            // accept can throw this from various reasons
            // and we don't want to continue then (?)
            logger.error("IOException accepting connection", e);
            keepReceiving = false;
        }
    }

    /**
     * Sets new timeout for accepting new connection. The listening blocks the
     * for maximum this time, then it exits regardless the connection was
     * acctepted or not.
     *
     * @param value the new value for accept timeout
     */
    public void setAcceptTimeout(int value) {
        acceptTimeout = value;
    }

    /**
     * Returns the current setting of accept timeout.
     *
     * @return the current accept timeout
     * @see #setAcceptTimeout(int)
     */
    public long getAcceptTimeout() {
        return acceptTimeout;
    }
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/09/30 09:17:49  sverkera
 * Created an interface for UserListener and SMSCSession and implementations of them  so that it is possible to provide other implementations of these classes.
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 * 
 * Old changelog:
 * 23-08-01 ticp@logica.com added yield() to run() to give chance to other
 *						    threads
 * 26-09-01 ticp@logica.com debug now in a group
 */
