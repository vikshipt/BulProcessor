/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.database;

import com.hti.util.IConstants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class DBConnection {

    private static Logger logger = Logger.getLogger(DBConnection.class);
    private static final List ConnectionPool = new ArrayList();
    private static int MAX_CONNECTION = 20;

    static {
        logger.info("Initializing Connection Pool");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            for (int i = 0; i < MAX_CONNECTION; i++) {
                Connection con = DriverManager.getConnection(IConstants.CONNECTION_URL, IConstants.USERNAME, IConstants.PASSWORD);
                ConnectionPool.add(con);
            }
        } catch (Exception ex) {
            logger.error("Connection Pool Initialize Error: " + ex);
        }
        logger.info("Total Connection Stored: " + ConnectionPool.size());
    }

    public synchronized Connection getConnection() {
        logger.trace("getConnection(): " + ConnectionPool.size());
        boolean isCreate = false;
        Connection connection = null;
        if (!ConnectionPool.isEmpty()) {
            connection = (Connection) ConnectionPool.remove(0);
            if (connection != null) {
                try {
                    if (!connection.isValid(0)) {
                        logger.warn("<--- Invalid Database Connection Found  --->");
                        isCreate = true;
                    }
                } catch (SQLException ex) {
                    isCreate = true;
                    logger.warn("<- SQLException While Validating Connection -> ");
                }
            }
            if (isCreate) {
                logger.warn("<--- Creating Additional Connection.Pooled:-> " + ConnectionPool.size());
                connection = createConnection();
            }
        } else {
            logger.warn("<--- No Pool Connection Found --->");
            while (true) {
                try {
                    logger.info("<-- Waiting For Database Connection --> ");
                    wait();
                    logger.info("<-- Exit Waiting For Database Connection --> ");
                } catch (InterruptedException ex) {
                    logger.debug("<- InterruptedException While Waiting For Connection -> ");
                }
                try {
                    if (!ConnectionPool.isEmpty()) {
                        connection = (Connection) ConnectionPool.remove(0);
                        break;
                    }
                } catch (Exception ex) {
                    logger.warn(ex);
                }
            }
        }
        return connection;
    }

    private Connection createConnection() {
        logger.debug("createConnection()");
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(IConstants.CONNECTION_URL, IConstants.USERNAME, IConstants.PASSWORD);
        } catch (Exception ex) {
            logger.error("createConnection(): " + ex);
        }
        return connection;
    }

    /**
     *
     * @param connection
     */
    public synchronized void putConnection(Connection connection) {
        try {
            if (connection != null) {
                ConnectionPool.add(connection);
                notifyAll();
            }
        } catch (Exception ex) {
            logger.error("putConnection(): " + ex);
        }
    }
}
