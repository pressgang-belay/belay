package org.jboss.pressgangccms.oauth.server.oauth.util;

import static org.jboss.pressgangccms.oauth.server.util.Common.BEARER;

/**
 * Encapsulates OAuth utility methods shared across packages.
 *
 * @author kamiller@redhat.com (Katie Miller
 */
public class OAuthUtil {
    public static String trimAccessToken(String accessToken) {
        if (accessToken.toLowerCase().startsWith(BEARER)) {
            // Remove leading header
            accessToken = accessToken.substring(BEARER.length()).trim();
        }
        return accessToken;
    }
}
