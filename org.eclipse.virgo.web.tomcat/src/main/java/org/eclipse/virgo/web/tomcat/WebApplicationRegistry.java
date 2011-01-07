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

/**
 * 
 */
public interface WebApplicationRegistry {

    /**
     * Gets the name of the web application with the supplied <code>contextPath</code>.
     * @param contextPath of registered web application
     * @return name of registered web application
     */
    String getWebApplicationName(String contextPath);
}
