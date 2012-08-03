package org.jboss.pressgangccms.oauth2.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Set;

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
    private String username;
    private Identity primaryIdentity;
    private Set<Identity> userIdentities;

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

    // @NotNull
    @OneToOne
    @JoinColumn(name = "OPENID_IDENTITY_IDENTITY_ID")
    public Identity getPrimaryIdentity() {
        return primaryIdentity;
    }

    @OneToMany(mappedBy = "user")
    public Set<Identity> getUserIdentities() {
        return userIdentities;
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
        return new ToStringBuilder(this).append("primaryIdentity", primaryIdentity)
                .append("username", username)
                .append("userIdentities", userIdentities)
                .toString();
    }
}
