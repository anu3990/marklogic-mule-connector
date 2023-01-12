/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2021 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.internal.connection.provider;

import com.marklogic.mule.extension.connector.api.connection.AuthenticationType;
import com.marklogic.mule.extension.connector.api.connection.MarkLogicConnectionType;
import com.marklogic.mule.extension.connector.internal.connection.MarkLogicConnection;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.*;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class (as it's name implies) provides connection instances and the
 * functionality to disconnect and validate those connections.
 * <p>
 * All connection related parameters (values required in order to create a
 * connection) must be declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares
 * that connections resolved by this provider will be pooled and reused. There
 * are other implementations like {@link CachedConnectionProvider} which lazily
 * creates and caches connections or simply {@link ConnectionProvider} if you
 * want a new connection each time something requires one.
 */
public class MarkLogicConnectionProvider implements CachedConnectionProvider<MarkLogicConnection>
{

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicConnectionProvider.class);

    @DisplayName("Host name")
    @Parameter
    @Summary("The hostname against which operations should run.")
    @Example("localhost")
    private String hostname;

    @DisplayName("Port")
    @Parameter
    @Summary("The app server port against which operations should run.")
    @Example("8010")
    private int port;

    @DisplayName("Database")
    @Parameter
    @Summary("The MarkLogic database name (i.e., xdmp:database-name()), against which operations should run. If not supplied or left as null, the database will be determined automatically by the app server port being called.")
    @Optional(defaultValue = "null")
    @Example("data-hub-STAGING")
    private String database;

    @DisplayName("User name")
    @Parameter
    @Summary("The named user.")
    @Example("admin")
    private String username;

    @DisplayName("Password")
    @Parameter
    @Summary("The named user's password.")
    @Password
    @Example("admin")
    private String password;

    @DisplayName("Authentication Type")
    @Parameter
    @Summary("The authentication type used to authenticate to MarkLogic. Valid values are: digest, basic.")
    private AuthenticationType authenticationType; 

    @DisplayName("Connection Type")
    @Parameter
    @Summary("The type of connection used to work with MarkLogic, either DIRECT (non-load balanced) or GATEWAY (load-balanced).")
    @Optional
    private MarkLogicConnectionType marklogicConnectionType; 

    @DisplayName("TLS Context")
    @Placement(tab="Security")
    @Parameter
    @Optional
    private TlsContextFactory tlsContextFactory;

    @DisplayName("Kerberos External Name (Not Yet Supported)")
    @Placement(tab="Security")
    @Parameter
    @Summary("If \"kerberos\" is used for the authenticationType parameter, a Kerberos external name value can be supplied if needed.")
    @Optional(defaultValue = "null")
    private String kerberosExternalName;

    @DisplayName("Connection ID")
    @Parameter
    @Summary("An identifier used for the Mulesoft Connector to keep state of its connection to MarkLogic. Also set on the Connector configuration parameters.")
    @Example("testConfig-223efe")
    private String connectionId;

    @Override
    public MarkLogicConnection connect() throws ConnectionException
    {
        MarkLogicConnection conn = new MarkLogicConnection(this);
        logger.info("MarkLogicConnectionProvider connect() called");
        conn.connect();
        return conn;
    }

    @Override
    public void disconnect(MarkLogicConnection connection)
    {
        logger.info("MarkLogicConnectionProvider disconnect() called, connection invalidated");
        connection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(MarkLogicConnection connection)
    {
        ConnectionValidationResult result;
        if (connection.isConnected(port))
        {
            result = ConnectionValidationResult.success();
            logger.info("MarkLogicConnectionProvider validate() result succeeded");
        }
        else
        {
            result = ConnectionValidationResult.failure("Connection failed " + connection.getId(), new Exception());
            logger.info("MarkLogicConnectionProvider validate() result failed");
        }
        return result;
    }

    public String getHostname() {
        return hostname;
    }

    public MarkLogicConnectionProvider withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public int getPort() {
        return port;
    }

    public MarkLogicConnectionProvider withPort(int port) {
        this.port = port;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public MarkLogicConnectionProvider withDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MarkLogicConnectionProvider withUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public MarkLogicConnectionProvider withPassword(String password) {
        this.password = password;
        return this;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public MarkLogicConnectionProvider withAuthenticationType(AuthenticationType type) {
        this.authenticationType = type;
        return this;
    }

    public MarkLogicConnectionType getMarklogicConnectionType() {
        return marklogicConnectionType;
    }

    public MarkLogicConnectionProvider withMarklogicConnectionType(MarkLogicConnectionType type) {
        this.marklogicConnectionType = type;
        return this;
    }

    public TlsContextFactory getTlsContextFactory() {
        return tlsContextFactory;
    }

    public MarkLogicConnectionProvider withTlsContextFactory(TlsContextFactory factory) {
        this.tlsContextFactory = factory;
        return this;
    }

    public String getKerberosExternalName() {
        return kerberosExternalName;
    }

    public MarkLogicConnectionProvider withKerberosExternalName(String name) {
        this.kerberosExternalName = name;
        return this;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public MarkLogicConnectionProvider withConnectionId(String id) {
        this.connectionId = id;
        return this;
    }
}
