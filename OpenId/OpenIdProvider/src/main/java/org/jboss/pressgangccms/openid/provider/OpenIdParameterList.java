package org.jboss.pressgangccms.openid.provider;

import org.openid4java.message.ParameterList;

import java.util.Map;

/**
 * This class is based on org.picketlink.identity.federation.api.openid.provider.OpenIDParameterList by Anil Saldhana,
 * It has been modified from the original, in 2012.
 *
 * <p/>
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 * <p/>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * <p/>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */

public class OpenIdParameterList extends ParameterList {

    public OpenIdParameterList() {
        super();
    }

    @SuppressWarnings("unchecked")
    public OpenIdParameterList(Map parameterMap) {
        super(parameterMap);
    }
}
