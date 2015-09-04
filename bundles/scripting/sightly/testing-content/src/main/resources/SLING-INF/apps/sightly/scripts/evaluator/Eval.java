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
package apps.sightly.scripts.evaluator;

import javax.script.Bindings;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.apache.sling.scripting.sightly.runtime.Evaluator;

public class Eval implements Use {

    public static final String PROPERTIES = "properties";
    public static final String TEST = "test";

    private String test;

    public void init(Bindings bindings) {
        Resource resource = (Resource)bindings.get(SlingBindings.RESOURCE);
        SlingScriptHelper sling = (SlingScriptHelper)bindings.get(SlingBindings.SLING);
        ValueMap properties = (ValueMap)bindings.get(PROPERTIES);

        if (properties != null) {
            Evaluator evaluator = sling.getService(Evaluator.class);
            test = evaluator.evaluate(properties.get(TEST, resource.getPath()), bindings);
        }

    }

    public String getTest() {
        return test;
    }
}