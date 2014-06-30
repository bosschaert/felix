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
package org.apache.felix.resolver.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.resolver.Logger;
import org.apache.felix.resolver.ResolverImpl;
import org.junit.Test;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;
import org.osgi.service.resolver.Resolver;

public class ResolverTest
{
    @Test
    public void testScenario1() throws Exception
    {
        Resolver resolver = new ResolverImpl(new Logger(Logger.LOG_DEBUG));

        Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
        Map<Requirement, List<Capability>> candMap = new HashMap<Requirement, List<Capability>>();
        List<Resource> mandatory;
        ResolveContextImpl rci;
        Map<Resource, List<Wire>> wireMap;

        mandatory = populateScenario1(wirings, candMap);
        rci = new ResolveContextImpl(wirings, candMap, mandatory, Collections.<Resource>emptyList());
        wireMap = resolver.resolve(rci);
        assertEquals(2, wireMap.size());

        Resource aRes = findResource("A", wireMap.keySet());
        List<Wire> aWires = wireMap.get(aRes);
        assertEquals(0, aWires.size());

        Resource bRes = findResource("B", wireMap.keySet());

        List<Wire> bWires = wireMap.get(bRes);
        assertEquals(1, bWires.size());
        Wire bWire = bWires.iterator().next();
        assertEquals(aRes, bWire.getProvider());
        assertEquals(bRes, bWire.getRequirer());
        Capability cap = bWire.getCapability();
        assertEquals(PackageNamespace.PACKAGE_NAMESPACE, cap.getNamespace());
        assertEquals(1, cap.getAttributes().size());
        assertEquals("foo", cap.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE));
        assertEquals(0, cap.getDirectives().size());
        assertEquals(aRes, cap.getResource());

        Requirement req = bWire.getRequirement();
        assertEquals(1, req.getDirectives().size());
        assertEquals("(osgi.wiring.package=foo)", req.getDirectives().get(PackageNamespace.REQUIREMENT_FILTER_DIRECTIVE));
        assertEquals(0, req.getAttributes().size());
        assertEquals(PackageNamespace.PACKAGE_NAMESPACE, req.getNamespace());
        assertEquals(bRes, req.getResource());
    }

    private static Resource findResource(String identity, Collection<Resource> resources)
    {
        for (Resource r : resources)
        {
            for (Capability c : r.getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE)) {
                if (identity.equals(c.getAttributes().get(IdentityNamespace.IDENTITY_NAMESPACE)))
                    return r;
            }
        }
        return null;
    }

    private static List<Resource> populateScenario1(Map<Resource, Wiring> wirings, Map<Requirement, List<Capability>> candMap)
    {
        wirings.clear();
        candMap.clear();

        ResourceImpl exporter = new ResourceImpl("A");
        exporter.addCapability(new PackageCapability(exporter, "foo"));
        ResourceImpl importer = new ResourceImpl("B");
        importer.addRequirement(new PackageRequirement(importer, "foo"));
        candMap.put(importer.getRequirements(null).get(0), exporter.getCapabilities(PackageNamespace.PACKAGE_NAMESPACE));
        List<Resource> resources = new ArrayList<Resource>();
        resources.add(importer);
        return resources;
    }

}
