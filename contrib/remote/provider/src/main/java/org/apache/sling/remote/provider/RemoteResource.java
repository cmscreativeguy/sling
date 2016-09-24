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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteResource} is an implementation of a {@link Resource} backed by {@link JSONObject data}.
 *
 * It can be adapted to a {@link JSONObject} or a {@link ValueMap}
 */
public class RemoteResource extends AbstractResource {

    public static final String TYPE = "/remote";
    public static final String SUPER_TYPE = "";

    private Logger log = LoggerFactory.getLogger(RemoteResource.class);

    private final ResourceResolver resourceResolver;
    private final ResourceMetadata metadata;
    private final JSONObject data;

    public RemoteResource(ResourceResolver resourceResolver, String path, JSONObject data) {
        this.resourceResolver = resourceResolver;
        this.metadata = new ResourceMetadata();
        metadata.setResolutionPath(path);
        this.data = data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return metadata.getResolutionPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceSuperType() {
        return SUPER_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceMetadata getResourceMetadata() {
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type == JSONObject.class) {
            return (AdapterType) data;
        } else if (type == ValueMap.class ) {
            try {
                // adapting the same mutable object. Should this be cloned on each adaption?
                return (AdapterType) new ValueMapDecorator(toMap(data));
            } catch (Exception e) {
                log.error("Error while adapting", e);
            }
        }
        return super.adaptTo(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName()
                + ", type=" + getResourceType()
                + ", superType=" + getResourceSuperType()
                + ", path=" + getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Resource> listChildren() {
        return new Iterator<Resource>() {

            Iterator<String> iter = data.keys();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Resource next() {
                log.debug("next");
                return getResourceResolver().getResource(iter.next());
            }
        };
    }

    /**
     * Convert a {@link JSONObject} to a {@link Map}
     *
     * @param jsonObject The {@link JSONObject} to convert
     * @return {@link Map} representation of the object
     *
     * @throws JSONException
     */
    protected static Map<String, Object> toMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonObject.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object o = jsonObject.get(key);
            Object to = null;
            if(o instanceof JSONArray) {
                to = toList((JSONArray) o);
            } else if(o instanceof JSONObject) {
                to = toMap((JSONObject) o);
            } else {
                to = o;
            }
            map.put(key, to);
        }
        return map;
    }

    /**
     * Convert a {@link JSONArray} to a {@link List}
     *
     * @param jsonArray The {@link JSONArray} to convert
     * @return {@link List} representation of the {@link JSONArray}
     *
     * @throws JSONException
     */
    protected static List<Object> toList(JSONArray jsonArray) throws JSONException {
        List<Object> list = new LinkedList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            Object to = null;
            if(value instanceof JSONArray) {
                to = toList((JSONArray) value);
            } else if(value instanceof JSONObject) {
                to = toMap((JSONObject) value);
            }
            list.add(to);
        }
        return list;
    }
}
