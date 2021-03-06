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

package org.eclipse.virgo.web.core.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.gemini.web.core.InstallationOptions;
import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.internal.StandardBundleManifest;
import org.junit.Test;
import org.osgi.framework.Version;

public class WebBundleTransformerTests {
    
    private StubWebContainer webContainer = new StubWebContainer();
    
    private StubWebApplicationRegistry applicationRegistry = new StubWebApplicationRegistry();
    
    private WebBundleManifestTransformer manifestTransformer = createMock(WebBundleManifestTransformer.class);
    
    private WebDeploymentEnvironment environment = new WebDeploymentEnvironment(webContainer, applicationRegistry, manifestTransformer);
    
    private WebBundleTransformer webBundleTransformer = new WebBundleTransformer(environment);
    
    @Test
    public void transformation() throws IOException, DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.getBundleSymbolicName().setSymbolicName("foo.war");
               
        File sourceFile = new File("/bar.war");
        URI sourceUri = sourceFile.toURI();
        
        BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, sourceFile, bundleManifest);
        
        manifestTransformer.transform(eq(bundleManifest), eq(sourceUri.toURL()), isA(InstallationOptions.class), eq(true));
        
        replay(manifestTransformer);
        
        Tree<InstallArtifact> installTree = new ThreadSafeArrayListTree<InstallArtifact>(installArtifact);
        webBundleTransformer.transform(installTree, null);
        
        verify(manifestTransformer);
        
        assertManifestTransformations(bundleManifest, "web-bundle");
    }
    
    @Test
    public void nonBundleInstallArtifactTransformation() throws DeploymentException {                
        InstallArtifact installArtifact = TestUtils.createInstallArtifact("foo", new Version(1,0,0), new File("location"), URI.create("file:/bar"));
        
        replay(manifestTransformer);
        
        Tree<InstallArtifact> installTree = new ThreadSafeArrayListTree<InstallArtifact>(installArtifact);
        webBundleTransformer.transform(installTree, null);
        
        verify(manifestTransformer);
    }
    
    @Test
    public void nonWarBundleInstallArtifactTransformation() throws DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.getBundleSymbolicName().setSymbolicName("foo.jar");
        
        URI sourceUri = URI.create("file:/bar");
        BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, new File("location"), bundleManifest);
        
        replay(manifestTransformer);
        
        Tree<InstallArtifact> installTree = new ThreadSafeArrayListTree<InstallArtifact>(installArtifact);
        webBundleTransformer.transform(installTree, null);
        
        verify(manifestTransformer);
        
        assertManifestTransformations(bundleManifest, null);
    }
    
    @Test
    public void bundleWithWebContextPathTransformation() throws DeploymentException, IOException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.setHeader("Web-ContextPath", "/foo");
        bundleManifest.getBundleSymbolicName().setSymbolicName("foo.jar");
        
        File sourceFile = new File("/foo.jar");
        URI sourceUri = sourceFile.toURI();
        BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, sourceFile, bundleManifest);
        
        manifestTransformer.transform(eq(bundleManifest), eq(sourceUri.toURL()), isA(InstallationOptions.class), eq(true));
        
        replay(manifestTransformer);
        
        Tree<InstallArtifact> installTree = new ThreadSafeArrayListTree<InstallArtifact>(installArtifact);
        webBundleTransformer.transform(installTree, null);
        
        verify(manifestTransformer);
        
        assertManifestTransformations(bundleManifest, "web-bundle");
    }
    
    @Test
    public void ignoreWebBundlesWithExistingModuleType() throws DeploymentException {
        BundleManifest bundleManifest = new StandardBundleManifest(null);
        bundleManifest.getBundleSymbolicName().setSymbolicName("foo.war");
        bundleManifest.setModuleType("web-slice");
        
        URI sourceUri = URI.create("file:/bar");
        BundleInstallArtifact installArtifact = TestUtils.createBundleInstallArtifact(sourceUri, new File("location"), bundleManifest);
        
        replay(manifestTransformer);
        
        Tree<InstallArtifact> installTree = new ThreadSafeArrayListTree<InstallArtifact>(installArtifact);
        webBundleTransformer.transform(installTree, null);
        
        verify(manifestTransformer);
        
        assertManifestTransformations(bundleManifest, "web-slice");
    }
    
    public void assertManifestTransformations(BundleManifest bundleManifest, String expectedModuleType) {
        assertEquals(expectedModuleType, bundleManifest.getHeader("Module-Type"));
    }
}
