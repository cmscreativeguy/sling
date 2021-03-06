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
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * test write
 */
public class WritePipeTest extends AbstractPipeTest {

    public static final String NN_PIPED = "piped";
    public static final String NN_VARIABLE_PIPED = "variablePipe";
    public static final String NN_SIMPLETREE = "simpleTree";

    @Before
    public void setup() {
        super.setup();
        context.load().json("/write.json", PATH_PIPE);
    }

    @Test
    public void testSimple() throws Exception {
        Resource confResource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_SIMPLE);
        Pipe pipe = plumber.getPipe(confResource);
        assertNotNull("pipe should be found", pipe);
        assertTrue("this pipe should be marked as content modifier", pipe.modifiesContent());
        pipe.getOutput();
        context.resourceResolver().commit();
        ValueMap properties =  context.resourceResolver().getResource("/content/fruits/apple").adaptTo(ValueMap.class);
        assertTrue("There should be hasSeed set to true", properties.get("hasSeed", false));
        assertArrayEquals("Colors should be correctly set", new String[]{"green", "red"}, properties.get("colors", String[].class));
        assertFalse("worm property should be gone (${null} conf)", properties.get("worm", false));
    }

    /**
     *
     * @param resource
     */
    public static void assertPiped(Resource resource) {
        ValueMap properties = resource.adaptTo(ValueMap.class);
        assertArrayEquals("Second fruit should have been correctly instantiated & patched, added to the first", new String[]{"apple","banana"}, properties.get("fruits", String[].class));
        assertArrayEquals("Fixed mv should be there", new String[]{"cabbage","carrot"}, properties.get("fixedVegetables", String[].class));
    }

    @Test
    public void testPiped() throws Exception {
        Resource confResource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_PIPED);
        Pipe pipe = plumber.getPipe(confResource);
        assertNotNull("pipe should be found", pipe);
        assertTrue("this pipe should be marked as content modifier", pipe.modifiesContent());
        Iterator<Resource> it = pipe.getOutput();
        assertTrue("There should be one result", it.hasNext());
        Resource resource = it.next();
        assertNotNull("The result should not be null", resource);
        assertEquals("The result should be the configured one in the piped write pipe", "/content/fruits", resource.getPath());
        context.resourceResolver().commit();
        ValueMap properties = resource.adaptTo(ValueMap.class);
        assertArrayEquals("First fruit should have been correctly instantiated & patched from nothing", new String[]{"apple"}, properties.get("fruits", String[].class));
        assertTrue("There should be a second result awaiting", it.hasNext());
        resource = it.next();
        assertNotNull("The second result should not be null", resource);
        assertEquals("The second result should be the configured one in the piped write pipe", "/content/fruits", resource.getPath());
        context.resourceResolver().commit();
        assertPiped(resource);
    }

    @Test
    public void testVariablePiped() throws Exception {
        String pipePath = PATH_PIPE + "/" + NN_VARIABLE_PIPED;
        Resource confResource = context.resourceResolver().getResource(pipePath);
        Pipe pipe = plumber.getPipe(confResource);
        assertNotNull("pipe should be found", pipe);
        Iterator<Resource> it = pipe.getOutput();
        Resource resource = it.next();
        assertEquals("path should be the one configured in first pipe", pipePath + "/conf/fruit/conf/apple", resource.getPath());
        context.resourceResolver().commit();
        ValueMap properties = resource.adaptTo(ValueMap.class);
        assertEquals("Configured value should be written", "apple is a fruit and its color is green", properties.get("jcr:description", ""));
        assertEquals("Worm has been removed", "", properties.get("worm", ""));
        Resource archive = resource.getChild("archive/wasthereworm");
        assertNotNull("there is an archive of the worm value", archive);
        assertEquals("Worm value has been written at the same time", "true", archive.adaptTo(String.class));
    }

    @Test
    public void testSimpleTree() throws Exception {
        Resource confResource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_SIMPLETREE);
        Pipe pipe = plumber.getPipe(confResource);
        assertNotNull("pipe should be found", pipe);
        assertTrue("this pipe should be marked as content modifier", pipe.modifiesContent());
        pipe.getOutput();
        context.resourceResolver().commit();
        Resource appleResource = context.resourceResolver().getResource("/content/fruits/apple");
        ValueMap properties =  appleResource.adaptTo(ValueMap.class);
        assertTrue("There should be hasSeed set to true", properties.get("hasSeed", false));
        assertArrayEquals("Colors should be correctly set", new String[]{"green", "red"}, properties.get("colors", String[].class));
        Node appleNode = appleResource.adaptTo(Node.class);
        NodeIterator children = appleNode.getNodes();
        assertTrue("Apple node should have children", children.hasNext());
    }
}