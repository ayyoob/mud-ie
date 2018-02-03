package com.networkseer.seer.mgt.dao;

import com.networkseer.seer.mgt.dto.Group;
import com.networkseer.seer.mgt.exception.SeerManagementException;

public interface GroupDAO {

	int addGroup(Group group) throws SeerManagementException;

	boolean updateGroup(Group group) throws SeerManagementException;

	boolean updateGroupName(String name, int groupId) throws SeerManagementException;

	boolean removeGroup(int groupId) throws SeerManagementException;

	Group getGroup(String dpId, String deviceMac) throws SeerManagementException;

}
