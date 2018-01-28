package com.networkseer.datasource;

import com.networkseer.common.SeerDirectory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class DataSourceFactory {
    private static final String JNDI_PROPERTY = "jndiName";
    private static Map<String, DataSource> dataSourceMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(DataSourceFactory.class);

    private DataSourceFactory() { }

    public static void registerDatasources() {
        for (File file: getDatasourceFiles()) {
            Properties conf = new Properties();
            try {
                InputStream is = new FileInputStream(file);
                conf.load(is);
                String jndiName = conf.getProperty(JNDI_PROPERTY);
                if (jndiName != null && jndiName.length() > 0) {
                    conf.remove(JNDI_PROPERTY);
                    HikariConfig config = new HikariConfig(conf);
                    DataSource dataSource = new HikariDataSource(config);
                    Context context = new InitialContext();
                    Context jdbcCtx;
                    try {
                        Context compCtx = (Context) context.lookup("java:comp");
                        Context envCtx = compCtx.createSubcontext("env");
                        jdbcCtx = envCtx.createSubcontext("jdbc");
                    } catch (NameAlreadyBoundException e) {
                        jdbcCtx = (Context) context.lookup("java:comp/env/jdbc");
                    }
                    jdbcCtx.bind(jndiName, dataSource);
                    dataSourceMap.put(jndiName, dataSource);
                } else {
                    log.error("JNDI name cannot be found for: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                log.error("Can't locate database configuration", e);
            } catch (NamingException e) {
                log.error("Failed to initialize jndi", e);
            }
        }
    }

    private static File[] getDatasourceFiles() {
        String datasourceDir = SeerDirectory.getDataSourceLocation();
        File datasourceFile = new File(datasourceDir);
        return datasourceFile.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".properties");
            }
        });
    }

}