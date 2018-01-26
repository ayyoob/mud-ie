package com.networkseer.common;

import javax.sql.DataSource;

public interface SeerDatasource {

    DataSource getDatasource(String jndi);
}
