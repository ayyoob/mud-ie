package com.mudie.seer.mgt.dao.impl;

import com.mudie.seer.mgt.dao.GroupDAO;
import com.mudie.seer.mgt.dao.SeerManagementDAOFactory;
import com.mudie.seer.mgt.exception.SeerManagementException;
import com.mudie.seer.mgt.util.SeerManagementDAOUtil;
import com.mudie.seer.mgt.dto.Group;

import java.sql.*;
import java.util.Date;

public class GroupDAOImpl implements GroupDAO {

	@Override
	public int addGroup(Group group) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int userId = -1;
		try {
			conn = this.getConnection();
			String sql = "INSERT INTO SM_GROUP(GROUP_NAME, SWITCH_ID, CREATED_TIME, LAST_UPDATED_TIME, " +
					"QUOTA_APP, PARENTAL_APP, IOT_SECURITY_APP, QUOTA) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, group.getGroupName());
			stmt.setInt(2, group.getId());
			stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
			stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
			stmt.setBoolean(5, group.isQuotoAppEnabled());
			stmt.setBoolean(6, group.isParentalAppEnabled());
			stmt.setBoolean(7, group.isSecurityAppEnabled());
			stmt.setLong(8, group.getQuota());
			stmt.executeUpdate();

			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				userId = rs.getInt(1);
			}
			return userId;
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while creating a group", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public boolean updateGroup(Group group) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE SM_GROUP SET LAST_UPDATED_TIME = ?, QUOTA_APP=?," +
					"PARENTAL_APP=?, IOT_SECURITY_APP =?, QUOTA=?  WHERE SWITCH_ID = ? AND GROUP_NAME= ?";
			stmt = conn.prepareStatement(sql, new String[] {"id"});

			stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
			stmt.setBoolean(2, group.isQuotoAppEnabled());
			stmt.setBoolean(3, group.isParentalAppEnabled());
			stmt.setBoolean(4, group.isSecurityAppEnabled());
			stmt.setLong(5, group.getQuota());
			stmt.setInt(6, group.getSwitchId());
			stmt.setString(7, group.getGroupName());
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while updating the group. '" +
					group.getGroupName() + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public boolean updateGroupName(String name, int groupId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE SM_GROUP SET GROUP_NAME = ?, LAST_UPDATED_TIME = ?  WHERE ID = ?";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, name);
			stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			stmt.setInt(3, groupId);
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while updating the group. '" +
					groupId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public boolean removeGroup(int groupId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String sql = "DELETE * FROM SM_GROUP WHERE ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, groupId);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while deleting the group. '" +
					groupId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public Group getGroup(String dpId, String deviceMac) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Group group = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM SM_GROUP WHERE SWITCH_ID = (SELECT SWITCH_ID FROM SM_DEVICE" +
					" WHERE MAC_ADDRESS=? AND SWITCH_ID==(SELECT ID FROM SM_SWITCH WHERE DPID= ?)) ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, deviceMac);
			stmt.setString(2, dpId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				group = new Group();
				group.setId(rs.getInt("ID"));
				group.setGroupName(rs.getString("GROUP_NAME"));
				group.setSwitchId(rs.getInt("SWITCH_ID"));
				group.setDateOfCreation(rs.getTimestamp("CREATED_TIME").getTime());
				group.setDateOfLastUpdate(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				group.setQuota(rs.getLong("QUOTA"));
				group.setQuotoAppEnabled(rs.getBoolean("QUOTA_APP"));
				group.setParentalAppEnabled(rs.getBoolean("PARENTAL_APP"));
				group.setSecurityAppEnabled(rs.getBoolean("IOT_SECURITY_APP"));
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing group information for switch " +
					"'" + dpId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return group;
	}

	private Connection getConnection() throws SQLException {
		return SeerManagementDAOFactory.getConnection();
	}
}
