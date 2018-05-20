package com.networkseer.seer.mgt.service;

import com.networkseer.seer.mgt.dto.Device;
import com.networkseer.seer.mgt.dto.DeviceRecord;
import com.networkseer.seer.mgt.dto.Group;
import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;

import java.util.List;

public interface SeerMgtService {

	int addDevice(Device device) throws SeerManagementException;

	int addSwitch(Switch aswitch) throws SeerManagementException;

	Switch getSwitch(String dpId) throws SeerManagementException;

	Switch getSwitchFromVxlanId(String vxlanId) throws SeerManagementException;

	List<Switch> getSwitches(String username) throws SeerManagementException;

	List<Switch> getSwitches() throws SeerManagementException;

	List<Device> getDevices(String dpId) throws SeerManagementException;

	Group getGroup(String dpId, String deviceMac) throws SeerManagementException;

	DeviceRecord getDeviceRecord(String vlanId, String deviceMac) throws SeerManagementException;

	List<DeviceRecord> getAllDevices(Device.Status status) throws SeerManagementException;

	List<DeviceRecord> getIoTDeviceRecord() throws SeerManagementException;

	void updateDeviceName(String deviceName, int deviceId) throws SeerManagementException;

	void updateDeviceNameAndStatus(String deviceName,Device.Status status, int deviceId) throws SeerManagementException;

	void updateDevicePropertyAndSwitch(String property,  int switchId, int deviceId) throws SeerManagementException;
}
