package org.jboss.pressgang.belay.oauth2.resourceserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Persistence logic for OAuth2 scopes and their mapping to resource server endpoints.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name = "RS_SCOPE", uniqueConstraints = @UniqueConstraint(columnNames = {"SCOPE_NAME"}))
public class OAuth2RSScope implements Serializable {
    private static final long serialVersionUID = -255914952651554970L;

    private long scopeId;
    private String scopeName;
    private Set<OAuth2RSEndpoint> scopeEndpoints = newHashSet();

    public OAuth2RSScope() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCOPE_ID")
    public long getScopeId() {
        return scopeId;
    }

    @NotNull
    @Column(name = "SCOPE_NAME")
    public String getScopeName() {
        return scopeName;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "RS_SCOPE_RS_ENDPOINT", joinColumns = {@JoinColumn(name = "SCOPE_ID")},
            inverseJoinColumns = {@JoinColumn(name = "ENDPOINT_ID")})
    public Set<OAuth2RSEndpoint> getScopeEndpoints() {
        return scopeEndpoints;
    }

    public void setScopeId(long scopeId) {
        this.scopeId = scopeId;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public void setScopeEndpoints(Set<OAuth2RSEndpoint> scopeEndpoints) {
        this.scopeEndpoints = scopeEndpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuth2RSScope)) return false;

        OAuth2RSScope that = (OAuth2RSScope) o;

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
