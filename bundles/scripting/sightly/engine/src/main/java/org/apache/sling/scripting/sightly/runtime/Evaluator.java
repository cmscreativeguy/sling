/*******************************************************************************
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
 ******************************************************************************/
package org.apache.sling.scripting.sightly.runtime;

import javax.script.Bindings;

import aQute.bnd.annotation.ConsumerType;

/**
 * <p>An {@code Evaluator} takes in a {@code String} possibly containing Sightly expressions and
 * evaluates the expression at runtime.</p>
 */
@ConsumerType
public interface Evaluator {

    /**
     * Identifies Sightly expressions in {@code input} string and evaluates them at runtime using the provided
     * {@code bindings}
     *
     * @param input
     * @param bindings
     * @return
     */
    String evaluate(String input, Bindings bindings);
}
