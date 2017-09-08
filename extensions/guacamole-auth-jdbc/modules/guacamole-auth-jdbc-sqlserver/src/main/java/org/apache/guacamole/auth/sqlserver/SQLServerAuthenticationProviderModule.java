/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.auth.sqlserver;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import java.util.Properties;
import org.apache.guacamole.GuacamoleException;
import org.mybatis.guice.datasource.helper.JdbcHelper;

/**
 * Guice module which configures SQLServer-specific injections.
 */
public class SQLServerAuthenticationProviderModule implements Module {

    /**
     * MyBatis-specific configuration properties.
     */
    private final Properties myBatisProperties = new Properties();

    /**
     * SQLServer-specific driver configuration properties.
     */
    private final Properties driverProperties = new Properties();

    /**
     * Whether or not to use JTDS Driver
     */
    private String sqlServerDriver;

    /**
     * Creates a new SQLServer authentication provider module that configures
     * driver and MyBatis properties using the given environment.
     *
     * @param environment
     *     The environment to use when configuring MyBatis and the underlying
     *     JDBC driver.
     *
     * @throws GuacamoleException
     *     If a required property is missing, or an error occurs while parsing
     *     a property.
     */
    public SQLServerAuthenticationProviderModule(SQLServerEnvironment environment)
            throws GuacamoleException {

        // Set the SQLServer-specific properties for MyBatis.
        myBatisProperties.setProperty("mybatis.environment.id", "guacamole");
        myBatisProperties.setProperty("JDBC.host", environment.getSQLServerHostname());
        myBatisProperties.setProperty("JDBC.port", String.valueOf(environment.getSQLServerPort()));
        myBatisProperties.setProperty("JDBC.schema", environment.getSQLServerDatabase());
        myBatisProperties.setProperty("JDBC.username", environment.getSQLServerUsername());
        myBatisProperties.setProperty("JDBC.password", environment.getSQLServerPassword());
        myBatisProperties.setProperty("JDBC.autoCommit", "false");
        myBatisProperties.setProperty("mybatis.pooled.pingEnabled", "true");
        myBatisProperties.setProperty("mybatis.pooled.pingQuery", "SELECT 1");

        // Use UTF-8 in database
        driverProperties.setProperty("characterEncoding", "UTF-8");

        // Capture which driver to use for the connection.
        this.sqlServerDriver = environment.getSQLServerDriver();

    }

    @Override
    public void configure(Binder binder) {

        // Bind SQLServer-specific properties
        // Look at the property to choose the correct driver.
        if (sqlServerDriver.equals(SQLServerEnvironment.SQLSERVER_DRIVER_JTDS))
            JdbcHelper.SQL_Server_jTDS.configure(binder);
        else if (sqlServerDriver.equals(SQLServerEnvironment.SQLSERVER_DRIVER_DATADIRECT))
            JdbcHelper.SQL_Server_DataDirect.configure(binder);
        else if (sqlServerDriver.equals(SQLServerEnvironment.SQLSERVER_DRIVER_MS))
            JdbcHelper.SQL_Server_MS_Driver.configure(binder);
        else
            JdbcHelper.SQL_Server_2005_MS_Driver.configure(binder);
        
        // Bind MyBatis properties
        Names.bindProperties(binder, myBatisProperties);

        // Bind JDBC driver properties
        binder.bind(Properties.class)
            .annotatedWith(Names.named("JDBC.driverProperties"))
            .toInstance(driverProperties);

    }

}
