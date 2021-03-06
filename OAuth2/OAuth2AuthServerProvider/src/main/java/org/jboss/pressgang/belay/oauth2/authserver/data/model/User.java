package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.pressgang.belay.oauth2.authserver.data.constraint.PrimaryIdentityAssociated;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Persistence logic for a group of Users that all represent the same end user.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@PrimaryIdentityAssociated
@Table(name = "OPENID_USER")
public class User implements Serializable {
    private static final long serialVersionUID = 6622976631392573530L;

    private long userId;
    private Identity primaryIdentity;
    private String username;
    private Set<Identity> userIdentities = newHashSet();
    private Set<TokenGrant> tokenGrants = newHashSet();
    private Set<CodeGrant> codeGrants = newHashSet();
    private Set<Scope> userScopes = newHashSet();
    private Set<ClientApproval> clientApprovals = newHashSet();

    public User() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    public long getUserId() {
        return userId;
    }

    @Column(name = "USERNAME", unique = true)
    public String getUsername() {
        return username;
    }

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "PRIMARY_IDENTITY_ID", unique = true)
    public Identity getPrimaryIdentity() {
        return primaryIdentity;
    }

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    public Set<Identity> getUserIdentities() {
        return userIdentities;
    }

    @OneToMany(mappedBy = "grantUser", fetch = FetchType.EAGER)
    public Set<TokenGrant> getTokenGrants() {
        return tokenGrants;
    }

    @OneToMany(mappedBy = "grantUser", fetch = FetchType.EAGER)
    public Set<CodeGrant> getCodeGrants() {
        return codeGrants;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "OPENID_USER_SCOPE", joinColumns = {@JoinColumn(name = "USER_ID")},
            inverseJoinColumns = {@JoinColumn(name = "SCOPE_ID")})
    public Set<Scope> getUserScopes() {
        return userScopes;
    }

    @OneToMany(mappedBy = "approver", fetch = FetchType.EAGER)
    public Set<ClientApproval> getClientApprovals() {
        return clientApprovals;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPrimaryIdentity(Identity primaryIdentity) {
        this.primaryIdentity = primaryIdentity;
    }

    public void setUserIdentities(Set<Identity> userIdentities) {
        this.userIdentities = userIdentities;
    }

    public void setTokenGrants(Set<TokenGrant> tokenGrants) {
        this.tokenGrants = tokenGrants;
    }

    public void setCodeGrants(Set<CodeGrant> codeGrants) {
        this.codeGrants = codeGrants;
    }

    public void setUserScopes(Set<Scope> userScopes) {
        this.userScopes = userScopes;
    }

    public void setClientApprovals(Set<ClientApproval> clientApprovals) {
        this.clientApprovals = clientApprovals;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User that = (User) o;

        return new EqualsBuilder()
                .append(primaryIdentity, that.getPrimaryIdentity())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(primaryIdentity)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("primaryIdentity", primaryIdentity)
                .append("username", username)
                .append("userIdentities", userIdentities)
                .append("userScopes", userScopes)
                .toString();
    }
}
