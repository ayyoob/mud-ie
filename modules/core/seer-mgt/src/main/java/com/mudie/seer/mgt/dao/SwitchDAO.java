package com.mudie.seer.mgt.dao;

import com.mudie.seer.mgt.exception.SeerManagementException;
import com.mudie.seer.mgt.dto.Switch;

import java.util.List;

public interface SwitchDAO {

	Switch getSwitch(String dpId) throws SeerManagementException;

	Switch getSwitchFromVxlanId(String vlanId) throws SeerManagementException;

	List<Switch> getAllSwitches() throws SeerManagementException;

	int getSwitchesCount() throws SeerManagementException;

	List<Switch> getSwitches(String username) throws SeerManagementException;

	List<Switch> getSwitches() throws SeerManagementException;

	int addSwitch(Switch smSwitch) throws SeerManagementException;

	boolean updateStatus(String dpId, Switch.Status status) throws SeerManagementException;


}
