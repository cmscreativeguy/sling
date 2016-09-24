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
package org.apache.sling.remote.provider;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;

/**
 * This is the worker for getting/updating data from/to remote services
 */
@ProviderType
public interface RemoteProviderService {

    /**
     * Returns a {@link RemoteResource}, in case the service is able to, {@code null} otherwise.
     *
     * @param resolver The {@link ResourceResolver}
     * @param path The resource's path
     * @return {@link RemoteResource} or {@code null}
     */
    RemoteResource get(ResourceResolver resolver, String path);
}
