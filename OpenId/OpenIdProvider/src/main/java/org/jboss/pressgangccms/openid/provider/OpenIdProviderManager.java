package org.jboss.pressgangccms.openid.provider;


import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;

/**
 * This class is based on org.picketlink.identity.federation.api.openid.provider.OpenIDProviderManager by Anil Saldhana.
 * It has been modified from the original, in 2012.
 *
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OpenIdProviderManager {

    private static ServerManager serverManager;

    static {
        serverManager = new ServerManager();
        initialize();
    }

    public static void initialize() {
        serverManager.setEnforceRpId(true);
        serverManager.setSharedAssociations(new InMemoryServerAssociationStore());
        serverManager.setPrivateAssociations(new InMemoryServerAssociationStore());
    }

    public String getEndPoint() {
        return serverManager.getOPEndpointUrl();
    }

    public void setEndPoint(String url) {
        serverManager.setOPEndpointUrl(url);
    }

    public OpenIdMessage processAuthenticationRequest(ParameterList requestParams,
                                                      String userSelId,
                                                      String userSelClaimed,
                                                      boolean authenticatedAndApproved) {
        Message authMessage = serverManager.authResponse(requestParams,
                userSelId, userSelClaimed, authenticatedAndApproved);

        return new OpenIdMessage(authMessage);
    }

    public OpenIdMessage processAssociationRequest(ParameterList requestParams) {
        return new OpenIdMessage(serverManager.associationResponse(requestParams));
    }

    public OpenIdMessage verify(ParameterList requestParams) {
        return new OpenIdMessage(serverManager.verify(requestParams));
    }

    public OpenIdMessage getDirectError(String msg) {
        return new OpenIdMessage(DirectError.createDirectError(msg));
    }

    /**
     * Message class to model OpenID responses.
     */
    public static class OpenIdMessage {
        private Message message;

        OpenIdMessage(Message message) {
            this.message = message;
        }

        public boolean isSuccessful() {
            return message instanceof AuthSuccess;
        }

        public String getDestinationURL(boolean httpget) {
            return message.getDestinationUrl(httpget);
        }

        public String getResponseText() {
            return message.keyValueFormEncoding();
        }
    }
}
