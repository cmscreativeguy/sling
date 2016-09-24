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

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.remote.provider.restjson.RequestProcessor;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Deactivate;

/**
 * A basic implementation of a {@link RequestProcessor} which injects a static header for all requests to a specific URL.
 */
@Service
@Component(metatype = true,
        name="org.apache.sling.remote.provider.restjson.impl.RequestProcessor.factory.config",
        label = "Apache Sling Remote Resource Provider REST/JSON Header Injection Request Processor",
        description = "Defines a basic RequestProcessor to inject headers to a RemoteProviderService request",
        configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
public class HeaderInjectionRequestProcessor implements RequestProcessor {

    private Logger log = LoggerFactory.getLogger(HeaderInjectionRequestProcessor.class);

    @Property(label = "URI",
            description = "URI to match.",
            cardinality = Integer.MAX_VALUE,
            value = "http")
    private static final String URI_PROPERTY = "uri";

    private String uri;

    @Property(label = "header",
            description = "Header to inject.",
            cardinality = Integer.MAX_VALUE,
            value = "")
    private static final String HEADER_PROPERTY = "header";

    private String header;

    @Property(label = "value",
            description = "Value for the header.",
            cardinality = Integer.MAX_VALUE,
            value = "")
    private static final String VALUE_PROPERTY = "value";

    private String value;

    @Activate
    @SuppressWarnings("unused")
    private void activate(ComponentContext componentContext, Map<String, Object> config) {
        uri = PropertiesUtil.toString(config.get(URI_PROPERTY), null);
        if (uri != null) {
            uri = uri.toUpperCase();
        }
        header = PropertiesUtil.toString(config.get(HEADER_PROPERTY), null);
        value = PropertiesUtil.toString(config.get(VALUE_PROPERTY), null);
        log.debug("activate(uri=" + uri + ")");
    }

    @Deactivate
    @SuppressWarnings("unused")
    private void deactivate() {
        log.debug("Deactivate()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(HttpRequestBase request) {
        log.debug("process({})", request);
        if (request == null) {
            log.debug("Not processing null requests");
            return;
        }
        if (header == null) {
            log.debug("No header to inject");
        }
        if (request.getURI().toString().toUpperCase().startsWith(uri)) {
            if (value == null) {
                log.debug("Removed header {}", header);
                request.removeHeaders(header);
            } else {
                log.debug("Added header {}={}", header, value);
                request.addHeader(header, value);
            }
        } else {
            log.debug("URI not matched {}", request.getURI());
        }
    }

}
