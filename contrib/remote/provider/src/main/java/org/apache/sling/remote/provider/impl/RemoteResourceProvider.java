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

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.remote.provider.RemoteProviderService;
import org.apache.sling.remote.provider.RemoteResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ResourceProvider} using a list of {@link RemoteProviderService}. The provider iterates all
 * remote services and returns a {@link RemoteResource} as soon as one of the services returns a non-{@code null} one.
 */
public class RemoteResourceProvider implements ResourceProvider {

    private final Logger log = LoggerFactory.getLogger(RemoteResourceProvider.class);
    private final String root;
    private final List<RemoteProviderService> remoteServices;

    public RemoteResourceProvider(String root, List<RemoteProviderService> remoteServices) {
        this.root = root;
        this.remoteServices = remoteServices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource(ResourceResolver resourceResolver, HttpServletRequest request, String path) {
        return getResource(resourceResolver, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource(ResourceResolver resourceResolver, String path) {
        log.debug("getResource(path={}, remoteServices={})", path, remoteServices.size());
        if (remoteServices.isEmpty()) {
            log.debug("No remote services available");
            return null;
        }

        String relPath = toRelativePath(path);
        if (StringUtils.isEmpty(relPath) || (relPath.lastIndexOf(".") > relPath.lastIndexOf("/"))) {
            log.debug("Not serving empty paths or files");
            return null;
        }

        for (RemoteProviderService remoteService : remoteServices) {
            RemoteResource resource =  remoteService.get(resourceResolver, relPath);
            if (resource != null) {
                return resource;
            }
        }

        log.debug("No service was able to fetch the resource");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Resource> listChildren(Resource parent) {
        log.debug("listChildren({})", parent);
        RemoteResource remoteResource = parent.adaptTo(RemoteResource.class);
        if (remoteResource == null) {
            return null;
        }
        return remoteResource.listChildren();
    }

    private String toRelativePath(String path) {
        return StringUtils.substringAfter(path, root);
    }
}
