package com.networkseer.parental.mgt.dao.impl;

import com.networkseer.parental.mgt.dao.ParentalDAO;
import com.networkseer.parental.mgt.dao.ParentalManagementDAOFactory;
import com.networkseer.parental.mgt.dto.DnsEntry;
import com.networkseer.parental.mgt.exception.ParentalManagementException;
import com.networkseer.seer.mgt.util.SeerManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParentalDAOImpl implements ParentalDAO {

	private static final int BATCH_SIZE = 1000;

	@Override
	public void addTags(List<DnsEntry> dnsEntries) throws ParentalManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			String sql = "INSERT INTO PARENTAL_TAG_RECORDS(DNS_DOMAIN, TAGS) " +
					"VALUES (?, ?)";
			int count = 0;
			stmt = conn.prepareStatement(sql);

			for (DnsEntry dnsEntry : dnsEntries) {
				stmt.setString(1, dnsEntry.getDnsDomain());
				stmt.setString(2, dnsEntry.getTags());
				stmt.addBatch();

				if(++count % BATCH_SIZE == 0) {
					stmt.executeBatch();
				}
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			throw new ParentalManagementException("Error occurred while adding dns tags.", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public List<String> getUntaggedDns() throws ParentalManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> dns = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT DISTINCT t.DNS_DOMAIN as DNS FROM PARENTAL_RECORDS p LEFT JOIN PARENTAL_TAG_RECORDS t " +
					"ON p.DNS_DOMAIN == t.DNS_DOMAIN WHERE t.DNS_DOMAIN is NULL";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				dns.add(rs.getString("DNS"));
			}
		} catch (SQLException e) {
			throw new ParentalManagementException("Error occurred while listing switch information for switch ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return dns;
	}

	@Override
	public void addDnsEntries(List<DnsEntry> dnsEntries) throws ParentalManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			String sql = "INSERT INTO PARENTAL_RECORDS(DNS_DOMAIN, DEVICE_MAC, DPID, CREATED_TIME) " +
					"VALUES (?, ?, ?, ?)";
			int count = 0;
			stmt = conn.prepareStatement(sql);

			for (DnsEntry dnsEntry : dnsEntries) {
				stmt.setString(1, dnsEntry.getDnsDomain());
				stmt.setString(2, dnsEntry.getDeviceMac());
				stmt.setString(3, dnsEntry.getDpId());
				stmt.setTimestamp(4, new Timestamp(dnsEntry.getTimestamp()));
				stmt.addBatch();

				if(++count % BATCH_SIZE == 0) {
					stmt.executeBatch();
				}
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			throw new ParentalManagementException("Error occurred while adding dns entries", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public List<DnsEntry> getDnsEntries(String dpId, String deviceMac) throws ParentalManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DnsEntry> dnsEntries = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT DISTINCT p.DNS_DOMAIN as DNS, p.CREATED_TIMESTAMP as DNS_TIME, t.TAGS as TAGS" +
					" FROM PARENTAL_RECORDS p LEFT JOIN PARENTAL_TAG_RECORDS t " +
					"ON p.DNS_DOMAIN == t.DNS_DOMAIN WHERE p.DEVICE_MAC = ?, p.DPID=?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, deviceMac);
			stmt.setString(2, dpId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				DnsEntry dnsEntry = new DnsEntry();
				dnsEntry.setDnsDomain(rs.getString("DNS"));
				dnsEntry.setDpId(dpId);
				dnsEntry.setDeviceMac(deviceMac);
				dnsEntry.setTimestamp(rs.getTimestamp("DNS_TIME").getTime());
				dnsEntry.setTags(rs.getString("TAGS"));
				dnsEntries.add(dnsEntry);
			}
		} catch (SQLException e) {
			throw new ParentalManagementException("Error occurred while listing switch information for switch ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return dnsEntries;
	}

	private Connection getConnection() throws SQLException {
		return ParentalManagementDAOFactory.getConnection();
	}

}
