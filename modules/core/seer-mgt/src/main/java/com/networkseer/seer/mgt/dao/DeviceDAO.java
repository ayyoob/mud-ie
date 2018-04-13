package com.networkseer.seer.mgt.dao;

import com.networkseer.seer.mgt.dto.Device;
import com.networkseer.seer.mgt.dto.DeviceRecord;
import com.networkseer.seer.mgt.exception.SeerManagementException;

import java.util.List;

public interface DeviceDAO {

	int addDevice(Device device) throws SeerManagementException;

	boolean updateDeviceName(String deviceName, int deviceId) throws SeerManagementException;

	boolean updateStatus(Device.Status status, int deviceId) throws SeerManagementException;

	boolean updateGroupId(int groupId, int deviceId) throws SeerManagementException;

	int getDeviceCount(String dpId) throws SeerManagementException;

	Device getDevice(int id) throws SeerManagementException;

	List<Device> getDevices(int switchId) throws SeerManagementException;

	List<Device> getDevices(String dpId) throws SeerManagementException;

	DeviceRecord getDeviceRecord(String vlanId, String deviceMac) throws SeerManagementException;

	List<DeviceRecord> getIoTDeviceRecord() throws SeerManagementException;

	boolean updateDeviceNameAndStatus(String deviceName,Device.Status status, int deviceId) throws SeerManagementException;

	boolean updateSwitchAndProperty(String property, int switchId, int deviceId) throws SeerManagementException;

}
