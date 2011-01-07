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

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import org.eclipse.virgo.medic.eventlog.EventLogger;

final class WebApplicationEventLogger implements EventHandler {
            
    static final String EVENT_NAME_PREFIX = "org/osgi/service/web/";

    /**
     * The {@link EventAdmin} topic for web bundle <code>DEPLOYING</code> events.
     */
    static final String EVENT_DEPLOYING = EVENT_NAME_PREFIX + "DEPLOYING";

    /**
     * The {@link EventAdmin} topic for web bundle <code>DEPLOYED</code> events.
     */
    static final String EVENT_DEPLOYED = EVENT_NAME_PREFIX + "DEPLOYED";

    /**
     * The {@link EventAdmin} topic for web bundle <code>UNDEPLOYING</code> events.
     */
    static final String EVENT_UNDEPLOYING = EVENT_NAME_PREFIX + "UNDEPLOYING";

    /**
     * The {@link EventAdmin} topic for web bundle <code>UNDEPLOYED</code> events.
     */
    static final String EVENT_UNDEPLOYED = EVENT_NAME_PREFIX + "UNDEPLOYED";

    /**
     * The {@link EventAdmin} topic for web bundle <code>FAILED</code> events.
     */
    static final String EVENT_FAILED = EVENT_NAME_PREFIX + "FAILED";

    /**
     * The {@link org.osgi.service.event.Event Event} property for the web application bundle's context path.
     */
    static final String EVENT_PROPERTY_CONTEXT_PATH = "context.path";

    /**
     * The {@link org.osgi.service.event.Event Event} property for the web application bundle's version.
     */
    static final String EVENT_PROPERTY_BUNDLE_VERSION = "bundle.version";
	
    
    
    
    
    
	
	
    private static final String EMPTY_CONTEXT_PATH = "";

    private static final String ROOT_CONTEXT_PATH = "/";

    private final EventLogger eventLogger;

    private static final Map<String, WebLogEvents> MAPPINGS;    

    static {
        Map<String, WebLogEvents> mappings = new HashMap<String, WebLogEvents>();
        mappings.put(EVENT_DEPLOYING, WebLogEvents.STARTING_WEB_BUNDLE);
        mappings.put(EVENT_DEPLOYED, WebLogEvents.STARTED_WEB_BUNDLE);
        mappings.put(EVENT_UNDEPLOYING, WebLogEvents.STOPPING_WEB_BUNDLE);
        mappings.put(EVENT_UNDEPLOYED, WebLogEvents.STOPPED_WEB_BUNDLE);
        MAPPINGS = mappings;
    }

    public WebApplicationEventLogger(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    public void handleEvent(Event event) {
        String topic = event.getTopic();
        if (EVENT_FAILED.equals(topic)) {
            logFailure(event);
        } else {
            WebLogEvents logEvent = MAPPINGS.get(topic);
            if (logEvent != null) {
                this.eventLogger.log(logEvent, bundleName(event), bundleVersion(event), contextPathFor(event));
            }
        }
    }

    private void logFailure(Event event) {
        Exception ex = (Exception) event.getProperty(EventConstants.EXCEPTION);
        if (ex.getClass().getName().contains("ContextPathExistsException")) {
            this.eventLogger.log(WebLogEvents.WEB_BUNDLE_FAILED_CONTEXT_PATH_USED, bundleName(event), bundleVersion(event), contextPathFor(event));
        } else {
            this.eventLogger.log(WebLogEvents.WEB_BUNDLE_FAILED, bundleName(event), bundleVersion(event));
        }
    }

    private String contextPathFor(Event event) {
        String contextPath = (String) event.getProperty(EVENT_PROPERTY_CONTEXT_PATH);
        if(EMPTY_CONTEXT_PATH.equals(contextPath)) {
            return ROOT_CONTEXT_PATH;
        }
        return contextPath;
    }

    private String bundleName(Event event) {
        return (String) event.getProperty(EventConstants.BUNDLE_SYMBOLICNAME);
    }
    
    private Version bundleVersion(Event event) {
        return (Version) event.getProperty(EVENT_PROPERTY_BUNDLE_VERSION);
    }
}
