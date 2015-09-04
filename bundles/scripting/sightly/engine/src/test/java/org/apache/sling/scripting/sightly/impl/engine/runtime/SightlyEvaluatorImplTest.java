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
package org.apache.sling.scripting.sightly.impl.engine.runtime;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.scripting.sightly.impl.utils.RenderUtils;
import org.apache.sling.scripting.sightly.runtime.Evaluator;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SightlyEvaluatorImplTest {

    public class Mock {

        @SuppressWarnings("unused")
        public String getTest() {
            return "test";
        }

        @SuppressWarnings("unused")
        public Mock getMock() {
            return new Mock();
        }

    }

    private Bindings bindings;
    private Evaluator evaluator;

    @Before
    public void init() {
        bindings = new SimpleBindings();
        evaluator = new SightlyEvaluatorImpl();
    }

    private Object eval(String expression) {
        return evaluator.evaluate(expression, bindings);
    }

    @Test
    public void testPassthrough() {
        assertEquals("Hello world!", eval("Hello world!"));
    }

    @Test
    public void testLiterals() {
        // Numeric
        assertEquals("0", eval("${ 0 }"));
        assertEquals("42", eval("${ 42 }"));
        assertEquals("0.1", eval("${ 0.1 }"));
        // Boolean
        assertEquals("true", eval("${ true }"));
        // String
        assertEquals("string", eval("${ 'string' }"));
        assertEquals("string", eval("${ \"string\" }"));
        // Array
        assertEquals(RenderUtils.toString(new Object[]{1, 2, 3, true, "string"}),
                eval("${ [1, 2, 3, true, 'string'] }"));
    }

    @Test
    public void testIdentifiers() {
        // Missing identifier
        assertEquals("", eval("${ bogusIdentifier }"));

        bindings.put("myVar", "myVarValue");
        SlingHttpServletRequest request = new MockSlingHttpServletRequest();
        request.setAttribute("myOtherVar", "myOtherVarValue");
        bindings.put("request", request);

        // Lookup identifier in context bindings
        assertEquals("myVarValue", eval("${ myVar }"));

        // Lookup identifier in request attributes
        assertEquals("myOtherVarValue", eval("${ myOtherVar }"));
    }

    @Test
    public void testPropertyAccess() {
        bindings.put("mock", new Mock());
        bindings.put("test", "test");

        assertEquals("test", eval("${ mock.test }"));
        assertEquals("test", eval("${ mock.mock.test }"));
        assertEquals("test", eval("${ mock['test'] }"));
        assertEquals("test", eval("${ mock[test] }"));
        assertEquals("test", eval("${ mock[mock.test] }"));
        assertEquals("test", eval("${ mock.mock[mock.test] }"));
        assertEquals("test", eval("${ mock[mock.mock.test] }"));

        assertEquals("", eval("${ foo.bar }"));
    }

    @Test
    public void testArrays() {
        bindings.put("myArray", new Object[]{"zero", "one", "two"});
        bindings.put("two", 2);

        assertEquals("one", eval("${ myArray[1] }"));
        assertEquals("two", eval("${ myArray[two] }"));
    }

    @Test
    public void testLogicalOperators() {
        bindings.put("varTrue", true);
        bindings.put("varFalse", false);

        // AND
        assertEquals("true", eval("${ varTrue && varTrue }"));
        assertEquals("false", eval("${ varTrue && varFalse }"));

        // OR
        assertEquals("true", eval("${ varTrue || varFalse }"));

        // NOT
        assertEquals("true", eval("${ !varFalse }"));
        assertEquals("false", eval("${ !varTrue }"));

        // GROUPING
        assertEquals("false", eval("${ true && !(varTrue || varFalse) }"));

        // TERNARY
        assertEquals("true", eval("${ varFalse ? varFalse : varTrue }"));
    }

    @Test
    public void testComparisonOperators() {
        // NULL comparison
        assertEquals("true", eval("${ null == null }"));
        assertEquals("false", eval("${ null != null }"));

        // String comparison
        assertEquals("true", eval("${ 'string' == 'string' }"));
        assertEquals("false", eval("${ 'string' != 'string' }"));
        assertEquals("false", eval("${ 'string1' == 'string2' }"));
        assertEquals("true", eval("${ 'string1' != 'string2' }"));

        // Number comparison
        assertEquals("true", eval("${ 1 == 1 }"));
        assertEquals("false", eval("${ 1 != 1 }"));
        assertEquals("false", eval("${ 1 == 2 }"));
        assertEquals("true", eval("${ 1 != 2 }"));

        assertEquals("true", eval("${ 1 <= 1 }"));
        assertEquals("false", eval("${ 1 <= 0 }"));
        assertEquals("false", eval("${ 1 < 1 }"));
        assertEquals("true", eval("${ 1 < 2 }"));

        assertEquals("true", eval("${ 1 >= 1 }"));
        assertEquals("false", eval("${ 0 >= 1 }"));
        assertEquals("false", eval("${ 1 > 1 }"));
        assertEquals("true", eval("${ 2 > 1 }"));

        // Boolean comparison
        assertEquals("true", eval("${ true == true }"));
        assertEquals("false", eval("${ true == false }"));
        assertEquals("false", eval("${ true != true }"));
        assertEquals("true", eval("${ true != false }"));

    }

    @Test
    public void testStringAndExpressions() {
        bindings.put("one", "one1");
        bindings.put("two", "two2");

        assertEquals("Marker one1", eval("Marker ${ one }"));
        assertEquals("Marker one1 and two2", eval("Marker ${ one } and ${ two }"));
    }
}
