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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.gemini.web.core.WebApplication;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListenerSupport;
import org.eclipse.virgo.web.tomcat.WebApplicationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final public class StandardWebApplicationRegistry extends InstallArtifactLifecycleListenerSupport implements WebApplicationRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String EMPTY_CONTEXT_PATH = "";

    private static final String ROOT_CONTEXT_PATH = "/";
    
    private final Map<String, String> deployedWebAppNames = new ConcurrentHashMap<String, String>();

    private final Map<InstallArtifact, WebApplication> webApplications = new ConcurrentHashMap<InstallArtifact, WebApplication>();
    
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
    public void onStarted(InstallArtifact installArtifact) throws DeploymentException {
        WebApplication webApplication = this.webApplications.get(installArtifact);
        if (webApplication != null) {
        	String contextPath = getContextPath(webApplication);
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
        	String contextPath = getContextPath(webApplication);
        	logger.debug("Unregistering web application with context path [{}].", contextPath);
        	this.deployedWebAppNames.remove(contextPath);
        }
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
