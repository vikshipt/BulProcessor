/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.hti.hlr.BulkBackup;
import com.hti.init.Starter;
import com.hti.user.UserDTO;
import com.hti.util.IConstants;
import com.hti.util.TextEncoder;

/**
 * @author Administrator
 */
public class DatabaseUtil {
	private Logger logger = Logger.getLogger(DatabaseUtil.class);

	public DatabaseUtil() {
	}

	public BulkBackup getBackupEntry(String filename) {
		BulkBackup backup = null;
		String query = "select * from " + IConstants.HLR_DATABASE + ".backupfile where filename = ? ";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(query);
			pStmt.setString(1, filename);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				backup = new BulkBackup();
				backup.setId(rs.getInt("id"));
				backup.setSystemid(rs.getString("username"));
				backup.setPassword(rs.getString("password"));
				backup.setFilename(filename);
				backup.setBatchid(rs.getString("batchid"));
				backup.setThreadCount(rs.getInt("thread_count"));
			}
		} catch (SQLException sqle) {
			logger.error("Record Check Error .Filename: " + filename, sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
		return backup;
	}

	public int addFileBackupEntry(BulkBackup backup) throws SQLException {
		int generatedID = 0;
		String insertQry = "insert into " + IConstants.HLR_DATABASE + ".backupfile "
				+ "(username,password,batchid,filename,thread_count) values (?,?,?,?,?)";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(insertQry, Statement.RETURN_GENERATED_KEYS);
			pStmt.setString(1, backup.getSystemid());
			pStmt.setString(2, backup.getPassword());
			pStmt.setString(3, backup.getBatchid());
			pStmt.setString(4, backup.getFilename());
			pStmt.setInt(5, backup.getThreadCount());
			int a = pStmt.executeUpdate();
			if (a != 0) {
				rs = pStmt.getGeneratedKeys();
				if (rs.next()) {
					generatedID = rs.getInt(1);
				}
			}
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
		logger.info("Record Inserted .Filename: " + backup.getFilename() + " Id:" + generatedID);
		return generatedID;
	}

	public int addFileBackupEntry(String user, String filename, String senderID, String startDate, String startTime,
			String totalNum, String firstNumber, double delay, String reqType) throws SQLException {
		int generatedID = checkFileBackupEntry(filename);
		String insertQry = "insert into backupfile "
				+ "(filename,name,senderId,startDate,startTime,totalNum,firstNum,delay,reqType) values (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY update delay=?,senderId=?,active=?";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(insertQry, Statement.RETURN_GENERATED_KEYS);
			pStmt.setString(1, filename);
			pStmt.setString(2, user);
			pStmt.setString(3, senderID);
			pStmt.setString(4, startDate);
			pStmt.setString(5, startTime);
			pStmt.setString(6, totalNum);
			pStmt.setString(7, firstNumber);
			pStmt.setDouble(8, delay);
			pStmt.setString(9, reqType);
			pStmt.setDouble(10, delay);
			pStmt.setString(11, senderID);
			pStmt.setBoolean(12, true);
			int a = pStmt.executeUpdate();
			if (a != 0) {
				rs = pStmt.getGeneratedKeys();
				if (rs.next()) {
					generatedID = rs.getInt(1);
				}
			}
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
		logger.info("Record Inserted .Filename: " + filename + " Id:" + generatedID);
		return generatedID;
	}

	public int checkFileBackupEntry(String filename) {
		int id = 0;
		String query = "select id from backupfile where filename = ? ";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(query);
			pStmt.setString(1, filename);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				id = rs.getInt("id");
			}
		} catch (SQLException sqle) {
			logger.error("Record Check Error .Filename: " + filename, sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
		return id;
	}

	public UserDTO getUserObject(String username) throws Exception {
		UserDTO user = null;
		String query = "select system_id,password,timeout,force_delay from usermaster where system_id=?";
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Connection connection = null;
		String password = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(query);
			pStmt.setString(1, username);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				user = new UserDTO();
				try {
					password = new TextEncoder().decode(rs.getString("password"));
				} catch (Exception ex) {
					logger.error(username + " Password Decode Error: " + rs.getString("password"));
					throw new Exception(username + " Password Decode Error: " + rs.getString("password"));
				}
				user.setSystemid(rs.getString("system_id"));
				user.setTimeout(rs.getInt("timeout"));
				user.setPassword(password);
				user.setForceDelay(rs.getDouble("force_delay"));
			}
		} catch (SQLException sqle) {
			logger.error("Fetch User Details Error: ", sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
		return user;
	}

	/*
	 * public Map getForceDelay() { Map timeoutmap = new HashMap(); double delay = 0; String query =
	 * "select username,force_delay from registrymaster order by username ASC"; PreparedStatement pStmt = null; ResultSet rs = null; Connection
	 * connection = null; try { connection = Starter.dbConnection.getConnection(); pStmt = connection.prepareStatement(query); rs =
	 * pStmt.executeQuery(); while (rs.next()) { delay = rs.getDouble("force_delay"); if (delay > 0) { timeoutmap.put(rs.getString("username"),
	 * delay); } } } catch (SQLException sqle) { logger.error("Fetch User Delay Error: ", sqle); } finally { try { if (pStmt != null) { pStmt.close();
	 * pStmt = null; } } catch (SQLException sqle) { } if (connection != null) { Starter.dbConnection.putConnection(connection); } } return
	 * timeoutmap; }
	 */
	public boolean deleteLookupEntry(int id) {
		boolean result = false;
		String query = "delete from " + IConstants.HLR_DATABASE + ".backupfile where id = ? ";
		PreparedStatement pStmt = null;
		Connection connection = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(query);
			pStmt.setInt(1, id);
			int ans = pStmt.executeUpdate();
			if (ans != 0) {
				result = true;
			}
		} catch (SQLException sqle) {
			logger.error("Record Deletion Error .Id: " + id, sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
		logger.debug("Database Record Deleted.Id: " + id);
		return result;
	}

	public boolean deleteFileBackupEntry(int id) {
		boolean result = false;
		String query = "delete from backupfile where id = ? ";
		PreparedStatement pStmt = null;
		Connection connection = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(query);
			pStmt.setInt(1, id);
			int ans = pStmt.executeUpdate();
			if (ans != 0) {
				result = true;
			}
		} catch (SQLException sqle) {
			logger.error("Record Deletion Error .Id: " + id, sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
		logger.info("Database Record Deleted.Id: " + id);
		return result;
	}

	public void setBatchStatus(int id, boolean status) {
		// boolean result = false;
		String query = "update backupfile set active=? where id = ? ";
		PreparedStatement pStmt = null;
		Connection connection = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(query);
			pStmt.setBoolean(1, status);
			pStmt.setInt(2, id);
			int ans = pStmt.executeUpdate();
		} catch (SQLException sqle) {
			logger.error("setBatchStatus Error .Id: " + id, sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
	}

	public boolean getBatchStatus(int id) {
		boolean status = true;
		String query = "select active from backupfile where id = ? ";
		PreparedStatement pStmt = null;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = Starter.dbConnection.getConnection();
			pStmt = connection.prepareStatement(query);
			pStmt.setInt(1, id);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				status = rs.getBoolean("active");
			}
		} catch (SQLException sqle) {
			logger.error("getBatchStatus Error .Id: " + id, sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
			} catch (SQLException sqle) {
			}
			if (connection != null) {
				Starter.dbConnection.putConnection(connection);
			}
		}
		return status;
	}
}
