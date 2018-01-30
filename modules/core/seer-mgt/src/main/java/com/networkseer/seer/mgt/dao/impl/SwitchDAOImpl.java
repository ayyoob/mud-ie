package com.networkseer.seer.mgt.dao.impl;

import com.networkseer.seer.mgt.dao.SeerManagementDAOFactory;
import com.networkseer.seer.mgt.dao.SwitchDAO;
import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import com.networkseer.seer.mgt.util.SeerManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SwitchDAOImpl implements SwitchDAO {

	@Override
	public Switch getSwitch(String dpId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Switch aSwitch = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM SM_SWITCH WHERE DPID = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, dpId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				aSwitch = new Switch();
				aSwitch.setOwner(rs.getString("OWNER"));
				aSwitch.setDpId(dpId);
				aSwitch.setDateOfCreation(rs.getTimestamp("CREATED_TIME").getTime());
				aSwitch.setDateOfLastUpdate(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				aSwitch.setQuota(rs.getLong("QUOTA"));
				aSwitch.setBillingDay(rs.getInt("BILLING_DAY"));
				aSwitch.setStatus(Switch.Status.valueOf(rs.getString(rs.getString("STATUS"))));
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing switch information for switch " +
					"'" + dpId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return aSwitch;
	}

	@Override
	public List<Switch> getAllSwitches() throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Switch> aSwitchs = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM SM_SWITCH";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Switch aSwitch = new Switch();
				aSwitch.setOwner(rs.getString("OWNER"));
				aSwitch.setDpId(rs.getString("DPID"));
				aSwitch.setDateOfCreation(rs.getTimestamp("CREATED_TIME").getTime());
				aSwitch.setDateOfLastUpdate(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				aSwitch.setQuota(rs.getLong("QUOTA"));
				aSwitch.setBillingDay(rs.getInt("BILLING_DAY"));
				aSwitch.setStatus(Switch.Status.valueOf(rs.getString(rs.getString("STATUS"))));
				aSwitchs.add(aSwitch);
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing switch information for switch ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return aSwitchs;
	}

	@Override
	public int getSwitchesCount() throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT count(*) as SWITCH_COUNT FROM SM_SWITCH";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("SWITCH_COUNT");
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing switch information", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return 0;
	}

	@Override
	public List<Switch> getSwitches(String owner) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Switch> aSwitchs = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM SM_SWITCH WHERE OWNER=?";
			stmt.setString(1, owner);
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Switch aSwitch = new Switch();
				aSwitch.setOwner(rs.getString("OWNER"));
				aSwitch.setDpId(rs.getString("DPID"));
				aSwitch.setDateOfCreation(rs.getTimestamp("CREATED_TIME").getTime());
				aSwitch.setDateOfLastUpdate(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				aSwitch.setQuota(rs.getLong("QUOTA"));
				aSwitch.setBillingDay(rs.getInt("BILLING_DAY"));
				aSwitch.setStatus(Switch.Status.valueOf(rs.getString(rs.getString("STATUS"))));
				aSwitchs.add(aSwitch);
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing switch information for switch ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return aSwitchs;
	}

	@Override
	public int addSwitch(Switch smSwitch) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int userId = -1;
		try {
			conn = this.getConnection();
			String sql = "INSERT INTO SM_SWITCH(OWNER, DPID, CREATED_TIME, LAST_UPDATED_TIME, " +
					"QUOTA, BILLING_DAY, STATUS) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, smSwitch.getOwner());
			stmt.setString(2, smSwitch.getDpId());
			stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
			stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
			stmt.setLong(5, smSwitch.getQuota());
			stmt.setInt(6, smSwitch.getBillingDay());
			stmt.setString(7, smSwitch.getStatus().toString());
			stmt.executeUpdate();

			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				userId = rs.getInt(1);
			}
			return userId;
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while adding switch", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public boolean updateStatus(String dpId, Switch.Status status) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE SM_SWITCH SET STATUS = ?, LAST_UPDATED_TIME = ? WHERE DPID = ?";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, status.toString());
			stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			stmt.setString(3, dpId);
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while updating the switch. '" +
					dpId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	private Connection getConnection() throws SQLException {
		return SeerManagementDAOFactory.getConnection();
	}
}
