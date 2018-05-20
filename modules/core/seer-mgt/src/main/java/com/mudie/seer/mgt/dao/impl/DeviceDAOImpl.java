package com.mudie.seer.mgt.dao.impl;

import com.mudie.seer.mgt.dao.DeviceDAO;
import com.mudie.seer.mgt.dao.SeerManagementDAOFactory;
import com.mudie.seer.mgt.dto.DeviceRecord;
import com.mudie.seer.mgt.exception.SeerManagementException;
import com.mudie.seer.mgt.dto.Device;
import com.mudie.seer.mgt.dto.Group;
import com.mudie.seer.mgt.dto.Switch;
import com.mudie.seer.mgt.util.SeerManagementDAOUtil;

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
					"SWITCH_ID, STATUS, GROUP_ID, PROPERTY) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, device.getMac());
			stmt.setString(2, device.getName());
			stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
			stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
			stmt.setInt(5, device.getSwitchId());
			stmt.setString(6, device.getStatus().toString());
			stmt.setInt(7, device.getGroupId());
			stmt.setString(8, device.getProperty());
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
	public boolean updateDeviceNameAndStatus(String deviceName,Device.Status status, int deviceId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE SM_DEVICE SET DEVICE_NAME = ?,STATUS = ?, LAST_UPDATED_TIME = ?  WHERE ID = ?";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, deviceName);
			stmt.setString(2, status.toString());
			stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
			stmt.setInt(4, deviceId);
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
				device.setProperty(rs.getString("PROPERTY"));
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
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, switchId);

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
				device.setProperty(rs.getString("PROPERTY"));
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
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, dpId);

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
				device.setProperty(rs.getString("PROPERTY"));
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
	public List<DeviceRecord> getAllDevices(Device.Status status) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceRecord> iotDevices = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT d.DEVICE_NAME as DEVICE_NAME, d.MAC_ADDRESS as MAC_ADDRESS, d.SWITCH_ID as SWITCH_ID," +
					" d.STATUS as STATUS, d.GROUP_ID as GROUP_ID, d.PROPERTY as PROPERTY," +
					" s.ID as SID, s.OWNER as OWNER, s.DPID as DPID, s.QUOTA as QUOTA, s.BILLING_DAY as BILLING_DAY, s.STATUS as SWITCH_STATUS, " +
					"g.ID as GID, g.GROUP_NAME as GROUP_NAME, g.SWITCH_ID as GSID, g.QUOTA as GQUOTA, g.QUOTA_APP as QUOTA_APP," +
					"g.PARENTAL_APP as PARENTAL_APP, g.IOT_SECURITY_APP as IOT_SECURITY_APP" +
					" FROM (SM_DEVICE d LEFT JOIN SM_SWITCH s ON d.SWITCH_ID = s.ID) LEFT JOIN SM_GROUP g ON d.GROUP_ID = g.id  " +
					"WHERE d.STATUS=?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, status.toString());

			rs = stmt.executeQuery();
			while (rs.next()) {
				DeviceRecord deviceRecord = new DeviceRecord();
				Device device = new Device();
				device.setName(rs.getString("DEVICE_NAME"));
				device.setSwitchId(rs.getInt("SWITCH_ID"));
				device.setMac(rs.getString("MAC_ADDRESS"));
				device.setStatus(status);
				device.setGroupId(rs.getInt("GROUP_ID"));
				device.setProperty(rs.getString("PROPERTY"));
				deviceRecord.setDevice(device);

				Switch aSwitch = new Switch();
				aSwitch.setId(rs.getInt("SID"));
				aSwitch.setOwner(rs.getString("OWNER"));
				aSwitch.setDpId(rs.getString("DPID"));
				aSwitch.setQuota(rs.getLong("QUOTA"));
				aSwitch.setBillingDay(rs.getInt("BILLING_DAY"));
				aSwitch.setStatus(Switch.Status.valueOf(rs.getString("SWITCH_STATUS")));
				deviceRecord.setaSwitch(aSwitch);

				Group group = new Group();
				group.setId(rs.getInt("GID"));
				group.setGroupName(rs.getString("GROUP_NAME"));
				group.setSwitchId(rs.getInt("GSID"));
				group.setQuota(rs.getLong("GQUOTA"));
				group.setQuotoAppEnabled(rs.getBoolean("QUOTA_APP"));
				group.setParentalAppEnabled(rs.getBoolean("PARENTAL_APP"));
				group.setSecurityAppEnabled(rs.getBoolean("IOT_SECURITY_APP"));
				deviceRecord.setGroup(group);
				iotDevices.add(deviceRecord);
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing device information ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return iotDevices;
	}

	@Override
	public DeviceRecord getDeviceRecord(String vlanId, String deviceMac) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DeviceRecord deviceRecord = new DeviceRecord();
		try {
			conn = this.getConnection();
			String sql = "SELECT d.DEVICE_NAME as DEVICE_NAME, d.SWITCH_ID as SWITCH_ID, d.STATUS as STATUS, d.GROUP_ID as GROUP_ID, d.PROPERTY as PROPERTY," +
					" s.ID as SID, s.OWNER as OWNER, s.DPID as DPID, s.QUOTA as QUOTA, s.BILLING_DAY as BILLING_DAY, s.STATUS as SWITCH_STATUS, " +
					"g.ID as GID, g.GROUP_NAME as GROUP_NAME, g.SWITCH_ID as GSID, g.QUOTA as GQUOTA, g.QUOTA_APP as QUOTA_APP," +
					"g.PARENTAL_APP as PARENTAL_APP, g.IOT_SECURITY_APP as IOT_SECURITY_APP" +
					" FROM (SM_DEVICE d LEFT JOIN SM_SWITCH s ON d.SWITCH_ID = s.ID) LEFT JOIN SM_GROUP g ON d.GROUP_ID = g.id  " +
					"WHERE d.MAC_ADDRESS=? AND s.DPID LIKE ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, deviceMac);
			stmt.setString(2, "%" + vlanId);

			rs = stmt.executeQuery();
			if (rs.next()) {
				Device device = new Device();
				device.setName(rs.getString("DEVICE_NAME"));
				device.setSwitchId(rs.getInt("SWITCH_ID"));
				device.setMac(deviceMac);
				device.setStatus(Device.Status.valueOf(rs.getString("STATUS")));
				device.setGroupId(rs.getInt("GROUP_ID"));
				device.setProperty(rs.getString("PROPERTY"));
				deviceRecord.setDevice(device);

				Switch aSwitch = new Switch();
				aSwitch.setId(rs.getInt("SID"));
				aSwitch.setOwner(rs.getString("OWNER"));
				aSwitch.setDpId(rs.getString("DPID"));
				aSwitch.setQuota(rs.getLong("QUOTA"));
				aSwitch.setBillingDay(rs.getInt("BILLING_DAY"));
				aSwitch.setStatus(Switch.Status.valueOf(rs.getString("SWITCH_STATUS")));
				deviceRecord.setaSwitch(aSwitch);

				Group group = new Group();
				group.setId(rs.getInt("GID"));
				group.setGroupName(rs.getString("GROUP_NAME"));
				group.setSwitchId(rs.getInt("GSID"));
				group.setQuota(rs.getLong("GQUOTA"));
				group.setQuotoAppEnabled(rs.getBoolean("QUOTA_APP"));
				group.setParentalAppEnabled(rs.getBoolean("PARENTAL_APP"));
				group.setSecurityAppEnabled(rs.getBoolean("IOT_SECURITY_APP"));
				deviceRecord.setGroup(group);
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing device information ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return deviceRecord;
	}

	@Override
	public List<DeviceRecord> getIoTDeviceRecord() throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceRecord> iotDevices = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT d.DEVICE_NAME as DEVICE_NAME, d.MAC_ADDRESS as MAC_ADDRESS, d.SWITCH_ID as SWITCH_ID," +
					" d.STATUS as STATUS, d.GROUP_ID as GROUP_ID, d.PROPERTY as PROPERTY," +
					" s.ID as SID, s.OWNER as OWNER, s.DPID as DPID, s.QUOTA as QUOTA, s.BILLING_DAY as BILLING_DAY, s.STATUS as SWITCH_STATUS, " +
					"g.ID as GID, g.GROUP_NAME as GROUP_NAME, g.SWITCH_ID as GSID, g.QUOTA as GQUOTA, g.QUOTA_APP as QUOTA_APP," +
					"g.PARENTAL_APP as PARENTAL_APP, g.IOT_SECURITY_APP as IOT_SECURITY_APP" +
					" FROM (SM_DEVICE d LEFT JOIN SM_SWITCH s ON d.SWITCH_ID = s.ID) LEFT JOIN SM_GROUP g ON d.GROUP_ID = g.id  " +
					"WHERE g.IOT_SECURITY_APP=?";
			stmt = conn.prepareStatement(sql);
			stmt.setBoolean(1, true);

			rs = stmt.executeQuery();
			while (rs.next()) {
				DeviceRecord deviceRecord = new DeviceRecord();
				Device device = new Device();
				device.setName(rs.getString("DEVICE_NAME"));
				device.setSwitchId(rs.getInt("SWITCH_ID"));
				device.setMac(rs.getString("MAC_ADDRESS"));
				device.setStatus(Device.Status.valueOf(rs.getString("STATUS")));
				device.setGroupId(rs.getInt("GROUP_ID"));
				device.setProperty(rs.getString("PROPERTY"));
				deviceRecord.setDevice(device);

				Switch aSwitch = new Switch();
				aSwitch.setId(rs.getInt("SID"));
				aSwitch.setOwner(rs.getString("OWNER"));
				aSwitch.setDpId(rs.getString("DPID"));
				aSwitch.setQuota(rs.getLong("QUOTA"));
				aSwitch.setBillingDay(rs.getInt("BILLING_DAY"));
				aSwitch.setStatus(Switch.Status.valueOf(rs.getString("SWITCH_STATUS")));
				deviceRecord.setaSwitch(aSwitch);

				Group group = new Group();
				group.setId(rs.getInt("GID"));
				group.setGroupName(rs.getString("GROUP_NAME"));
				group.setSwitchId(rs.getInt("GSID"));
				group.setQuota(rs.getLong("GQUOTA"));
				group.setQuotoAppEnabled(rs.getBoolean("QUOTA_APP"));
				group.setParentalAppEnabled(rs.getBoolean("PARENTAL_APP"));
				group.setSecurityAppEnabled(rs.getBoolean("IOT_SECURITY_APP"));
				deviceRecord.setGroup(group);
				iotDevices.add(deviceRecord);
			}
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while listing device information ", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return iotDevices;
	}

	@Override
	public boolean updateSwitchAndProperty(String property, int switchId, int deviceId) throws SeerManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE SM_DEVICE SET PROPERTY = ?, SWITCH_ID = ? , LAST_UPDATED_TIME = ?  WHERE ID = ?";
			stmt = conn.prepareStatement(sql, new String[] {"id"});
			stmt.setString(1, property);
			stmt.setInt(2, switchId);
			stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
			stmt.setInt(4, deviceId);
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new SeerManagementException("Error occurred while updating the device. '" +
					deviceId + "'", e);
		} finally {
			SeerManagementDAOUtil.cleanupResources(stmt, null);
		}
	}



	private Connection getConnection() throws SQLException {
		return SeerManagementDAOFactory.getConnection();
	}
}
