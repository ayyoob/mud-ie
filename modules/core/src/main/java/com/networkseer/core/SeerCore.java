package com.networkseer.core;

import com.networkseer.common.SeerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SeerCore {

    private static final Logger log = LoggerFactory.getLogger(SeerCore.class);
    private static List<SeerPlugin> seerPlugins = new ArrayList<>();
    private static Map<String, Boolean> activatedSeerPlugin = new HashMap<>();
    private static final int MAX_INTERATION = 1000;
    private static int currentIteration = 0;
    private static String apiParams[] = {"server"};

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new SeerShutdownHook());
        if (log.isDebugEnabled()) {
            log.debug("datasource registered");
        }
        loadSeerPlugins();
        activatePlugin();
        SeerApiCore seerApiCore = new SeerApiCore();
        seerApiCore.run(apiParams);
        log.info("Server is started.");
    }


    public static void activatePlugin() {
        seerPlugins.forEach(seerPlugin -> {
            List<String> dependencies = seerPlugin.getModuleDependencies();
            if (dependencies == null || dependencies.isEmpty()) {
                seerPlugin.activate();
                activatedSeerPlugin.put(seerPlugin.getClass().getCanonicalName(), true);
            }
        });

        while(currentIteration<MAX_INTERATION) {
            currentIteration++;
            activatePendingPlugins();
            if (activatedSeerPlugin.size() == seerPlugins.size()) {
                break;
            }
        }

        seerPlugins.forEach(seerModule -> {
            if (activatedSeerPlugin.get(seerModule.getClass().getCanonicalName()) == null) {
                log.error(seerModule.getClass().getCanonicalName() + " is failed to loaded, check dependencies.");
            }
        });

    }

    private static void loadSeerPlugins() {
        ServiceLoader<SeerPlugin> serviceLoader = ServiceLoader.load(SeerPlugin.class);
        serviceLoader.forEach(service -> {
            seerPlugins.add(service);});
    }

    private static void activatePendingPlugins() {
        seerPlugins.forEach(seerModule -> {
            if (activatedSeerPlugin.get(seerModule.getClass().getCanonicalName()) == null) {
                List<String> dependencies = seerModule.getModuleDependencies();
                boolean canActivate = true;
                for (String dependecy: dependencies) {
                    if (activatedSeerPlugin.get(dependecy) == null) {
                        canActivate = false;
                        break;
                    }
                }
                if (canActivate) {
                    seerModule.activate();
                    activatedSeerPlugin.put(seerModule.getClass().getCanonicalName(), true);
                }
            }
        });
    }

    public static Map<String, Boolean> getActivatedSeerPlugin() {
        return activatedSeerPlugin;
    }

    public static List<SeerPlugin> getSeerPlugins() {
        return seerPlugins;
    }

}
