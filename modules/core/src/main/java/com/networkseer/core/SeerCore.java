package com.networkseer.core;

import com.networkseer.common.SeerDirectory;
import com.networkseer.common.SeerPlugin;
import com.networkseer.core.api.SeerApiCore;
import com.networkseer.core.shutdown.SeerShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SeerCore {

    private static final Logger log = LoggerFactory.getLogger(SeerCore.class);
    private static List<SeerPlugin> seerPlugins = new ArrayList<>();
    private static Map<String, Boolean> activatedSeerPlugin = new HashMap<>();
    private static final int MAX_INTERATION = 1000;
    private static int currentIteration = 0;
    private static final String SERVER_TAG = "server";
    private static final String CONFIG_FILE_NAME = "seer.yml";
    private static final String PID_FILE_NAME = "seer.pid";

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new SeerShutdownHook());
        setupPidFile();
        SeerApiCore seerApiCore = new SeerApiCore();
        loadSeerPlugins();
        activatePlugin();
        String apiParams[] = {SERVER_TAG, SeerDirectory.getConfigDirectory() + File.separator + CONFIG_FILE_NAME};
        seerApiCore.run(apiParams);
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

    public static void setupPidFile() {
        String processName =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        long pid = Long.parseLong(processName.split("@")[0]);
        try {
            FileWriter fileWriter = new FileWriter(SeerDirectory.getRootDirectory()
                    + File.separator + PID_FILE_NAME);
            fileWriter.write("" + pid);
            fileWriter.close();
        } catch (IOException e) {
            log.error("Failed to setup pid file");
        }

    }

}
