package com.redhat.prototype.oauth;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;
import org.apache.amber.oauth2.rsfilter.OAuthRSProvider;

import javax.servlet.http.HttpServletRequest;

public class OAuthRSProviderImpl implements OAuthRSProvider {

    @Override
    public OAuthDecision validateRequest(String realm, String token, HttpServletRequest req) throws OAuthProblemException {
        return new OAuthDecisionImpl(realm, token, req);
    }
}
