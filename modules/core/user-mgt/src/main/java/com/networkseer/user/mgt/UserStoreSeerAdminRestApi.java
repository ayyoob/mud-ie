package com.networkseer.user.mgt;

import com.networkseer.common.SeerApiPlugin;
import com.networkseer.user.mgt.dto.User;
import com.networkseer.user.mgt.exception.UserManagementException;
import com.networkseer.user.mgt.internal.UserManagementDataHolder;
import com.networkseer.user.mgt.internal.UserManagmentSeerPluginImpl;
import com.networkseer.user.mgt.service.UserStoreService;
import com.networkseer.user.mgt.dto.UserCount;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/admin/users", description = "admin user mgt services")
public class UserStoreSeerAdminRestApi implements SeerApiPlugin {
	private static final Logger log = LoggerFactory.getLogger(UserManagmentSeerPluginImpl.class);


	@GET
	@Path("/count")
	@ApiOperation(
			value = "user count",
			notes = "Returns user count",
			response = UserCount.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "return user count")})
	public Response countUser() {
		try {
			UserStoreService userStoreService = UserManagementDataHolder.getUserStoreService();
			UserCount userCount = new UserCount();
			userCount.setCount(userStoreService.getUserCount());
			return Response.ok().entity(userCount).build();
		} catch (UserManagementException e) {
			log.error("Failed to process request ", e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}

	}

	@GET
	@ApiOperation(
			value = "user detail",
			notes = "Returns user detail",
			response = List.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "return user details")})
	public Response getUser() {
		try {
			UserStoreService userStoreService = UserManagementDataHolder.getUserStoreService();
			List<User> users = userStoreService.getAllUsers();
			return Response.ok().entity(users).build();
		} catch (UserManagementException e) {
			log.error("Failed to process request.", e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}

	}

	@GET
	@ApiOperation(
			value = "user detail",
			notes = "Returns user detail",
			response = User.class)
	@Path("/{username}")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "return user details")})
	public Response getUser(@PathParam("username") String username) {
		try {
			UserStoreService userStoreService = UserManagementDataHolder.getUserStoreService();
			User user = userStoreService.getUser(username);
			return Response.ok().entity(user).build();
		} catch (UserManagementException e) {
			log.error("Failed to process request.", e);
			return Response.serverError().entity("login service is temporarily unavailable, try again later").build();
		}

	}

}
