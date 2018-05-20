package com.mudie.seer.mgt.dao;

import com.mudie.seer.mgt.exception.SeerManagementException;
import com.mudie.seer.mgt.dto.Group;

public interface GroupDAO {

	int addGroup(Group group) throws SeerManagementException;

	boolean updateGroup(Group group) throws SeerManagementException;

	boolean updateGroupName(String name, int groupId) throws SeerManagementException;

	boolean removeGroup(int groupId) throws SeerManagementException;

	Group getGroup(String dpId, String deviceMac) throws SeerManagementException;

}
