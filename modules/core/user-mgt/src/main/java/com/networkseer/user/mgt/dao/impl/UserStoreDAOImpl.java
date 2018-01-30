package com.networkseer.user.mgt.dao.impl;

import com.networkseer.user.mgt.dao.UserStoreDAO;
import com.networkseer.user.mgt.dao.UserStoreManagementDAOFactory;
import com.networkseer.user.mgt.dto.Role;
import com.networkseer.user.mgt.dto.User;
import com.networkseer.user.mgt.dto.UserAttribute;
import com.networkseer.user.mgt.exception.UserManagementException;
import com.networkseer.user.mgt.util.UserManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class UserStoreDAOImpl implements UserStoreDAO {

	@Override
	public int getUserCount() throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		User user = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT count(*) as USER_COUNT FROM UM_USER";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("USER_COUNT");
			}
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while listing user information", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return 0;
	}

	@Override
	public int addUser(User user) throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int userId = -1;
		try {
			conn = this.getConnection();
			String sql = "INSERT INTO UM_USER(UM_USER_NAME, UM_USER_PASSWORD, UM_CHANGED_TIME) " +
					"VALUES (?, ?, ?)";
			stmt = conn.prepareStatement(sql, new String[] {"um_id"});
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPassword());
			stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
			stmt.executeUpdate();

			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				userId = rs.getInt(1);
			}
			return userId;
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while adding user", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public boolean updateUserPassword(String username, String password) throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE UM_USER SET UM_USER_PASSWORD = ?, UM_CHANGED_TIME = ? WHERE UM_USER_NAME = ?";
			stmt = conn.prepareStatement(sql, new String[] {"um_id"});
			stmt.setString(1, password);
			stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			stmt.setString(3, username);
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while updating a user. '" +
					username + "'", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public User getUser(String username) throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		User user = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT UM_ID, UM_CHANGED_TIME,UM_USER_PASSWORD FROM UM_USER WHERE UM_USER_NAME = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, username);
			rs = stmt.executeQuery();
			if (rs.next()) {
				user = new User();
				user.setUserId(rs.getInt("UM_ID"));
				user.setTimestamp(rs.getTimestamp("UM_CHANGED_TIME"));
				user.setPassword(rs.getString("UM_USER_PASSWORD"));
			}
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while listing user information for user " +
					"'" + username + "'", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return user;
	}

	@Override
	public List<User> getAllUsers() throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<User> users = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM UM_USER";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				User user = new User();
				user.setUserId(rs.getInt("UM_ID"));
				user.setTimestamp(rs.getTimestamp("UM_CHANGED_TIME"));
				users.add(user);
			}
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while listing user information", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return users;
	}

	@Override
	public boolean addAttributes(int userId, List<UserAttribute> attributes) throws UserManagementException {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean status = false;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"INSERT INTO UM_USER_ATTRIBUTE(UM_ATTR_NAME, UM_ATTR_VALUE, UM_USER_ID) VALUES (?, ?, ?)");
			for (UserAttribute attribute : attributes) {
				stmt.setString(1, attribute.getName());
				stmt.setString(2, attribute.getValue());
				stmt.setInt(3, userId);
				stmt.addBatch();
			}
			stmt.executeBatch();
			status = true;
		} catch (SQLException e) {
			String msg = "Error occurred while adding the user attribute '" + userId ;
			throw new UserManagementException(msg, e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, null);
		}
		return status;
	}

	@Override
	public boolean updateAttributeValues(int userId, String attributeName, String value) throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"UPDATE UM_USER_ATTRIBUTE SET  UM_ATTR_VALUE = ? WHERE  UM_USER_ID = ? and " + attributeName + " = ?");

			stmt.setString(1, value);
			stmt.setInt(2, userId);

			stmt.setString(3, attributeName);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			String msg = "Error occurred while modifying the user attributes for '" + userId;
			throw new UserManagementException(msg, e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public List<UserAttribute> getUserAttributes(String userId) throws UserManagementException {
		Connection conn = null;
		PreparedStatement stmt = null;
		List<UserAttribute> attributes = new ArrayList<>();
		ResultSet resultSet = null;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"SELECT * FROM UM_USER_ATTRIBUTE WHERE UM_USER_ID = ?");
			stmt.setString(1, userId);

			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				UserAttribute property = new UserAttribute();
				property.setName(resultSet.getString("UM_ATTR_NAME"));
				property.setValue(resultSet.getString("UM_ATTR_VALUE"));
				attributes.add(property);
			}
			return attributes;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching user attributes : '" + userId;
			throw new UserManagementException(msg, e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, resultSet);
		}
	}

	@Override
	public int addRole(String rolename) throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int userId = -1;
		try {
			conn = this.getConnection();
			String sql = "INSERT INTO UM_SYSTEM_ROLE(UM_ROLE_NAME) " +
					"VALUES (?)";
			stmt = conn.prepareStatement(sql, new String[] {"um_id"});
			stmt.setString(1, rolename);
			stmt.executeUpdate();

			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				userId = rs.getInt(1);
			}
			return userId;
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while adding role", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public boolean updateRole(String oldRoleName, String newRoleName) throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = this.getConnection();
			String sql = "UPDATE UM_SYSTEM_ROLE SET UM_ROLE_NAME = ? WHERE UM_ROLE_NAME = ?";
			stmt = conn.prepareStatement(sql, new String[] {"um_id"});
			stmt.setString(1, newRoleName);
			stmt.setString(2, oldRoleName);
			rows = stmt.executeUpdate();
			return (rows > 0);
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while updating a role. '" +
					oldRoleName + "'", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public List<Role> getAllRoles() throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Role> roles = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT * FROM UM_SYSTEM_ROLE";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Role role = new Role();
				role.setRoleId(rs.getInt("UM_ID"));
				role.setRoleName(rs.getString("UM_ROLE_NAME"));
				roles.add(role);
			}
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while listing user information", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return roles;
	}

	@Override
	public List<Role> getUserRoles(String username) throws UserManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Role> roles = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql = "SELECT b.UM_ROLE_NAME, b.UM_ID as UM_ROLE_NAME FROM UM_SYSTEM_USER_ROLE a " +
					"LEFT JOIN UM_SYSTEM_ROLE b ON a.UM_ROLE_ID = b.UM_ID" +
					" WHERE UM_USER_NAME = ?";
			stmt.setString(1, username);
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Role role = new Role();
				role.setRoleId(rs.getInt("UM_ID"));
				role.setRoleName(rs.getString("UM_ROLE_NAME"));
				roles.add(role);
			}
		} catch (SQLException e) {
			throw new UserManagementException("Error occurred while listing role information", e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, rs);
		}
		return roles;
	}

	@Override
	public boolean addUserRoles(String username, int roleIds[]) throws UserManagementException {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean status = false;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"INSERT INTO UM_SYSTEM_USER_ROLE(UM_USER_NAME, UM_ROLE_ID) VALUES (?, ?)");
			for (int roleId : roleIds) {
				stmt.setString(1, username);
				stmt.setInt(2, roleId);
				stmt.addBatch();
			}
			stmt.executeBatch();
			status = true;
		} catch (SQLException e) {
			String msg = "Error occurred while adding the user roles for '" + username ;
			throw new UserManagementException(msg, e);
		} finally {
			UserManagementDAOUtil.cleanupResources(stmt, null);
		}
		return status;
	}

	private Connection getConnection() throws SQLException {
		return UserStoreManagementDAOFactory.getConnection();
	}

}
