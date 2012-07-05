package com.redhat.prototype.oauth.request;

import com.redhat.prototype.model.auth.User;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    public String getUserId() {
        return user.getIdentifier();
    }
}
