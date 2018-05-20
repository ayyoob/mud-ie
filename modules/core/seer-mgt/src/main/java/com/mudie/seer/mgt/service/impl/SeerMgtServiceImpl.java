package com.mudie.seer.mgt.service.impl;

import com.mudie.seer.mgt.dao.DeviceDAO;
import com.mudie.seer.mgt.dto.DeviceRecord;
import com.mudie.seer.mgt.dao.GroupDAO;
import com.mudie.seer.mgt.dao.SeerManagementDAOFactory;
import com.mudie.seer.mgt.dao.SwitchDAO;
import com.mudie.seer.mgt.dao.impl.DeviceDAOImpl;
import com.mudie.seer.mgt.dao.impl.GroupDAOImpl;
import com.mudie.seer.mgt.dao.impl.SwitchDAOImpl;
import com.mudie.seer.mgt.dto.Device;
import com.mudie.seer.mgt.dto.Group;
import com.mudie.seer.mgt.dto.Switch;
import com.mudie.seer.mgt.exception.SeerManagementException;
import com.mudie.seer.mgt.service.SeerMgtService;

import java.util.List;

public class SeerMgtServiceImpl implements SeerMgtService {

	private DeviceDAO deviceDAO;
	private SwitchDAO switchDAO;
	private GroupDAO groupDAO;

	public SeerMgtServiceImpl() {
		deviceDAO = new DeviceDAOImpl();
		switchDAO = new SwitchDAOImpl();
		groupDAO = new GroupDAOImpl();
	}

	@Override
	public int addDevice(Device device) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.beginTransaction();
			int deviceId = deviceDAO.addDevice(device);
			SeerManagementDAOFactory.commitTransaction();
			return deviceId;
		} catch (Exception e) {
			SeerManagementDAOFactory.rollbackTransaction();
			throw new SeerManagementException("Error occurred while adding device", e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public int addSwitch(Switch aswitch) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.beginTransaction();
			int deviceId = switchDAO.addSwitch(aswitch);
			SeerManagementDAOFactory.commitTransaction();
			return deviceId;
		} catch (Exception e) {
			SeerManagementDAOFactory.rollbackTransaction();
			throw new SeerManagementException("Error occurred while adding switch", e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public Switch getSwitch(String dpId) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return switchDAO.getSwitch(dpId);
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public Switch getSwitchFromVxlanId(String vxlanId) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return switchDAO.getSwitchFromVxlanId(vxlanId);
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<Switch> getSwitches(String username) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return switchDAO.getSwitches(username);
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<Switch> getSwitches() throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return switchDAO.getSwitches();
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<Device> getDevices(String dpId) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return deviceDAO.getDevices(dpId);
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public Group getGroup(String dpId, String deviceMac) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return groupDAO.getGroup(dpId, deviceMac);
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public DeviceRecord getDeviceRecord(String vlanId, String deviceMac) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return deviceDAO.getDeviceRecord(vlanId,deviceMac);
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<DeviceRecord> getAllDevices(Device.Status status) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return deviceDAO.getAllDevices(status);
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<DeviceRecord> getIoTDeviceRecord() throws SeerManagementException {
		try {
			SeerManagementDAOFactory.openConnection();
			return deviceDAO.getIoTDeviceRecord();
		} catch (Exception e) {
			throw new SeerManagementException(e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public void updateDeviceName(String deviceName, int deviceId) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.beginTransaction();
			deviceDAO.updateDeviceName(deviceName, deviceId);
			SeerManagementDAOFactory.commitTransaction();
		} catch (Exception e) {
			SeerManagementDAOFactory.rollbackTransaction();
			throw new SeerManagementException("Error occurred while updating device name", e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public void updateDeviceNameAndStatus(String deviceName, Device.Status status, int deviceId)
			throws SeerManagementException {
		try {
			SeerManagementDAOFactory.beginTransaction();
			deviceDAO.updateDeviceNameAndStatus(deviceName,status, deviceId);
			SeerManagementDAOFactory.commitTransaction();
		} catch (Exception e) {
			SeerManagementDAOFactory.rollbackTransaction();
			throw new SeerManagementException("Error occurred while updating device name", e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public void updateDevicePropertyAndSwitch(String property, int switchId,  int deviceId) throws SeerManagementException {
		try {
			SeerManagementDAOFactory.beginTransaction();
			deviceDAO.updateSwitchAndProperty(property,switchId, deviceId);
			SeerManagementDAOFactory.commitTransaction();
		} catch (Exception e) {
			SeerManagementDAOFactory.rollbackTransaction();
			throw new SeerManagementException("Error occurred while updating device name", e);
		} finally {
			SeerManagementDAOFactory.closeConnection();
		}
	}
}
