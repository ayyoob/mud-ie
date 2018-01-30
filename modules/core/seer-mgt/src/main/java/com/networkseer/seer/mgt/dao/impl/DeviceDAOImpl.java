package com.networkseer.seer.mgt.dao.impl;

import com.networkseer.seer.mgt.dao.DeviceDAO;
import com.networkseer.seer.mgt.dao.SeerManagementDAOFactory;
import com.networkseer.seer.mgt.dto.Device;
import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import com.networkseer.seer.mgt.util.SeerManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceDAOImpl implements DeviceDAO {

	@Override
	public int addDevice(Device device) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int userId = -1;
		try {
			conn = this.getConnection();
			String sql = "INSERT INTO SM_DEVICE(MAC_ADDRESS, DEVICE_NAME, CREATED_TIME, LAST_UPDATED_TIME, " +
					"SWITCH_ID, STATUS, GROUP_ID) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, device.getMac());
			stmt.setString(2, device.getName());
			stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
			stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
			stmt.setInt(5, device.getSwitchId());
			stmt.setString(6, device.getStatus().toString());
			stmt.setInt(7, device.getGroupId());
			stmt.executeUpdate();

			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				userId = rs.getInt(1);
			}
			return userId;
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while adding a device", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public boolean updateDeviceName(String deviceName, int deviceId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE SM_DEVICE SET DEVICE_NAME = ?, LAST_UPDATED_TIME = ?  WHERE ID = ?";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, deviceName);
			stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			stmt.setInt(3, deviceId);
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while updating the device. '" +
					deviceId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public boolean updateStatus(Device.Status status, int deviceId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE SM_DEVICE SET STATUS = ?, LAST_UPDATED_TIME = ?  WHERE ID = ?";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, status.toString());
			stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			stmt.setInt(3, deviceId);
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while updating the device. '" +
					deviceId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public boolean updateGroupId(int groupId, int deviceId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE SM_DEVICE SET SM_GROUP_ID = ?, LAST_UPDATED_TIME = ?  WHERE ID = ?";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setInt(1, groupId);
			stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			stmt.setInt(3, deviceId);
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while updating the device. '" +
					deviceId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, null);
		}
	}


	@Override
	public int getDeviceCount(String dpId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT count(*) as DEVICE_COUNT FROM SM_DEVICE WHERE SWITCH_ID =?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, dpId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("DEVICE_COUNT");
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing switch information", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return 0;
	}

	@Override
	public Device getDevice(int deviceId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Device device = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM SM_DEVICE WHERE ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, deviceId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				device = new Device();
				device.setCreatedTimestamp(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				device.setLastUpdatedTimestamp(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				device.setName(rs.getString("DEVICE_NAME"));
				device.setMac(rs.getString("MAC_ADDRESS"));
				device.setSwitchId(rs.getInt("SWITCH_ID"));
				device.setStatus(Device.Status.valueOf(rs.getString("STATUS")));
				device.setGroupId(rs.getInt("GROUP_ID"));
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing device information for device id:" +
					"'" + deviceId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return device;
	}

	@Override
	public List<Device> getDevices(int switchId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Device> devices = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM SM_DEVICE WHERE SWITCH_ID=?";
			stmt.setInt(1, switchId);
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Device device = new Device();
				device.setCreatedTimestamp(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				device.setLastUpdatedTimestamp(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				device.setName(rs.getString("DEVICE_NAME"));
				device.setMac(rs.getString("MAC_ADDRESS"));
				device.setSwitchId(rs.getInt("SWITCH_ID"));
				device.setStatus(Device.Status.valueOf(rs.getString("STATUS")));
				device.setGroupId(rs.getInt("GROUP_ID"));
				devices.add(device);
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing switch information for switch ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return devices;
	}

	@Override
	public List<Device> getDevices(String dpId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Device> devices = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM SM_DEVICE WHERE SWITCH_ID=(SELECT ID FROM SM_SWITCH WHERE DPID=?)";
			stmt.setString(1, dpId);
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Device device = new Device();
				device.setCreatedTimestamp(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				device.setLastUpdatedTimestamp(rs.getTimestamp("LAST_UPDATED_TIME").getTime());
				device.setName(rs.getString("DEVICE_NAME"));
				device.setMac(rs.getString("MAC_ADDRESS"));
				device.setSwitchId(rs.getInt("SWITCH_ID"));
				device.setStatus(Device.Status.valueOf(rs.getString("STATUS")));
				device.setGroupId(rs.getInt("GROUP_ID"));
				devices.add(device);
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing switch information for switch ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return devices;
	}

	private Connection getConnection() throws SQLException {
		return SeerManagementDAOFactory.getConnection();
	}
}
