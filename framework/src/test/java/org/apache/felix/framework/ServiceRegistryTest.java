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
package org.apache.felix.framework;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import junit.framework.TestCase;

import org.apache.felix.framework.ServiceRegistry.ServiceHolder;
import org.apache.felix.framework.ServiceRegistry.UsageCount;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class ServiceRegistryTest extends TestCase
{
    /*
    public void testRegisterEventHookService()
    {
        MockControl control = MockControl.createNiceControl(Bundle.class);
        Bundle b = (Bundle) control.getMock();
        control.replay();

        MockControl controlContext = MockControl.createNiceControl(BundleContext.class);
        BundleContext c = (BundleContext) controlContext.getMock();
        controlContext.expectAndReturn(c.getBundle(), b);
        controlContext.replay();

        ServiceRegistry sr = new ServiceRegistry(new Logger(), null);
        EventHook hook = new EventHook()
        {
            public void event(ServiceEvent event, Collection contexts)
            {
            }
        };

        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        ServiceRegistration reg = sr.registerService(c.getBundle(), new String [] {EventHook.class.getName()}, hook, new Hashtable());
        assertEquals(1, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertTrue(sr.getHookRegistry().getHooks(EventHook.class).iterator().next() instanceof ServiceReference);
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(EventHook.class).iterator().next());
        assertSame(hook, ((ServiceRegistrationImpl) reg).getService());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());

        sr.unregisterService(b, reg);
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
    }

    public void testRegisterEventHookServiceFactory()
    {
        MockControl control = MockControl.createNiceControl(Bundle.class);
        Bundle b = (Bundle) control.getMock();
        control.replay();

        MockControl controlContext = MockControl.createNiceControl(BundleContext.class);
        BundleContext c = (BundleContext) controlContext.getMock();
        controlContext.expectAndReturn(c.getBundle(), b);
        controlContext.replay();

        ServiceRegistry sr = new ServiceRegistry(new Logger(), null);
        MockControl sfControl = MockControl.createNiceControl(ServiceFactory.class);
        sfControl.replay();
        ServiceFactory sf = (ServiceFactory) sfControl.getMock();

        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        ServiceRegistration reg = sr.registerService(c.getBundle(), new String [] {EventHook.class.getName()}, sf, new Hashtable());
        assertEquals(1, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(EventHook.class).iterator().next());
        assertSame(sf, ((ServiceRegistrationImpl) reg).getService());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());

        sr.unregisterService(b, reg);
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
    }

    public void testRegisterFindHookService()
    {
        MockControl control = MockControl.createNiceControl(Bundle.class);
        Bundle b = (Bundle) control.getMock();
        control.replay();

        MockControl controlContext = MockControl.createNiceControl(BundleContext.class);
        BundleContext c = (BundleContext) controlContext.getMock();
        controlContext.expectAndReturn(c.getBundle(), b);
        controlContext.replay();

        ServiceRegistry sr = new ServiceRegistry(new Logger(), null);
        FindHook hook = new FindHook()
        {
            public void find(BundleContext context, String name, String filter,
                boolean allServices, Collection references)
            {
            }
        };

        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        ServiceRegistration reg = sr.registerService(c.getBundle(), new String [] {FindHook.class.getName()}, hook, new Hashtable());
        assertEquals(1, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(FindHook.class).iterator().next());
        assertSame(hook, ((ServiceRegistrationImpl) reg).getService());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());

        sr.unregisterService(b, reg);
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
    }

    public void testRegisterFindHookServiceFactory()
    {
        MockControl control = MockControl.createNiceControl(Bundle.class);
        Bundle b = (Bundle) control.getMock();
        control.replay();

        MockControl controlContext = MockControl.createNiceControl(BundleContext.class);
        BundleContext c = (BundleContext) controlContext.getMock();
        controlContext.expectAndReturn(c.getBundle(), b);
        controlContext.replay();

        ServiceRegistry sr = new ServiceRegistry(new Logger(), null);
        MockControl sfControl = MockControl.createNiceControl(ServiceFactory.class);
        sfControl.replay();
        ServiceFactory sf = (ServiceFactory) sfControl.getMock();

        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        ServiceRegistration reg = sr.registerService(c.getBundle(), new String [] {FindHook.class.getName()}, sf, new Hashtable());
        assertEquals(1, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(FindHook.class).iterator().next());
        assertSame(sf, ((ServiceRegistrationImpl) reg).getService());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());

        sr.unregisterService(b, reg);
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
    }

    public void testRegisterListenerHookService()
    {
        MockControl control = MockControl.createNiceControl(Bundle.class);
        Bundle b = (Bundle) control.getMock();
        control.replay();

        MockControl controlContext = MockControl.createNiceControl(BundleContext.class);
        BundleContext c = (BundleContext) controlContext.getMock();
        controlContext.expectAndReturn(c.getBundle(), b);
        controlContext.replay();

        ServiceRegistry sr = new ServiceRegistry(new Logger(), null);
        ListenerHook hook = new ListenerHook()
        {
            public void added(Collection listeners)
            {
            }

            public void removed(Collection listener)
            {
            }
        };

        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        ServiceRegistration reg = sr.registerService(c.getBundle(), new String [] {ListenerHook.class.getName()}, hook, new Hashtable());
        assertEquals(1, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(ListenerHook.class).iterator().next());
        assertSame(hook, ((ServiceRegistrationImpl) reg).getService());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());

        sr.unregisterService(b, reg);
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
    }

    public void testRegisterListenerHookServiceFactory()
    {
        MockControl control = MockControl.createNiceControl(Bundle.class);
        Bundle b = (Bundle) control.getMock();
        control.replay();

        MockControl controlContext = MockControl.createNiceControl(BundleContext.class);
        BundleContext c = (BundleContext) controlContext.getMock();
        controlContext.expectAndReturn(c.getBundle(), b);
        controlContext.replay();

        ServiceRegistry sr = new ServiceRegistry(new Logger(), null);
        MockControl sfControl = MockControl.createNiceControl(ServiceFactory.class);
        sfControl.replay();
        ServiceFactory sf = (ServiceFactory) sfControl.getMock();

        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        ServiceRegistration reg = sr.registerService(c.getBundle(), new String [] {ListenerHook.class.getName()}, sf, new Hashtable());
        assertEquals(1, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(ListenerHook.class).iterator().next());
        assertSame(sf, ((ServiceRegistrationImpl) reg).getService());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());

        sr.unregisterService(b, reg);
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
    }

    public void testRegisterCombinedService()
    {
        MockControl control = MockControl.createNiceControl(Bundle.class);
        Bundle b = (Bundle) control.getMock();
        control.replay();

        MockControl controlContext = MockControl.createNiceControl(BundleContext.class);
        BundleContext c = (BundleContext) controlContext.getMock();
        controlContext.expectAndReturn(c.getBundle(), b);
        controlContext.replay();

        ServiceRegistry sr = new ServiceRegistry(new Logger(), null);
        class CombinedService implements ListenerHook, FindHook, EventHook, Runnable
        {
            public void added(Collection listeners)
            {
            }

            public void removed(Collection listener)
            {
            }

            public void find(BundleContext context, String name, String filter,
                    boolean allServices, Collection references)
            {
            }

            public void event(ServiceEvent event, Collection contexts)
            {
            }

            public void run()
            {
            }

        }
        CombinedService hook = new CombinedService();

        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        ServiceRegistration reg = sr.registerService(c.getBundle(), new String [] {
            Runnable.class.getName(),
            ListenerHook.class.getName(),
            FindHook.class.getName(),
            EventHook.class.getName()}, hook, new Hashtable());
        assertEquals(1, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(ListenerHook.class).iterator().next());
        assertSame(hook, ((ServiceRegistrationImpl) reg).getService());
        assertEquals(1, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(EventHook.class).iterator().next());
        assertSame(hook, ((ServiceRegistrationImpl) reg).getService());
        assertEquals(1, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertSame(reg.getReference(), sr.getHookRegistry().getHooks(FindHook.class).iterator().next());
        assertSame(hook, ((ServiceRegistrationImpl) reg).getService());

        sr.unregisterService(b, reg);
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Should be no hooks left after unregistration", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
    }

    public void testRegisterPlainService()
    {
        MockControl control = MockControl.createNiceControl(Bundle.class);
        Bundle b = (Bundle) control.getMock();
        control.replay();

        MockControl controlContext = MockControl.createNiceControl(BundleContext.class);
        BundleContext c = (BundleContext) controlContext.getMock();
        controlContext.expectAndReturn(c.getBundle(), b);
        controlContext.replay();

        ServiceRegistry sr = new ServiceRegistry(new Logger(), null);
        String svcObj = "hello";
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Precondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
        ServiceRegistration reg = sr.registerService(c.getBundle(), new String [] {String.class.getName()}, svcObj, new Hashtable());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Postcondition failed", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());

        sr.unregisterService(b, reg);
        assertEquals("Unregistration should have no effect", 0, sr.getHookRegistry().getHooks(EventHook.class).size());
        assertEquals("Unregistration should have no effect", 0, sr.getHookRegistry().getHooks(FindHook.class).size());
        assertEquals("Unregistration should have no effect", 0, sr.getHookRegistry().getHooks(ListenerHook.class).size());
    }
    */ // TODO re-enable

    public void testObtainUsageCount() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        @SuppressWarnings("unchecked")
        ConcurrentMap<Bundle, UsageCount[]> inUseMap = (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        assertEquals("Precondition", 0, inUseMap.size());

        Bundle b = Mockito.mock(Bundle.class);
        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);
        UsageCount uc = sr.obtainUsageCount(b, ref, null, false);
        assertEquals(1, inUseMap.size());
        assertEquals(1, inUseMap.get(b).length);
        assertSame(uc, inUseMap.get(b)[0]);
        assertSame(ref, uc.m_ref);
        assertFalse(uc.m_prototype);

        UsageCount uc2 = sr.obtainUsageCount(b, ref, null, false);
        assertSame(uc, uc2);

        ServiceReference<?> ref2 = Mockito.mock(ServiceReference.class);
        UsageCount uc3 = sr.obtainUsageCount(b, ref2, null, false);
        assertNotSame(uc3, uc2);
        assertSame(ref2, uc3.m_ref);
    }

    public void testObtainUsageCountPrototype() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        @SuppressWarnings("unchecked")
        ConcurrentMap<Bundle, UsageCount[]> inUseMap = (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        Bundle b = Mockito.mock(Bundle.class);
        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);
        UsageCount uc = sr.obtainUsageCount(b, ref, null, true);
        assertEquals(1, inUseMap.size());
        assertEquals(1, inUseMap.values().iterator().next().length);

        ServiceReference<?> ref2 = Mockito.mock(ServiceReference.class);
        UsageCount uc2 = sr.obtainUsageCount(b, ref2, null, true);
        assertEquals(1, inUseMap.size());
        assertEquals(2, inUseMap.values().iterator().next().length);
        List<UsageCount> ucl = Arrays.asList(inUseMap.get(b));
        assertTrue(ucl.contains(uc));
        assertTrue(ucl.contains(uc2));
    }

    public void testObtainUsageCountPrototypeUnknownLookup() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        @SuppressWarnings("unchecked")
        ConcurrentMap<Bundle, UsageCount[]> inUseMap = (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        Bundle b = Mockito.mock(Bundle.class);
        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);

        UsageCount uc = new UsageCount(ref, true);
        ServiceHolder sh = new ServiceHolder();
        String svc = "foobar";
        sh.m_service = svc;
        uc.m_svcHolderRef.set(sh);
        inUseMap.put(b, new UsageCount[] {uc});

        assertNull(sr.obtainUsageCount(b, Mockito.mock(ServiceReference.class), null, null));

        UsageCount uc2 = sr.obtainUsageCount(b, ref, svc, null);
        assertSame(uc, uc2);
    }

    public void testObtainUsageCountPrototypeUnknownLookup2() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        @SuppressWarnings("unchecked")
        ConcurrentMap<Bundle, UsageCount[]> inUseMap = (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        Bundle b = Mockito.mock(Bundle.class);
        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);

        UsageCount uc = new UsageCount(ref, false);
        inUseMap.put(b, new UsageCount[] {uc});

        assertNull(sr.obtainUsageCount(b, Mockito.mock(ServiceReference.class), null, null));

        UsageCount uc2 = sr.obtainUsageCount(b, ref, null, null);
        assertSame(uc, uc2);
    }

    @SuppressWarnings("unchecked")
    public void testObtainUsageCountRetryNeeded1() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        final Bundle b = Mockito.mock(Bundle.class);

        final ConcurrentMap<Bundle, UsageCount[]> orgInUseMap =
            (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        ConcurrentMap<Bundle, UsageCount[]> inUseMap =
            Mockito.mock(ConcurrentMap.class, AdditionalAnswers.delegatesTo(orgInUseMap));
        Mockito.doAnswer(new Answer<UsageCount[]>()
            {
                @Override
                public UsageCount[] answer(InvocationOnMock invocation) throws Throwable
                {
                    // This mimicks another thread putting another UsageCount in concurrently
                    // The putIfAbsent() will fail and it has to retry
                    UsageCount uc = new UsageCount(Mockito.mock(ServiceReference.class), false);
                    UsageCount[] uca = new UsageCount[] {uc};
                    orgInUseMap.put(b, uca);
                    return uca;
                }
            }).when(inUseMap).putIfAbsent(Mockito.any(Bundle.class), Mockito.any(UsageCount[].class));
        setPrivateField(sr, "m_inUseMap", inUseMap);

        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);

        assertEquals(0, orgInUseMap.size());
        UsageCount uc = sr.obtainUsageCount(b, ref, null, false);
        assertEquals(1, orgInUseMap.size());
        assertEquals(2, orgInUseMap.get(b).length);
        assertSame(ref, uc.m_ref);
        assertFalse(uc.m_prototype);
        List<UsageCount> l = new ArrayList<UsageCount>(Arrays.asList(orgInUseMap.get(b)));
        l.remove(uc);
        assertEquals("There should be one UsageCount left", 1, l.size());
        assertNotSame(ref, l.get(0).m_ref);
    }

    @SuppressWarnings("unchecked")
    public void testObtainUsageCountRetryNeeded2() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        final Bundle b = Mockito.mock(Bundle.class);

        final ConcurrentMap<Bundle, UsageCount[]> orgInUseMap =
            (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");
        orgInUseMap.put(b, new UsageCount[] {new UsageCount(Mockito.mock(ServiceReference.class), false)});

        ConcurrentMap<Bundle, UsageCount[]> inUseMap =
            Mockito.mock(ConcurrentMap.class, AdditionalAnswers.delegatesTo(orgInUseMap));
        Mockito.doAnswer(new Answer<Boolean>()
            {
                @Override
                public Boolean answer(InvocationOnMock invocation) throws Throwable
                {
                    orgInUseMap.remove(b);
                    return false;
                }
            }).when(inUseMap).replace(Mockito.any(Bundle.class),
                    Mockito.any(UsageCount[].class), Mockito.any(UsageCount[].class));
        setPrivateField(sr, "m_inUseMap", inUseMap);

        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);

        assertEquals("Precondition", 1, inUseMap.size());
        assertEquals("Precondition", 1, inUseMap.values().iterator().next().length);
        assertNotSame("Precondition", ref, inUseMap.get(b)[0].m_ref);
        UsageCount uc = sr.obtainUsageCount(b, ref, null, false);
        assertEquals(1, inUseMap.size());
        assertEquals(1, inUseMap.values().iterator().next().length);
        assertSame("The old usage count should have been removed by the mock and this one should have been added",
                ref, inUseMap.get(b)[0].m_ref);
    }

    public void testFlushUsageCount() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        @SuppressWarnings("unchecked")
        ConcurrentMap<Bundle, UsageCount[]> inUseMap = (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        Bundle b = Mockito.mock(Bundle.class);

        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);
        UsageCount uc = new UsageCount(ref, false);
        ServiceReference<?> ref2 = Mockito.mock(ServiceReference.class);
        UsageCount uc2 = new UsageCount(ref2, true);

        inUseMap.put(b, new UsageCount[] {uc, uc2});

        assertEquals("Precondition", 1, inUseMap.size());
        assertEquals("Precondition", 2, inUseMap.values().iterator().next().length);

        sr.flushUsageCount(b, ref, uc);
        assertEquals(1, inUseMap.size());
        assertEquals(1, inUseMap.values().iterator().next().length);
        assertSame(uc2, inUseMap.values().iterator().next()[0]);

        sr.flushUsageCount(b, ref2, uc2);
        assertEquals(0, inUseMap.size());
    }

    public void testFlushUsageCountNullRef() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        @SuppressWarnings("unchecked")
        ConcurrentMap<Bundle, UsageCount[]> inUseMap = (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        Bundle b = Mockito.mock(Bundle.class);
        Bundle b2 = Mockito.mock(Bundle.class);

        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);
        UsageCount uc = new UsageCount(ref, false);
        ServiceReference<?> ref2 = Mockito.mock(ServiceReference.class);
        UsageCount uc2 = new UsageCount(ref2, true);
        ServiceReference<?> ref3 = Mockito.mock(ServiceReference.class);
        UsageCount uc3 = new UsageCount(ref3, true);

        inUseMap.put(b, new UsageCount[] {uc2, uc});
        inUseMap.put(b2, new UsageCount[] {uc3});

        assertEquals("Precondition", 2, inUseMap.size());

        sr.flushUsageCount(b, null, uc);
        assertEquals(2, inUseMap.size());

        sr.flushUsageCount(b, null, uc2);
        assertEquals(1, inUseMap.size());
    }

    public void testFlushUsageCountAlienObject() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        @SuppressWarnings("unchecked")
        ConcurrentMap<Bundle, UsageCount[]> inUseMap = (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        Bundle b = Mockito.mock(Bundle.class);

        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);
        UsageCount uc = new UsageCount(ref, false);

        inUseMap.put(b, new UsageCount[] {uc});
        assertEquals("Precondition", 1, inUseMap.size());
        assertEquals("Precondition", 1, inUseMap.values().iterator().next().length);

        UsageCount uc2 = new UsageCount(Mockito.mock(ServiceReference.class), false);
        sr.flushUsageCount(b, ref, uc2);
        assertEquals("Should be no changes", 1, inUseMap.size());
        assertEquals("Should be no changes", 1, inUseMap.values().iterator().next().length);
    }

    public void testFlushUsageCountNull() throws Exception
    {
        ServiceRegistry sr = new ServiceRegistry(null, null);

        @SuppressWarnings("unchecked")
        ConcurrentMap<Bundle, UsageCount[]> inUseMap = (ConcurrentMap<Bundle, UsageCount[]>) getPrivateField(sr, "m_inUseMap");

        Bundle b = Mockito.mock(Bundle.class);
        Bundle b2 = Mockito.mock(Bundle.class);

        ServiceReference<?> ref = Mockito.mock(ServiceReference.class);
        UsageCount uc = new UsageCount(ref, false);
        ServiceReference<?> ref2 = Mockito.mock(ServiceReference.class);
        UsageCount uc2 = new UsageCount(ref2, true);
        ServiceReference<?> ref3 = Mockito.mock(ServiceReference.class);
        UsageCount uc3 = new UsageCount(ref3, true);

        inUseMap.put(b, new UsageCount[] {uc2, uc});
        inUseMap.put(b2, new UsageCount[] {uc3});

        assertEquals("Precondition", 2, inUseMap.size());

        sr.flushUsageCount(b, ref, null);
        assertEquals(2, inUseMap.size());

        sr.flushUsageCount(b, ref2, null);
        assertEquals(1, inUseMap.size());

    }

    public void testFlushUsageCountRetry()
    {
        fail("TODO");
    }

    private Object getPrivateField(Object obj, String fieldName) throws NoSuchFieldException,
            IllegalAccessException
    {
        Field f = ServiceRegistry.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }

    private void setPrivateField(ServiceRegistry obj, String fieldName, Object val) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field f = ServiceRegistry.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(obj, val);
    }
}
