package com.networkseer.core.shutdown;

import com.networkseer.common.SeerPlugin;
import com.networkseer.core.SeerCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SeerShutdownHook extends Thread {
    private static final Logger log = LoggerFactory.getLogger(SeerShutdownHook.class);
    @Override
    public void run(){
        log.info("Server is shutting down.");
        deactivateModules();
    }

    private void deactivateModules() {
        Map<String, Boolean>  activatedSeerModules = SeerCore.getActivatedSeerPlugin();
        List<SeerPlugin> seerModules = SeerCore.getSeerPlugins();
        for (SeerPlugin seerModule: seerModules) {
            if (activatedSeerModules.get(seerModule.getClass().getCanonicalName()) != null) {
                seerModule.deactivate();
            }
        }
    }

}
