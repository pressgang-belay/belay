package org.jboss.pressgangccms.oauth.server.data.model.auth;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Set;

/**
 * Persistence logic for a group of Users that all represent the same end user.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="USER_GROUP", uniqueConstraints = @UniqueConstraint(columnNames = { "OPENID_USER_USER_ID" }))
public class UserGroup implements Serializable {
    private static final long serialVersionUID = 6622976631392573530L;

    private BigInteger groupId;
    private User primaryUser;
    private Set<User> groupUsers;

    public UserGroup() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "USER_GROUP_ID")
    public BigInteger getGroupId() {
        return groupId;
    }

    // @NotNull
    @OneToOne
    @JoinColumn(name = "OPENID_USER_USER_ID")
    public User getPrimaryUser() {
        return primaryUser;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userGroup")
    public Set<User> getGroupUsers() {
        return groupUsers;
    }

    public void setGroupId(BigInteger groupId) {
        this.groupId = groupId;
    }

    public void setPrimaryUser(User primaryUser) {
        this.primaryUser = primaryUser;
    }

    public void setGroupUsers(Set<User> groupUsers) {
        this.groupUsers = groupUsers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGroup)) return false;

        UserGroup that = (UserGroup) o;

        return new EqualsBuilder()
                .append(primaryUser, that.getPrimaryUser())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(primaryUser)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("primaryUser", primaryUser)
                .append("groupUsers", groupUsers)
                .toString();
    }
}
