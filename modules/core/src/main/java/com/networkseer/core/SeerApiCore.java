package com.networkseer.core;

import com.networkseer.common.SeerApiPlugin;
import com.networkseer.core.health.monitor.HealthRestApiController;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

public class SeerApiCore extends Application<Configuration> {

    private static final Logger log = LoggerFactory.getLogger(SeerApiCore.class);

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        log.info("Deploying services.");
        environment.jersey().register(new HealthRestApiController());

        ServiceLoader<SeerApiPlugin> loader = ServiceLoader.load(SeerApiPlugin.class);
        Iterator<SeerApiPlugin> it = loader.iterator();
        while (it.hasNext()) {
            SeerApiPlugin seerApi = it.next();
            environment.jersey().register(seerApi);
        }
    }
}
