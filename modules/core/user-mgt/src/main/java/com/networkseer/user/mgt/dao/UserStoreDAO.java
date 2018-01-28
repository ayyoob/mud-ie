package com.networkseer.user.mgt.dao;

import com.networkseer.user.mgt.dto.Role;
import com.networkseer.user.mgt.dto.User;
import com.networkseer.user.mgt.dto.UserAttribute;
import com.networkseer.user.mgt.exception.UserManagementException;

import java.util.List;

public interface UserStoreDAO {

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

	boolean addAttributes(int userId, List<UserAttribute> attributes) throws UserManagementException;

	boolean updateAttributeValues(int userId, String attributeName, String value) throws UserManagementException;

	List<UserAttribute> getUserAttributes(String userId) throws UserManagementException;


	int addRole(String rolename) throws UserManagementException;

	boolean updateRole(String oldRoleName,String newRoleName) throws UserManagementException;

	List<Role> getAllRoles() throws UserManagementException;

	List<Role> getUserRoles(String username) throws UserManagementException;

	boolean addUserRoles(String username, int roleIds[]) throws UserManagementException;


}
