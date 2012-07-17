package org.jboss.pressgangccms.oauth.server.data.model.auth;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Persistence logic for OAuth scopes.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="SCOPE", uniqueConstraints = @UniqueConstraint(columnNames = { "SCOPE_NAME" }))
public class Scope implements Serializable {
    private static final long serialVersionUID = -255914952651554970L;

    private long scopeId;
    private String scopeName;

    public Scope() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "SCOPE_ID")
    public long getScopeId() {
        return scopeId;
    }

    @NotNull
    @Column(name = "SCOPE_NAME")
    public String getScopeName() {
        return scopeName;
    }

    public void setScopeId(long scopeId) {
        this.scopeId = scopeId;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scope)) return false;

        Scope that = (Scope) o;

        return new EqualsBuilder()
                .append(scopeName, that.getScopeName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(scopeName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("scopeName", scopeName)
                .toString();
    }
}
