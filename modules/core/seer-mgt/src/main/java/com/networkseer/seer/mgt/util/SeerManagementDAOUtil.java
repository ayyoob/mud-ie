package com.networkseer.seer.mgt.util;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeerManagementDAOUtil {

	private static final Logger log = LoggerFactory.getLogger(SeerManagementDAOUtil.class);

	public static void cleanupResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing result set", e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing prepared statement", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing database connection", e);
			}
		}
	}

	public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing result set", e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing prepared statement", e);
			}
		}
	}

	public static DataSource lookupDataSource(String dataSourceName) {
		try {
			return (DataSource) InitialContext.doLookup(dataSourceName);
		} catch (Exception e) {
			throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
		}
	}

}
