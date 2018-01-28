package com.networkseer.user.mgt.service;

import com.networkseer.user.mgt.dto.Role;
import com.networkseer.user.mgt.dto.User;
import com.networkseer.user.mgt.exception.UserManagementException;

import java.util.List;

public interface UserStoreService {

	boolean authenticate(String username, String password) throws UserManagementException;

	int getUserCount() throws UserManagementException;

	/**
	 * This method is used to add a user.
	 *
	 * @param user user object.
	 * @return returns the id of the persisted user record.
	 * @throws UserManagementException
	 */
	int addUser(User user) throws UserManagementException;

	boolean updateUserPassword(String username, String password) throws UserManagementException;

	User getUser(String username) throws UserManagementException;

	List<User> getAllUsers() throws UserManagementException;

	int addRole(String rolename) throws UserManagementException;

	boolean updateRole(String oldRoleName,String newRoleName) throws UserManagementException;

	List<Role> getAllRoles() throws UserManagementException;

	List<Role> getUserRoles(String username) throws UserManagementException;

	boolean addUserRoles(String username, int roleIds[]) throws UserManagementException;


}
