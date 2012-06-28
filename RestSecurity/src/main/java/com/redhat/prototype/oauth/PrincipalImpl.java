package com.redhat.prototype.oauth;

import java.security.Principal;

public class PrincipalImpl implements Principal {

    private Principal currentUserPrincipal;
    private String userId;
    private String userName;

    public PrincipalImpl(String userId, Principal currentUserPrincipal) {
        this.userId = userId;
        this.userName = "user"; //TODO get name from repo, check against current principal as specified in request, if supplied
        this.currentUserPrincipal = currentUserPrincipal;
    }

    @Override
    public String getName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }
}
