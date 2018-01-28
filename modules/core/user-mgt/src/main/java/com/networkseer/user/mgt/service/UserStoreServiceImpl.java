package com.networkseer.user.mgt.service;

import com.networkseer.user.mgt.dao.UserStoreDAO;
import com.networkseer.user.mgt.dao.UserStoreManagementDAOFactory;
import com.networkseer.user.mgt.dao.impl.UserStoreDAOImpl;
import com.networkseer.user.mgt.dto.Role;
import com.networkseer.user.mgt.dto.User;
import com.networkseer.user.mgt.exception.UserManagementException;
import com.networkseer.user.mgt.util.UserManagementDAOUtil;

import java.util.List;

public class UserStoreServiceImpl implements UserStoreService{

	private UserStoreDAO userStoreDAO;

	public UserStoreServiceImpl() {
		userStoreDAO = new UserStoreDAOImpl();
	}

	@Override
	public boolean authenticate(String username, String password) throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.openConnection();
			User user = userStoreDAO.getUser(username);
			if (user == null) {
				return false;
			}
			return UserManagementDAOUtil.validatePassword(password, user.getPassword());
		} catch (Exception e) {
			throw new UserManagementException(e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public int getUserCount() throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.openConnection();
			return userStoreDAO.getUserCount();
		} catch (Exception e) {
			throw new UserManagementException(e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public int addUser(User user) throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.beginTransaction();
			if (user.getPassword() != null || !user.getPassword().isEmpty()) {
				user.setPassword(UserManagementDAOUtil.generateStorngPasswordHash(user.getPassword()));
			}
			int userId = userStoreDAO.addUser(user);
			if (user.getAttributes() != null) {
				userStoreDAO.addAttributes(user.getUserId(), user.getAttributes());
			}
			UserStoreManagementDAOFactory.commitTransaction();
			return userId;
		} catch (Exception e) {
			UserStoreManagementDAOFactory.rollbackTransaction();
			throw new UserManagementException("Error occurred while ...", e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public boolean updateUserPassword(String username, String password) throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.beginTransaction();
			int code = -1;
			userStoreDAO.updateUserPassword(username, password);
			UserStoreManagementDAOFactory.commitTransaction();
			return true;
		} catch (Exception e) {
			UserStoreManagementDAOFactory.rollbackTransaction();
			throw new UserManagementException("Error occurred while ...", e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public User getUser(String username) throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.openConnection();
			User user = userStoreDAO.getUser(username);
			user.setPassword(null);
			return user;
		} catch (Exception e) {
			throw new UserManagementException(e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<User> getAllUsers() throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.openConnection();
			return userStoreDAO.getAllUsers();
		} catch (Exception e) {
			throw new UserManagementException(e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public int addRole(String rolename) throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.beginTransaction();
			int roleId = userStoreDAO.addRole(rolename);
			UserStoreManagementDAOFactory.commitTransaction();
			return roleId;
		} catch (Exception e) {
			UserStoreManagementDAOFactory.rollbackTransaction();
			throw new UserManagementException("Error occurred while ...", e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public boolean updateRole(String oldRoleName, String newRoleName) throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.beginTransaction();
			userStoreDAO.updateRole(oldRoleName, newRoleName);
			UserStoreManagementDAOFactory.commitTransaction();
			return true;
		} catch (Exception e) {
			UserStoreManagementDAOFactory.rollbackTransaction();
			throw new UserManagementException("Error occurred while ...", e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<Role> getAllRoles() throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.openConnection();
			return userStoreDAO.getAllRoles();
		} catch (Exception e) {
			throw new UserManagementException(e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<Role> getUserRoles(String username) throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.openConnection();
			return userStoreDAO.getUserRoles(username);
		} catch (Exception e) {
			throw new UserManagementException(e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public boolean addUserRoles(String username, int roleIds[]) throws UserManagementException {
		try {
			UserStoreManagementDAOFactory.beginTransaction();
			userStoreDAO.addUserRoles(username, roleIds);
			UserStoreManagementDAOFactory.commitTransaction();
			return true;
		} catch (Exception e) {
			UserStoreManagementDAOFactory.rollbackTransaction();
			throw new UserManagementException("Error occurred while ...", e);
		} finally {
			UserStoreManagementDAOFactory.closeConnection();
		}
	}


}
