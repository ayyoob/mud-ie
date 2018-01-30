package com.networkseer.seer.mgt.service.impl;

import com.networkseer.seer.mgt.dao.DeviceDAO;
import com.networkseer.seer.mgt.dao.GroupDAO;
import com.networkseer.seer.mgt.dao.SeerManagementDAOFactory;
import com.networkseer.seer.mgt.dao.SwitchDAO;
import com.networkseer.seer.mgt.dao.impl.DeviceDAOImpl;
import com.networkseer.seer.mgt.dao.impl.GroupDAOImpl;
import com.networkseer.seer.mgt.dao.impl.SwitchDAOImpl;
import com.networkseer.seer.mgt.dto.Device;
import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import com.networkseer.seer.mgt.service.SeerMgtService;

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

}
