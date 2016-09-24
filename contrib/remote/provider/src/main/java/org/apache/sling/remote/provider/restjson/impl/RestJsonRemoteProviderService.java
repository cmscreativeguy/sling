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
package org.apache.sling.remote.provider.restjson.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.remote.provider.RemoteProviderService;
import org.apache.sling.remote.provider.RemoteResource;
import org.apache.sling.remote.provider.restjson.DataTransformer;
import org.apache.sling.remote.provider.restjson.RequestProcessor;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation of a {@link RemoteProviderService} which uses a REST/JSON client to retrieve data
 * from remote endpoints.
 */
@Service
@Component(metatype = true,
        name="org.apache.sling.remote.provider.restjson.impl.RestJsonRemoteProviderService.factory.config",
        label = "Apache Sling Remote Resource Provider REST/JSON RemoteProviderService",
        description = "Defines a simple REST/JSON HTTP client to load remote data",
        configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@SuppressWarnings("unused")
public class RestJsonRemoteProviderService implements RemoteProviderService {

    private final Logger log = LoggerFactory.getLogger(RestJsonRemoteProviderService.class);

    private CloseableHttpClient httpClient;

    @Property(label = "Path",
            description = "Path to match.",
            cardinality = Integer.MAX_VALUE)
    private static final String PATH_PROPERTY = "path";

    private String path;

    @Property(label = "uri",
            description = "URI for remote REST/JSON service.",
            cardinality = Integer.MAX_VALUE,
            value = "")
    private static final String URI_PROPERTY = "uri";

    private String uri;

    @Reference(cardinality= ReferenceCardinality.OPTIONAL_MULTIPLE,
            bind="bindProcessor", unbind="unbindProcessor",
            referenceInterface = RequestProcessor.class,
            policy = ReferencePolicy.DYNAMIC)
    List<RequestProcessor> requestProcessors = new LinkedList<>();

    @SuppressWarnings("unused")
    private void bindProcessor(RequestProcessor processor){
        requestProcessors.add(processor);
    }

    @SuppressWarnings("unused")
    private void unbindProcessor(RequestProcessor processor){
        requestProcessors.remove(processor);
    }

    @Reference(cardinality= ReferenceCardinality.OPTIONAL_MULTIPLE,
            bind="bindTransformer", unbind="unbindTransformer",
            referenceInterface = DataTransformer.class,
            policy = ReferencePolicy.DYNAMIC)
    List<DataTransformer> dataTransformers = new LinkedList<>();

    @SuppressWarnings("unused")
    private void bindTransformer(DataTransformer transformer){
        dataTransformers.add(transformer);
    }

    @SuppressWarnings("unused")
    private void unbindTransformer(DataTransformer transformer){
        dataTransformers.remove(transformer);
    }

    @Activate
    @SuppressWarnings("unused")
    private void activate(ComponentContext componentContext, Map<String, Object> config) {
        path = PropertiesUtil.toString(config.get(PATH_PROPERTY), null);
        uri = PropertiesUtil.toString(config.get(URI_PROPERTY), null);
        log.debug("activate(path = {}, uri={}", path, uri);
        this.httpClient = HttpClients.createDefault();
    }

    @Deactivate
    @SuppressWarnings("unused")
    private void deactivate() {
        try {
            httpClient.close();
        } catch (IOException ignore) {}
    }

    /**
     * Creates a {@link RemoteResource} using the data fetched from a remote REST/JSON service.
     * Before sending the HTTP request it executes all {@link RequestProcessor}s.
     * After retrieving the JSON data it executes all {@link DataTransformer}s.
     *
     * @param resolver The {@link ResourceResolver}
     * @param path The resource's path
     *
     * @return {@link RemoteResource} or {@code null}
     */
    @Override
    public RemoteResource get(ResourceResolver resolver, String path) {
        if (StringUtils.isEmpty(path) || !path.startsWith(this.path)) {
            log.debug("Not serving {}", StringUtils.isEmpty(path) ? "empty paths" : path);
            return null;
        }
        HttpGet get = new HttpGet(this.uri.replace("{}", path.replaceFirst(this.path, "")));
        get.addHeader("Accept", "application/json");
        CloseableHttpResponse response = null;
        log.debug("Processors {}", requestProcessors.size());
        for (RequestProcessor processor : requestProcessors) {
            processor.process(get);
        }
        try {
            log.debug("Loading {} ...", get.getURI());
            response = httpClient.execute(get);
            int status = response.getStatusLine().getStatusCode();
            log.debug("Returned {}", status);
            if (status == HttpStatus.SC_OK) {
                String str = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject data = new JSONObject(str);
                if (data == null) {
                    log.debug("Data cannot be transformed to JSONObject");
                    return null;
                }
                log.debug("Transformers {}", dataTransformers.size());
                for (DataTransformer transformer : dataTransformers) {
                    data = transformer.transform(data);
                }
                return new RemoteResource(resolver, path, data);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve remote resource", e);
        } finally {
            safeClose(response);
        }
        return null;
    }

    /**
     * Safely closes a {@link Closeable} by swallowing any thrown {@link Exception}.
     *
     * @param closeable The {@link Closeable} to close
     */
    private void safeClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception ignore) {}
    }
}
