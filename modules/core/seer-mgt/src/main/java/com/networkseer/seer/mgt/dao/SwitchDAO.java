package com.networkseer.seer.mgt.dao;

import com.networkseer.seer.mgt.dto.Switch;
import com.networkseer.seer.mgt.exception.SeerManagementException;

import java.util.List;

public interface SwitchDAO {

	Switch getSwitch(String dpId) throws SeerManagementException;

	List<Switch> getAllSwitches() throws SeerManagementException;

	int getSwitchesCount() throws SeerManagementException;

	List<Switch> getSwitches(String username) throws SeerManagementException;

	int addSwitch(Switch smSwitch) throws SeerManagementException;

	boolean updateStatus(String dpId, Switch.Status status) throws SeerManagementException;


}
