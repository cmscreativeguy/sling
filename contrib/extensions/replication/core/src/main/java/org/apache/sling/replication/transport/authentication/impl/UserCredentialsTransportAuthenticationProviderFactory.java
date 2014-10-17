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
package org.apache.sling.replication.transport.authentication.impl;

import java.util.Map;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.replication.agent.ReplicationComponentFactory;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationContext;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationException;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(label = "User Credentials based Transport Authentication Provider Factory",
        configurationFactory = true,
        specVersion = "1.1",
        policy = ConfigurationPolicy.REQUIRE)
@Service(value = TransportAuthenticationProvider.class)
public class UserCredentialsTransportAuthenticationProviderFactory implements
        TransportAuthenticationProvider {

    @Property(value = ReplicationComponentFactory.TRANSPORT_AUTHENTICATION_PROVIDER_USER, propertyPrivate = true)
    private static final String TYPE = ReplicationComponentFactory.COMPONENT_TYPE;

    @Property
    public final static String USERNAME = ReplicationComponentFactory.TRANSPORT_AUTHENTICATION_PROVIDER_USER_PROPERTY_USERNAME;

    @Property
    public final static String PASSWORD = ReplicationComponentFactory.TRANSPORT_AUTHENTICATION_PROVIDER_USER_PROPERTY_PASSWORD;

    private final Logger log = LoggerFactory.getLogger(getClass());


    @Reference
    ReplicationComponentFactory replicationComponentFactory;

    private TransportAuthenticationProvider transportAuthenticationProvider;


    public void activate(Map<String, Object> config) {
        transportAuthenticationProvider = replicationComponentFactory.createComponent(TransportAuthenticationProvider.class, config, null);
    }

    public Object authenticate(Object authenticable, TransportAuthenticationContext context)
            throws TransportAuthenticationException {
        return transportAuthenticationProvider.authenticate(authenticable, context);
    }

    public boolean canAuthenticate(Class authenticable) {
        return transportAuthenticationProvider.canAuthenticate(authenticable);
    }

}