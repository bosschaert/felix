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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.resolver.Logger;
import org.apache.felix.resolver.ResolverImpl;
import org.junit.Test;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.Resolver;

public class ResolverTest
{
    @Test
    public void testScenario1() throws Exception
    {
        Resolver resolver = new ResolverImpl(new Logger(Logger.LOG_DEBUG));

        Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
        Map<Requirement, List<Capability>> candMap = new HashMap<Requirement, List<Capability>>();
        List<Resource> mandatory = populateScenario1(wirings, candMap);
        ResolveContextImpl rci = new ResolveContextImpl(wirings, candMap, mandatory, Collections.<Resource> emptyList());

        Map<Resource, List<Wire>> wireMap = resolver.resolve(rci);
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

    @Test
    public void testScenario2() throws Exception
    {
        Resolver resolver = new ResolverImpl(new Logger(Logger.LOG_DEBUG));

        Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
        Map<Requirement, List<Capability>> candMap = new HashMap<Requirement, List<Capability>>();
        List<Resource> mandatory = populateScenario2(wirings, candMap);
        ResolveContextImpl rci = new ResolveContextImpl(wirings, candMap, mandatory, Collections.<Resource> emptyList());

        Map<Resource, List<Wire>> wireMap = resolver.resolve(rci);
        assertEquals(2, wireMap.size());

        Resource bRes = findResource("B", wireMap.keySet());
        List<Wire> bWires = wireMap.get(bRes);
        assertEquals(0, bWires.size());

        Resource cRes = findResource("C", wireMap.keySet());
        List<Wire> cWires = wireMap.get(cRes);
        assertEquals(2, cWires.size());

        boolean foundFoo = false;
        boolean foundBar = false;
        for (Wire w : cWires)
        {
            assertEquals(bRes, w.getProvider());
            assertEquals(cRes, w.getRequirer());

            Capability cap = w.getCapability();
            assertEquals(PackageNamespace.PACKAGE_NAMESPACE, cap.getNamespace());
            assertEquals(bRes, cap.getResource());
            Map<String, Object> attrs = cap.getAttributes();
            assertEquals(1, attrs.size());
            Object pkg = attrs.get(PackageNamespace.PACKAGE_NAMESPACE);
            if ("foo".equals(pkg))
            {
                foundFoo = true;
                assertEquals(0, cap.getDirectives().size());
            }
            else if ("bar".equals(pkg))
            {
                foundBar = true;
                assertEquals(1, cap.getDirectives().size());
                assertEquals("foo", cap.getDirectives().get(PackageNamespace.CAPABILITY_USES_DIRECTIVE));
            }
        }
        assertTrue(foundFoo);
        assertTrue(foundBar);
    }

    @Test
    public void testScenario3() throws Exception
    {
        Resolver resolver = new ResolverImpl(new Logger(Logger.LOG_DEBUG));

        Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
        Map<Requirement, List<Capability>> candMap = new HashMap<Requirement, List<Capability>>();
        List<Resource> mandatory = populateScenario3(wirings, candMap);
        ResolveContextImpl rci = new ResolveContextImpl(wirings, candMap, mandatory, Collections.<Resource> emptyList());

        Map<Resource, List<Wire>> wireMap = resolver.resolve(rci);
        assertEquals(3, wireMap.size());

        Resource cRes = findResource("C", wireMap.keySet());
        List<Wire> cWires = wireMap.get(cRes);
        assertEquals(0, cWires.size());

        Resource dRes = findResource("D", wireMap.keySet());
        List<Wire> dWires = wireMap.get(dRes);
        assertEquals(1, dWires.size());
        Wire dWire = dWires.iterator().next();
        assertEquals(cRes, dWire.getProvider());
        assertEquals(dRes, dWire.getRequirer());
        Capability dwCap = dWire.getCapability();
        assertEquals(PackageNamespace.PACKAGE_NAMESPACE, dwCap.getNamespace());
        assertEquals(1, dwCap.getAttributes().size());
        assertEquals("resources", dwCap.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE));
        assertEquals(0, dwCap.getDirectives().size());
        assertEquals(cRes, dwCap.getResource());

        Resource eRes = findResource("E", wireMap.keySet());
        List<Wire> eWires = wireMap.get(eRes);
        assertEquals(2, eWires.size());

        boolean foundC = false;
        boolean foundD = false;
        for (Wire w : eWires)
        {
            assertEquals(eRes, w.getRequirer());

            Capability cap = w.getCapability();
            if (cap.getNamespace().equals(PackageNamespace.PACKAGE_NAMESPACE))
            {
                assertEquals("resources", cap.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE));
                assertEquals(0, cap.getDirectives().size());
                assertEquals(cRes, cap.getResource());
                foundC = true;

                Requirement req = w.getRequirement();
                assertEquals(PackageNamespace.PACKAGE_NAMESPACE, req.getNamespace());
                assertEquals(eRes, req.getResource());
                assertEquals(0, req.getAttributes().size());
                assertEquals(1, req.getDirectives().size());
                assertEquals("(osgi.wiring.package=resources)", req.getDirectives().get("filter"));
            }
            else if (cap.getNamespace().equals(BundleNamespace.BUNDLE_NAMESPACE))
            {
                assertEquals("D", cap.getAttributes().get(BundleNamespace.BUNDLE_NAMESPACE));
                assertEquals(1, cap.getDirectives().size());
                assertEquals("resources", cap.getDirectives().get(Namespace.CAPABILITY_USES_DIRECTIVE));
                assertEquals(dRes, cap.getResource());
                foundD = true;

                Requirement req = w.getRequirement();
                assertEquals(BundleNamespace.BUNDLE_NAMESPACE, req.getNamespace());
                assertEquals(eRes, req.getResource());
                assertEquals(0, req.getAttributes().size());
                assertEquals(1, req.getDirectives().size());
                assertEquals("(osgi.wiring.bundle=D)", req.getDirectives().get("filter"));
            }
        }
        assertTrue(foundC);
        assertTrue(foundD);
    }

    @Test
    public void testScenario4() throws Exception
    {
        Resolver resolver = new ResolverImpl(new Logger(Logger.LOG_DEBUG));

        Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
        Map<Requirement, List<Capability>> candMap = new HashMap<Requirement, List<Capability>>();
        List<Resource> mandatory = populateScenario4(wirings, candMap);
        ResolveContextImpl rci = new ResolveContextImpl(wirings, candMap, mandatory, Collections.<Resource> emptyList());

        try
        {
            resolver.resolve(rci);
            fail("Should have thrown a resolution exception as bundle A in scenario 4 cannot be resolved due to constraint violations.");
        }
        catch (ResolutionException re)
        {
            // good
        }
    }

    @Test
    public void testScenario5() throws Exception
    {
        Resolver resolver = new ResolverImpl(new Logger(Logger.LOG_DEBUG));

        Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
        Map<Requirement, List<Capability>> candMap = new HashMap<Requirement, List<Capability>>();
        List<Resource> mandatory = populateScenario5(wirings, candMap);
        ResolveContextImpl rci = new ResolveContextImpl(wirings, candMap, mandatory, Collections.<Resource> emptyList());

        try
        {
            resolver.resolve(rci);
            fail("Should have thrown a resolution exception as bundle A in scenario 5 cannot be resolved due to constraint violations.");
        }
        catch (ResolutionException re)
        {
            // good
        }
    }

    private static Resource findResource(String identity, Collection<Resource> resources)
    {
        for (Resource r : resources)
        {
            for (Capability c : r.getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE))
            {
                if (identity.equals(c.getAttributes().get(IdentityNamespace.IDENTITY_NAMESPACE)))
                    return r;
            }
        }
        return null;
    }

    private static List<Resource> populateScenario1(Map<Resource, Wiring> wirings, Map<Requirement, List<Capability>> candMap)
    {
        ResourceImpl exporter = new ResourceImpl("A");
        exporter.addCapability(new PackageCapability(exporter, "foo"));
        ResourceImpl importer = new ResourceImpl("B");
        importer.addRequirement(new PackageRequirement(importer, "foo"));
        candMap.put(importer.getRequirements(null).get(0), exporter.getCapabilities(PackageNamespace.PACKAGE_NAMESPACE));
        List<Resource> resources = new ArrayList<Resource>();
        resources.add(importer);
        return resources;
    }

    private static List<Resource> populateScenario2(Map<Resource, Wiring> wirings, Map<Requirement, List<Capability>> candMap)
    {
        List<Capability> fooCands = new ArrayList<Capability>();
        List<Capability> barCands = new ArrayList<Capability>();

        // A
        ResourceImpl a = new ResourceImpl("A");
        PackageCapability p = new PackageCapability(a, "foo");
        a.addCapability(p);
        fooCands.add(p);

        // B
        ResourceImpl b = new ResourceImpl("B");
        p = new PackageCapability(b, "foo");
        b.addCapability(p);
        fooCands.add(p);

        p = new PackageCapability(b, "bar");
        p.addDirective(PackageNamespace.CAPABILITY_USES_DIRECTIVE, "foo");
        b.addCapability(p);
        barCands.add(p);

        // C
        ResourceImpl c = new ResourceImpl("C");
        Requirement r = new PackageRequirement(c, "foo");
        c.addRequirement(r);
        candMap.put(r, fooCands);

        r = new PackageRequirement(c, "bar");
        c.addRequirement(r);
        candMap.put(r, barCands);

        // Mandatory resources
        List<Resource> resources = new ArrayList<Resource>();
        resources.add(c);
        return resources;
    }

    private static List<Resource> populateScenario3(Map<Resource, Wiring> wirings, Map<Requirement, List<Capability>> candMap)
    {
        List<Capability> dResourcesCands = new ArrayList<Capability>();
        List<Capability> eBundleDCands = new ArrayList<Capability>();
        List<Capability> eResourcesCands = new ArrayList<Capability>();

        // B
        ResourceImpl b = new ResourceImpl("B");
        PackageCapability pc = new PackageCapability(b, "resources");
        b.addCapability(pc);
        eResourcesCands.add(pc);

        // C
        ResourceImpl c = new ResourceImpl("C");
        pc = new PackageCapability(c, "resources");
        c.addCapability(pc);
        eResourcesCands.add(pc);
        dResourcesCands.add(pc);

        // D
        ResourceImpl d = new ResourceImpl("D");
        pc = new PackageCapability(d, "export");
        pc.addDirective(Namespace.CAPABILITY_USES_DIRECTIVE, "resources");
        d.addCapability(pc);

        BundleCapability bc = new BundleCapability(d, "D");
        bc.addDirective(Namespace.CAPABILITY_USES_DIRECTIVE, "resources");
        d.addCapability(bc);
        eBundleDCands.add(bc);

        Requirement r = new PackageRequirement(d, "resources");
        d.addRequirement(r);
        candMap.put(r, dResourcesCands);

        // E
        ResourceImpl e = new ResourceImpl("E");
        r = new BundleRequirement(e, "D");
        e.addRequirement(r);
        candMap.put(r, eBundleDCands);

        r = new PackageRequirement(e, "resources");
        e.addRequirement(r);
        candMap.put(r, eResourcesCands);

        // Mandatory resources
        List<Resource> resources = new ArrayList<Resource>();
        resources.add(e);
        return resources;
    }

    private static List<Resource> populateScenario4(Map<Resource, Wiring> wirings, Map<Requirement, List<Capability>> candMap)
    {
        ResourceImpl a = new ResourceImpl("A");
        a.addRequirement(new BundleRequirement(a, "B"));
        a.addRequirement(new BundleRequirement(a, "C"));

        ResourceImpl b = new ResourceImpl("B");
        b.addCapability(new BundleCapability(b, "B"));
        b.addCapability(new PackageCapability(b, "p1"));

        ResourceImpl c = new ResourceImpl("C");
        c.addRequirement(new BundleRequirement(c, "D"));
        c.addCapability(new BundleCapability(c, "C"));
        PackageCapability p2 = new PackageCapability(c, "p2");
        p2.addDirective(Namespace.CAPABILITY_USES_DIRECTIVE, "p1");
        c.addCapability(p2);

        ResourceImpl d = new ResourceImpl("D");
        d.addCapability(new BundleCapability(d, "D"));
        d.addCapability(new PackageCapability(d, "p1"));

        candMap.put(a.getRequirements(null).get(0), b.getCapabilities(BundleNamespace.BUNDLE_NAMESPACE));
        candMap.put(a.getRequirements(null).get(1), c.getCapabilities(BundleNamespace.BUNDLE_NAMESPACE));
        candMap.put(c.getRequirements(null).get(0), d.getCapabilities(BundleNamespace.BUNDLE_NAMESPACE));

        List<Resource> resources = new ArrayList<Resource>();
        resources.add(a);
        return resources;
    }

    private static List<Resource> populateScenario5(Map<Resource, Wiring> wirings, Map<Requirement, List<Capability>> candMap)
    {
        ResourceImpl x = new ResourceImpl("X");
        x.addRequirement(new BundleRequirement(x, "A"));

        ResourceImpl a = new ResourceImpl("A");
        a.addCapability(new BundleCapability(a, "A"));
        a.addRequirement(new BundleRequirement(a, "B"));
        a.addRequirement(new BundleRequirement(a, "C"));

        ResourceImpl b = new ResourceImpl("B");
        b.addCapability(new BundleCapability(b, "B"));
        b.addCapability(new PackageCapability(b, "p1"));

        ResourceImpl c = new ResourceImpl("C");
        c.addRequirement(new BundleRequirement(c, "D"));
        c.addCapability(new BundleCapability(c, "C"));
        PackageCapability p2 = new PackageCapability(c, "p2");
        p2.addDirective(Namespace.CAPABILITY_USES_DIRECTIVE, "p1");
        c.addCapability(p2);

        ResourceImpl d = new ResourceImpl("D");
        d.addCapability(new BundleCapability(d, "D"));
        d.addCapability(new PackageCapability(d, "p1"));

        candMap.put(x.getRequirements(null).get(0), a.getCapabilities(BundleNamespace.BUNDLE_NAMESPACE));
        candMap.put(a.getRequirements(null).get(0), b.getCapabilities(BundleNamespace.BUNDLE_NAMESPACE));
        candMap.put(a.getRequirements(null).get(1), c.getCapabilities(BundleNamespace.BUNDLE_NAMESPACE));
        candMap.put(c.getRequirements(null).get(0), d.getCapabilities(BundleNamespace.BUNDLE_NAMESPACE));

        List<Resource> resources = new ArrayList<Resource>();
        resources.add(x);
        return resources;
    }

}
