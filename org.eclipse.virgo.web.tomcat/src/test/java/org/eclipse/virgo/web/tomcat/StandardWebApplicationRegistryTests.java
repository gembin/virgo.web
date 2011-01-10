/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.tomcat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URI;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardBundleManifest;
import org.eclipse.virgo.web.tomcat.internal.StandardWebApplicationRegistry;

public class StandardWebApplicationRegistryTests {
    
    private StubWebContainer webContainer = new StubWebContainer();

	private BundleContext stubBundleContext = new StubBundleContext();
    
    private StandardWebApplicationRegistry webAppRegistry = new StandardWebApplicationRegistry(webContainer, stubBundleContext);
    
    @Test
    public void standardLifecycleForNonBundleInstallArtifact() throws DeploymentException {
        InstallArtifact installArtifact = TestUtils.createInstallArtifact("foo", new Version(1,0,0), new File("location"), URI.create("file:/bar"));
        
        this.webAppRegistry.onStarting(installArtifact);
        this.webAppRegistry.onStarted(installArtifact);
        this.webAppRegistry.onStopping(installArtifact);
        
        //this.listener.assertStateUnchanged();
    }
    
    @Test
    public void testEmptyContextPathReplacement() throws DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setBundleVersion(new Version(1, 0, 0));
        bundleManifest.getBundleSymbolicName().setSymbolicName("foobar");
        bundleManifest.setHeader("Web-ContextPath", "");
        
        BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(URI.create("file:/testLocation"), new File("location"), bundleManifest);        
        
        StubWebApplication webApplication = new StubWebApplication("");
        this.webContainer.addWebApplication(installArtifact.getBundle(), webApplication);
        
        this.webAppRegistry.onStarting(installArtifact);
        
        this.webAppRegistry.onStarted(installArtifact);
        
        assertEquals("foobar-1.0.0", this.webAppRegistry.getWebApplicationName("/"));    

        assertEquals("/", installArtifact.getProperty("org.eclipse.virgo.web.contextPath"));  
        
        this.webAppRegistry.onStopping(installArtifact);
        
        assertNull(this.webAppRegistry.getWebApplicationName("/"));
        assertFalse(webApplication.isStarted());
    }
    
    @Test
    public void standardLifecycleForNonWebBundleBundleInstallArtifact() throws DeploymentException {
        InstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(URI.create("file:/bar"), new File("location"), new StandardBundleManifest(null));
        
        this.webAppRegistry.onStarting(installArtifact);
        this.webAppRegistry.onStarted(installArtifact);
        this.webAppRegistry.onStopping(installArtifact);
        
        //this.listener.assertStateUnchanged();
    }
    
    @Test
    public void standardLifecycle() throws DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setBundleVersion(new Version(1, 0, 0));
        bundleManifest.getBundleSymbolicName().setSymbolicName("foo");
        bundleManifest.setHeader("Web-ContextPath", "/bar");
        
        BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(URI.create("file:/bar"), new File("location"), bundleManifest);
        
        StubWebApplication webApplication = new StubWebApplication("/bar");
        this.webContainer.addWebApplication(installArtifact.getBundle(), webApplication);
        
        this.webAppRegistry.onStarting(installArtifact);
        
        this.webAppRegistry.onStarted(installArtifact);
        
        assertEquals("foo-1.0.0", this.webAppRegistry.getWebApplicationName("/bar"));
        assertEquals("/bar", installArtifact.getProperty("org.eclipse.virgo.web.contextPath"));           
        
        this.webAppRegistry.onStopping(installArtifact);
        
        assertNull(this.webAppRegistry.getWebApplicationName("/bar"));
        assertFalse(webApplication.isStarted());              
    }
    
    
}
