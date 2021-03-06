package org.jboss.pressgang.belay.oauth2.authserver.data.model;

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
@Table(name = "SCOPE")
public class Scope implements Serializable {
    private static final long serialVersionUID = -255914952651554970L;

    private long scopeId;
    private String scopeName;

    public Scope() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCOPE_ID")
    public long getScopeId() {
        return scopeId;
    }

    @NotNull
    @Column(name = "SCOPE_NAME", unique = true)
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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scope)) return false;

        Scope that = (Scope) o;

        return scopeName.equals(that.getScopeName());
    }

    @Override
    public final int hashCode() {
        return scopeName.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("scopeName", scopeName)
                .toString();
    }
}
