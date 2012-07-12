package com.redhat.prototype.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.auth.TokenGrant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

@ApplicationScoped
public class TokenGrantRepository {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public Optional<TokenGrant> getTokenGrantFromAccessToken(String accessToken) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TokenGrant> criteria = cb.createQuery(TokenGrant.class);
        Root<TokenGrant> tokenGrant = criteria.from(TokenGrant.class);
        criteria.select(tokenGrant).where(cb.equal(tokenGrant.get("accessToken"), accessToken));
        TypedQuery<TokenGrant> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return Optional.of(query.getSingleResult());
        } else {
            return Optional.absent();
        }
    }

    public Optional<TokenGrant> getTokenGrantFromRefreshToken(String refreshToken) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TokenGrant> criteria = cb.createQuery(TokenGrant.class);
        Root<TokenGrant> tokenGrant = criteria.from(TokenGrant.class);
        criteria.select(tokenGrant).where(cb.equal(tokenGrant.get("refreshToken"), refreshToken));
        TypedQuery<TokenGrant> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return Optional.of(query.getSingleResult());
        } else {
            return Optional.absent();
        }
    }
}
