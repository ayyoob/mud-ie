package com.networkseer.seer.mgt.service;

import com.networkseer.seer.mgt.dto.Device;
import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;

import java.util.List;

public interface SeerMgtService {

	int addDevice(Device device) throws SeerManagementException;

	int addSwitch(Switch aswitch) throws SeerManagementException;

	Switch getSwitch(String dpId) throws SeerManagementException;

	List<Switch> getSwitches(String username) throws SeerManagementException;

	List<Device> getDevices(String dpId) throws SeerManagementException;
}
