package com.redhat.prototype.service;

import com.redhat.prototype.data.AuthRepository;
import com.redhat.prototype.model.auth.TokenGrant;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class AuthService {

    @Inject
    private AuthRepository authRepository;

    public TokenGrant getTokenGrant(String accessToken) {
        return authRepository.getTokenGrantFromAccessToken(accessToken);
    }
}
