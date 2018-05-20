package com.mudie.framework.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mudie.framework.health.monitor.HealthRestApiController;
import com.mudie.common.SeerApiPlugin;
import com.mudie.common.config.SeerConfiguration;
import com.mudie.framework.SeerCore;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

public class SeerApiCore extends Application<SeerConfiguration> {

	private static final Logger log = LoggerFactory.getLogger(SeerApiCore.class);

	@Override
	public void run(SeerConfiguration configuration, Environment environment) throws Exception {
		SeerCore.activatePlugin(configuration);
		log.info("Deploying services...");
		environment.jersey().register(new ApiListingResource());
		environment.jersey().register(new HealthRestApiController());
		environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
		ServiceLoader<SeerApiPlugin> loader = ServiceLoader.load(SeerApiPlugin.class);
		Iterator<SeerApiPlugin> it = loader.iterator();
		while (it.hasNext()) {
			SeerApiPlugin seerApi = it.next();
			environment.jersey().register(seerApi);
		}
		BeanConfig config = new BeanConfig();
		config.setTitle("Seer");
		config.setVersion("1.0.0");
		config.setResourcePackage("com.mudie");
		config.setScan(true);


		environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
			@Override
			public void serverStarted(Server server) {
				for (Connector connector : server.getConnectors()) {
					if (connector instanceof ServerConnector) {
						ServerConnector serverConnector = (ServerConnector) connector;
						log.info(serverConnector.getName() +" services are deployed:"
								+ serverConnector.getProtocols().get(0)
								.replace("/1.1", "").toLowerCase()
								+ "://localhost:" + serverConnector.getLocalPort());
						log.info(serverConnector.getName() +" service definition can be found in:"
								+ serverConnector.getProtocols().get(0)
								.replace("/1.1", "").toLowerCase()
								+ "://localhost:" + serverConnector.getLocalPort() +"/swagger.yaml");
					}
				}
			}
		});

	}
}
