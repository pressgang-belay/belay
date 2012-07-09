package com.redhat.prototype.model.auth;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
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

        Scope scope = (Scope) o;

        if (!scopeName.equals(scope.scopeName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return scopeName.hashCode();
    }
}
