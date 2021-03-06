/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.pipes;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Map;
import java.util.Set;

/**
 * Plumber is an osgi service aiming to make pipes available to the sling system, in order to
 */
public interface Plumber {

    public static final String RESOURCE_TYPE = "slingPipes/plumber";

    /**
     * Instantiate a pipe from the given resource and returns it
     * @param resource
     * @return
     */
    Pipe getPipe(Resource resource);

    /**
     * Executes a pipe at a certain path
     * @param resolver resource resolver with which pipe will be executed
     * @param path path of a valid pipe configuration
     * @param bindings bindings to add to the execution of the pipe, can be null
     * @param save in case that pipe writes anything, wether the plumber should save changes or not
     *
     * @return
     */
    Set<String> execute(ResourceResolver resolver, String path, Map bindings, boolean save) throws Exception;

    /**
     * Executes a given pipe
     * @param resolver resource resolver with which pipe will be executed
     * @param pipe pipe to execute
     * @param bindings bindings to add to the execution of the pipe, can be null
     * @param save in case that pipe writes anything, wether the plumber should save changes or not
     *
     * @return
     */
    Set<String> execute(ResourceResolver resolver, Pipe pipe, Map bindings, boolean save) throws Exception;

    /**
     * Registers
     * @param type
     * @param pipeClass
     */
    void registerPipe(String type, Class<? extends BasePipe> pipeClass);
}
