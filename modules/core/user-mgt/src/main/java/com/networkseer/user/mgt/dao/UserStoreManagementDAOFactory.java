package com.networkseer.user.mgt.dao;

import com.networkseer.user.mgt.dao.exception.IllegalTransactionStateException;
import com.networkseer.user.mgt.dao.exception.TransactionManagementException;
import com.networkseer.user.mgt.util.UserManagementDAOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class intends to act as the primary entity that hides all DAO instantiation related complexities and logic so
 * that the business objection handling layer doesn't need to be aware of the same providing seamless plug-ability of
 * different data sources, connection acquisition mechanisms as well as different forms of DAO implementations to the
 * high-level implementations that require device management related metadata persistence.
 * <p/>
 * In addition, this also provides means to handle transactions across multiple device management related DAO objects.
 * Any high-level business logic that requires transaction handling to be done via utility methods provided in
 * DeviceManagementDAOFactory should adhere the following guidelines to avoid any unexpected behaviour that can cause
 * as a result of improper use of the aforementioned utility method.
 * <p/>
 * Any transaction that commits data into the underlying data persistence mechanism MUST follow the sequence of
 * operations mentioned below.
 * <p/>
 * <pre>
 * {@code
 * try {
 *      UserStoreManagementDAOFactory.beginTransaction();
 *      .....
 *      UserStoreManagementDAOFactory.commitTransaction();
 *      return success;
 * } catch (Exception e) {
 *      UserStoreManagementDAOFactory.rollbackTransaction();
 *      throw new UserManagementException("Error occurred while ...", e);
 * } finally {
 *      UserStoreManagementDAOFactory.closeConnection();
 * }
 * }
 * </pre>
 * <p/>
 * Any transaction that retrieves data from the underlying data persistence mechanism MUST follow the sequence of
 * operations mentioned below.
 * <p/>
 * <pre>
 * {@code
 * try {
 *      UserStoreManagementDAOFactory.openConnection();
 *      .....
 * } catch (Exception e) {
 *      throw new UserManagementException("Error occurred while ..., e);
 * } finally {
 *      UserStoreManagementDAOFactory.closeConnection();
 * }
 * }
 * </pre>
 */
public class UserStoreManagementDAOFactory {
	private static final String jndiPrefix = "java:comp/env/jdbc/";
	private static final String datasourceName = "user_mgt";
	private static DataSource dataSource;
	private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();
	private static final Logger log = LoggerFactory.getLogger(UserStoreManagementDAOFactory.class);

	public static void init() {
		dataSource = UserManagementDAOUtil.lookupDataSource(jndiPrefix +datasourceName);
	}

	public static void beginTransaction() throws TransactionManagementException {
		Connection conn = currentConnection.get();
		if (conn != null) {
			throw new IllegalTransactionStateException("A transaction is already active within the context of " +
					"this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
					"transaction is already active is a sign of improper transaction handling");
		}
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			currentConnection.set(conn);
		} catch (SQLException e) {
			throw new TransactionManagementException("Error occurred while retrieving config.datasource connection", e);
		}
	}

	public static void openConnection() throws SQLException {
		Connection conn = currentConnection.get();
		if (conn != null) {
			throw new IllegalTransactionStateException("A transaction is already active within the context of " +
					"this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
					"transaction is already active is a sign of improper transaction handling");
		}
		conn = dataSource.getConnection();
		currentConnection.set(conn);
	}

	public static Connection getConnection() throws SQLException {
		Connection conn = currentConnection.get();
		if (conn == null) {
			throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
					"This might have ideally been caused by not properly initiating the transaction via " +
					"'beginTransaction'/'openConnection' methods");
		}
		return conn;
	}

	public static void commitTransaction() {
		Connection conn = currentConnection.get();
		if (conn == null) {
			throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
					"This might have ideally been caused by not properly initiating the transaction via " +
					"'beginTransaction'/'openConnection' methods");
		}
		try {
			conn.commit();
		} catch (SQLException e) {
			log.error("Error occurred while committing the transaction", e);
		}
	}

	public static void rollbackTransaction() {
		Connection conn = currentConnection.get();
		if (conn == null) {
			throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
					"This might have ideally been caused by not properly initiating the transaction via " +
					"'beginTransaction'/'openConnection' methods");
		}
		try {
			conn.rollback();
		} catch (SQLException e) {
			log.warn("Error occurred while roll-backing the transaction", e);
		}
	}

	public static void closeConnection() {
		Connection conn = currentConnection.get();
		if (conn == null) {
			throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
					"This might have ideally been caused by not properly initiating the transaction via " +
					"'beginTransaction'/'openConnection' methods");
		}
		try {
			conn.close();
		} catch (SQLException e) {
			log.warn("Error occurred while close the connection");
		}
		currentConnection.remove();
	}

}