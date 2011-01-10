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

package org.eclipse.virgo.web.tomcat.internal;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.gemini.web.core.WebApplication;
import org.eclipse.gemini.web.core.WebContainer;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.web.tomcat.WebApplicationRegistry;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final public class StandardWebApplicationRegistry extends InstallArtifactLifecycleListenerSupport implements WebApplicationRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String EMPTY_CONTEXT_PATH = "";

    private static final String ROOT_CONTEXT_PATH = "/";
    
    private static final String MANIFEST_HEADER_WEB_CONTEXT_PATH = "Web-ContextPath";

    private final WebContainer webContainer;
    
    private final Map<String, String> deployedWebAppNames = new ConcurrentHashMap<String, String>();

    private final Map<InstallArtifact, WebApplication> webApplications = new ConcurrentHashMap<InstallArtifact, WebApplication>();
    
    public StandardWebApplicationRegistry(WebContainer webContainer){
    	this.webContainer = webContainer;
    }
    
    /**
     *
     */
    public String getWebApplicationName(String contextPath) {
        return this.deployedWebAppNames.get(contextPath);
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void onStarting(InstallArtifact installArtifact) throws DeploymentException {
        if (isWebBundle(installArtifact)) {
            try {
            	WebApplication webApplication = this.webContainer.createWebApplication(((BundleInstallArtifact)installArtifact).getBundle());
                this.webApplications.put(installArtifact, webApplication);
            } catch (BundleException be) {
                throw new DeploymentException("Failed to create new web application for web bundle '" + installArtifact + "'.", be);
            }
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void onStarted(InstallArtifact installArtifact) throws DeploymentException {
        WebApplication webApplication = this.webApplications.get(installArtifact);
        if (webApplication != null) {
        	String contextPath = this.getContextPath(webApplication);
        	String applicationName = getApplicationName(installArtifact);
            logger.debug("Registering web application with context path [{}] and application name [{}].", contextPath, applicationName);
        	this.deployedWebAppNames.put(contextPath, applicationName);
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void onStopping(InstallArtifact installArtifact) {
        WebApplication webApplication = this.webApplications.remove(installArtifact);
        if (webApplication != null) {
        	String contextPath = this.getContextPath(webApplication);
        	logger.debug("Unregistering web application with context path [{}].", contextPath);
        	this.deployedWebAppNames.remove(contextPath);
        }
    }
    
    private static boolean isWebBundle(InstallArtifact installArtifact) throws DeploymentException {
        if (installArtifact instanceof BundleInstallArtifact) {
            try {
                BundleManifest bundleManifest = ((BundleInstallArtifact)installArtifact).getBundleManifest();
                return null != bundleManifest.getHeader(MANIFEST_HEADER_WEB_CONTEXT_PATH);
            } catch (IOException ioe) {
                throw new DeploymentException("Failed to get bundle manifest from '" + installArtifact + "'", ioe);
            }
        }
        return false;
    }
    
    private String getApplicationName(InstallArtifact installArtifact) {
        return installArtifact.getName() + "-" + installArtifact.getVersion();
    }
    
    private String getContextPath(WebApplication webApplication) {
        String contextPath = webApplication.getServletContext().getContextPath();
        if(EMPTY_CONTEXT_PATH.equals(contextPath)) {
            return ROOT_CONTEXT_PATH;
        }
        return contextPath;
    }
    
}
