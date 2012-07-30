package org.jboss.pressgangccms.oauth.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistence logic for OAuth scopes.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="SCOPE", uniqueConstraints = @UniqueConstraint(columnNames = { "SCOPE_NAME" }))
public class Scope implements Serializable {
    private static final long serialVersionUID = -255914952651554970L;

    private BigInteger scopeId;
    private String scopeName;
    private Set<Endpoint> scopeEndpoints = new HashSet<Endpoint>();

    public Scope() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "SCOPE_ID")
    public BigInteger getScopeId() {
        return scopeId;
    }

    @NotNull
    @Column(name = "SCOPE_NAME")
    public String getScopeName() {
        return scopeName;
    }

    @ManyToMany
    @JoinTable(name = "SCOPE_ENDPOINT", joinColumns = { @JoinColumn(name = "SCOPE_ID") },
            inverseJoinColumns = { @JoinColumn(name = "ENDPOINT_ID") })
    public Set<Endpoint> getScopeEndpoints() {
        return scopeEndpoints;
    }

    public void setScopeId(BigInteger scopeId) {
        this.scopeId = scopeId;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public void setScopeEndpoints(Set<Endpoint> scopeEndpoints) {
        this.scopeEndpoints = scopeEndpoints;
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
                .append("scopeEndpoints", scopeEndpoints)
                .toString();
    }
}
