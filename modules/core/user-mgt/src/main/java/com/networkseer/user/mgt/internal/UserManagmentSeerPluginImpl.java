package com.networkseer.user.mgt.internal;

import com.networkseer.common.SeerPlugin;
import com.networkseer.common.config.SeerConfiguration;
import com.networkseer.user.mgt.dao.UserStoreManagementDAOFactory;
import com.networkseer.user.mgt.dto.Role;
import com.networkseer.user.mgt.dto.User;
import com.networkseer.user.mgt.service.UserStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class UserManagmentSeerPluginImpl implements SeerPlugin {
	private static final Logger log = LoggerFactory.getLogger(UserManagmentSeerPluginImpl.class);

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		UserStoreManagementDAOFactory.init();

		//add default user
		//add default role
		//add role mapping to user
		UserStoreService userStoreService = null;
		ServiceLoader<UserStoreService> serviceLoader = ServiceLoader.load(UserStoreService.class);
		for (UserStoreService provider : serviceLoader) {
			userStoreService = provider;
		}
		UserManagementDataHolder.setUserStoreService(userStoreService);

//		Create default user for server runtime;
		try {
			int roleIds[] = new int[seerConfiguration.getUserMgt().getServerRoles().size()];
			int i =0;
			List<Role> roles = userStoreService.getAllRoles();
			if (roles == null || roles.size() == 0) {
				for (String role : seerConfiguration.getUserMgt().getServerRoles()) {
					boolean roleExist = false;
					for (Role existingRole: roles) {
						if (existingRole.equals(role)) {
							roleExist = true;
							roleIds[i] = existingRole.getRoleId();
							break;
						}
					}
					if (!roleExist) {
						roleIds[i] = userStoreService.addRole(role);
					}
					i++;
				}
			}
			if (userStoreService.getUserCount()==0) {
				User user = new User();
				String username = seerConfiguration.getUserMgt().getServerAdminUserName();
				user.setUsername(username);
				user.setPassword(seerConfiguration.getUserMgt().getServerAdminPassword());
				userStoreService.addUser(user);
				userStoreService.addUserRoles(username, roleIds);
			}
		} catch (Exception e) {
			log.error("Failed to create a default user and roles", e);
		}
	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add("com.networkseer.datasource.internal.DatasourceSeerPluginImpl");
		return dependencies;
	}
}
