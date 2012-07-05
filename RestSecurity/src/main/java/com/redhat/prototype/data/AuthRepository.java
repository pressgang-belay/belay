package com.redhat.prototype.data;

import com.redhat.prototype.model.auth.TokenGrant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@ApplicationScoped
public class AuthRepository {

    @Inject
    private EntityManager em;

    public TokenGrant getTokenGrantFromAccessToken(String accessToken) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TokenGrant> criteria = cb.createQuery(TokenGrant.class);
        Root<TokenGrant> tokenGrant = criteria.from(TokenGrant.class);
        criteria.select(tokenGrant).where(cb.equal(tokenGrant.get("accessToken"), accessToken));
        TypedQuery<TokenGrant> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return query.getSingleResult();
        } else {
            return null;
        }
    }
}
