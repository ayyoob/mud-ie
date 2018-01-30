package com.networkseer.user.mgt;

import com.networkseer.common.PriviledgedSeerContext;
import com.networkseer.common.SeerApiPlugin;
import com.networkseer.user.mgt.dto.User;
import com.networkseer.user.mgt.exception.UserManagementException;
import com.networkseer.user.mgt.internal.UserManagementDataHolder;
import com.networkseer.user.mgt.internal.UserManagmentSeerPluginImpl;
import com.networkseer.user.mgt.service.UserStoreService;
import com.networkseer.user.mgt.dto.Login;
import com.networkseer.user.mgt.dto.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/users", description = "user mgt services")
public class UserStoreSeerRestApi implements SeerApiPlugin {
	private static final Logger log = LoggerFactory.getLogger(UserManagmentSeerPluginImpl.class);


	@POST
	@Path("/login")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "authenticated")})
	public Response login(Login login) {
		try {
			UserStoreService userStoreService = UserManagementDataHolder.getUserStoreService();
			boolean status = userStoreService.authenticate(login.getUsername(), login.getPassword());
			return Response.ok().entity(status).build();
		} catch (UserManagementException e) {
			log.error("Failed to authenticate user: " + login.getUsername(), e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}

	}

	@POST
	@ApiResponses(value = { @ApiResponse(code = 200, message = "user added")})
	public Response addUser(User user) {
		try {
			UserStoreService userStoreService = UserManagementDataHolder.getUserStoreService();
			if (user != null && user.getPassword()!= null && !user.getPassword().isEmpty()
					&& user.getUsername()!= null && !user.getUsername().isEmpty()) {
				userStoreService.addUser(user);
				return Response.ok().entity(true).build();
			}
			return Response.noContent().build();

		} catch (UserManagementException e) {
			log.error("Failed to process request.", e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}

	}

	@PUT
	@Path("/update-password")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "user added")})
	public Response updatePassword(Password password) {
		try {
			UserStoreService userStoreService = UserManagementDataHolder.getUserStoreService();
			if (password != null && password.getNewPassword()!= null && !password.getNewPassword().isEmpty()
					&& password.getOldPassword()!= null && !password.getOldPassword().isEmpty()) {
				if (userStoreService.authenticate(PriviledgedSeerContext.getUserName(), password.getOldPassword())) {
					userStoreService.updateUserPassword(PriviledgedSeerContext.getUserName(), password.getNewPassword());
					return Response.ok().entity(true).build();
				}
				return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Wrong Password").build();
			}
			return Response.noContent().build();

		} catch (UserManagementException e) {
			log.error("Failed to process request.", e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}

	}

	@GET
	@ApiResponses(value = { @ApiResponse(code = 200, message = "return user count")})
	public Response getUser() {
		try {
			UserStoreService userStoreService = UserManagementDataHolder.getUserStoreService();
			User user = userStoreService.getUser(PriviledgedSeerContext.getUserName());
			return Response.ok().entity(user).build();
		} catch (UserManagementException e) {
			log.error("Failed to process request.", e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}
	}

}
