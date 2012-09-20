package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Set;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;

/**
 * Persistence logic for a group of Users that all represent the same end user.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="OPENID_USER", uniqueConstraints = {@UniqueConstraint(columnNames = { "OPENID_IDENTITY_IDENTITY_ID" }),
                                                @UniqueConstraint(columnNames = { "USERNAME" })})
public class User implements Serializable {
    private static final long serialVersionUID = 6622976631392573530L;

    private BigInteger userId;
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
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    public BigInteger getUserId() {
        return userId;
    }

    @Column(name = "USERNAME")
    public String getUsername() {
        return username;
    }

    @OneToOne
    @JoinColumn(name = "OPENID_IDENTITY_IDENTITY_ID")
    public Identity getPrimaryIdentity() {
        return primaryIdentity;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
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
    @JoinTable(name = "OPENID_USER_SCOPE", joinColumns = { @JoinColumn(name = "USER_ID") },
            inverseJoinColumns = { @JoinColumn(name = "SCOPE_ID") })
    public Set<Scope> getUserScopes() {
        return userScopes;
    }

    @OneToMany(mappedBy = "approver", fetch = FetchType.EAGER)
    public Set<ClientApproval> getClientApprovals() {
        return clientApprovals;
    }

    public void setUserId(BigInteger userId) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User that = (User) o;

        return new EqualsBuilder()
                .append(primaryIdentity, that.getPrimaryIdentity())
                .isEquals();
    }

    @Override
    public int hashCode() {
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
                .append("tokenGrants", tokenGrants)
                .append("codeGrants", codeGrants)
                .append("userScopes", userScopes)
                .append("clientApprovals", clientApprovals)
                .toString();
    }
}
