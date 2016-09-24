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
package org.apache.sling.remote.provider.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceProviderFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.remote.provider.RemoteProviderService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Deactivate;

/**
 * {@link ResourceProviderFactory} implementation that uses a {@link RemoteResourceProvider}
 */
@Component(immediate = true, metatype = true,
        name="org.apache.sling.remote.provider.RemoteResourceProviderFactory.factory.config",
        label = "Apache Sling Remote Resource Provider Factory",
        description = "Defines a resource provider factory with remote persistence.",
        configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Service(value = ResourceProviderFactory.class)
public final class RemoteResourceProviderFactory implements ResourceProviderFactory {

    private static final String REMOTE = "/remote";
    private Logger log = LoggerFactory.getLogger(RemoteResourceProviderFactory.class);

    @Property(label = "Root paths",
              description = "Root paths for resource provider.",
              cardinality = Integer.MAX_VALUE,
              value = REMOTE)
    private static final String PROVIDER_ROOTS_PROPERTY = ResourceProvider.ROOTS;

    private String root;


    @Reference(cardinality= ReferenceCardinality.OPTIONAL_MULTIPLE,
            bind="bind", unbind="unbind",
            referenceInterface = RemoteProviderService.class,
            policy = ReferencePolicy.DYNAMIC)
    private final List<RemoteProviderService> remoteServices = new LinkedList<>();

    @SuppressWarnings("unused")
    private void bind(RemoteProviderService remoteService){
        remoteServices.add(remoteService);
    }

    @SuppressWarnings("unused")
    private void unbind(RemoteProviderService remoteService){
        remoteServices.remove(remoteService);
    }

    @Activate
    @SuppressWarnings("unused")
    private void activate(ComponentContext componentContext, Map<String, Object> config) {
        root = PropertiesUtil.toString(config.get(PROVIDER_ROOTS_PROPERTY), null);
        log.debug("activate(roots={})", root);
    }

    @Deactivate
    @SuppressWarnings("unused")
    private void deactivate() {
        log.debug("Deactivate()");
    }

    /**
     * {@inheritDoc}
     */
    public ResourceProvider getResourceProvider(Map<String, Object> authenticationInfo) throws LoginException {
        return new RemoteResourceProvider(root, remoteServices);
    }

    /**
     * {@inheritDoc}
     */
    public ResourceProvider getAdministrativeResourceProvider(Map<String, Object> authenticationInfo) throws LoginException {
        return getResourceProvider(authenticationInfo);
    }
}
