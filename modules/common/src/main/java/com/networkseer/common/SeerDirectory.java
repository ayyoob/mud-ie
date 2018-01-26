package com.networkseer.common;

public class SeerDirectory {

    private static final String SEER_HOME_DIR = "seer.home";
    private static final String SEER_CONFIG_DIR = "seer.config.dir.path";
    private static final String DATASOURCE_DIR = "seer.datasources.dir.path";
    private static final String LOG_DIR = "seer.logs.dir.path";

    public static String getDataSourceLocation() {
        return System.getProperty(DATASOURCE_DIR);
    }

    public static String getConfigDirectory() {
        return System.getProperty(SEER_CONFIG_DIR);
    }

    public static String getLogDirectory() {
        return System.getProperty(LOG_DIR);
    }

    public static String getRootDirectory() {
        return System.getProperty(SEER_HOME_DIR);
    }
}
