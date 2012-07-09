package com.redhat.prototype.data;

import com.redhat.prototype.model.auth.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Set;

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
            return null; //TODO implement/import Maybe/Option type to replace these nulls?
        }
    }

    public TokenGrant getTokenGrantFromRefreshToken(String refreshToken) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TokenGrant> criteria = cb.createQuery(TokenGrant.class);
        Root<TokenGrant> tokenGrant = criteria.from(TokenGrant.class);
        criteria.select(tokenGrant).where(cb.equal(tokenGrant.get("refreshToken"), refreshToken));
        TypedQuery<TokenGrant> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return query.getSingleResult();
        } else {
            return null; //TODO implement/import Maybe/Option type to replace these nulls?
        }
    }

    public User getUserFromIdentifier(String identifier) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        criteria.select(user).where(cb.equal(user.get("userIdentifier"), identifier));
        TypedQuery<User> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return query.getSingleResult();
        } else {
            return null;
        }
    }

    public OpenIdProvider getOpenIdProviderFromUrl(String providerUrl) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OpenIdProvider> criteria = cb.createQuery(OpenIdProvider.class);
        Root<OpenIdProvider> openIdProvider = criteria.from(OpenIdProvider.class);
        criteria.select(openIdProvider).where(cb.equal(openIdProvider.get("providerUrl"), providerUrl));
        TypedQuery<OpenIdProvider> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return query.getSingleResult();
        } else {
            return null;
        }
    }

    public Scope getDefaultScope() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Scope> criteria = cb.createQuery(Scope.class);
        Root<Scope> scope = criteria.from(Scope.class);
        criteria.select(scope).where(cb.equal(scope.get("scopeName"), "default"));
        return em.createQuery(criteria).getSingleResult();
    }

    public Scope getScopeFromName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Scope> criteria = cb.createQuery(Scope.class);
        Root<Scope> scope = criteria.from(Scope.class);
        criteria.select(scope).where(cb.equal(scope.get("scopeName"), name));
        TypedQuery<Scope> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return query.getSingleResult();
        } else {
            return null;
        }
    }

    public ClientApplication getClientApplicationFromClientIdentifier(String clientIdentifier) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientApplication> criteria = cb.createQuery(ClientApplication.class);
        Root<ClientApplication> clientApplication = criteria.from(ClientApplication.class);
        criteria.select(clientApplication).where(cb.equal(clientApplication.get("clientIdentifier"), clientIdentifier));
        TypedQuery<ClientApplication> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return query.getSingleResult();
        } else {
            return null;
        }
    }
}
