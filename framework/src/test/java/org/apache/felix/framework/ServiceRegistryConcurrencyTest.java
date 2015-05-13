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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import junit.framework.TestCase;

import org.apache.felix.framework.StartStopBundleTest.TestBundleActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.launch.Framework;

public class ServiceRegistryConcurrencyTest extends TestCase
{
    public static final int DELAY = 1000;

    public void testServiceRegistryConcurrently() throws Exception
    {
        Map params = new HashMap();
        params.put(Constants.FRAMEWORK_SYSTEMPACKAGES,
            "org.osgi.framework; version=1.4.0,"
            + "org.osgi.service.packageadmin; version=1.2.0,"
            + "org.osgi.service.startlevel; version=1.1.0,"
            + "org.osgi.util.tracker; version=1.3.3,"
            + "org.osgi.service.url; version=1.0.0");
        File cacheDir = File.createTempFile("felix-cache", ".dir");
        cacheDir.delete();
        cacheDir.mkdirs();
        String cache = cacheDir.getPath();
        params.put("felix.cache.profiledir", cache);
        params.put("felix.cache.dir", cache);
        params.put(Constants.FRAMEWORK_STORAGE, cache);

        String mf = "Bundle-SymbolicName: boot.test\n"
            + "Bundle-Version: 1.1.0\n"
            + "Bundle-ManifestVersion: 2\n"
            + "Import-Package: org.osgi.framework\n";
        File bundleFile = createBundle(mf, cacheDir);

        Framework f = new Felix(params);
        f.init();
        f.start();

        try
        {
            class PSF implements PrototypeServiceFactory<String>
            {
                @Override
                public String getService(Bundle bundle, ServiceRegistration<String> registration)
                {
                    try { Thread.sleep(100); } catch (Exception ex) {}
                    return "hi";
                }

                @Override
                public void ungetService(Bundle bundle, ServiceRegistration<String> registration, String service)
                {
                }
            };

            BundleContext ctx = f.getBundleContext();
            ServiceRegistration<String> reg = ctx.registerService(String.class, new PSF(), null);

            ServiceReference<String> ref = reg.getReference();
            final ServiceObjects<String> so = ctx.getServiceObjects(ref);

            final List<Exception> exceptions = new CopyOnWriteArrayList<Exception>();

            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String svc = so.getService();
                        assertEquals("hi", svc);
                        so.ungetService(svc);
                    }
                    catch (Exception e)
                    {
                        exceptions.add(e);
                    }
                }
            };

            Thread t1 = new Thread(r);
            Thread t2 = new Thread(r);
            Thread t3 = new Thread(r);
            t1.start();
            t2.start();
            t3.start();
            t1.join();
            t2.join();
            t3.join();

            assertEquals("Should not be any exception: " + exceptions, 0, exceptions.size());
        }
        finally
        {
            f.stop();
            Thread.sleep(DELAY);
            deleteDir(cacheDir);
        }
    }

    private static File createBundle(String manifest, File tempDir) throws IOException
    {
        File f = File.createTempFile("felix-bundle", ".jar", tempDir);

        Manifest mf = new Manifest(new ByteArrayInputStream(manifest.getBytes("utf-8")));
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        mf.getMainAttributes().putValue(Constants.BUNDLE_ACTIVATOR, TestBundleActivator.class.getName());
        JarOutputStream os = new JarOutputStream(new FileOutputStream(f), mf);

        String path = TestBundleActivator.class.getName().replace('.', '/') + ".class";
        os.putNextEntry(new ZipEntry(path));

        InputStream is = TestBundleActivator.class.getClassLoader().getResourceAsStream(path);
        byte[] b = new byte[is.available()];
        is.read(b);
        is.close();
        os.write(b);

        os.close();
        return f;
    }

    private static void deleteDir(File root) throws IOException
    {
        if (root.isDirectory())
        {
            for (File file : root.listFiles())
            {
                deleteDir(file);
            }
        }
        assertTrue(root.delete());
    }
}