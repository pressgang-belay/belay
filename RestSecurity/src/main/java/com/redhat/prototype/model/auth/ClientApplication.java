package com.redhat.prototype.model.auth;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
@Table(name="CLIENT", uniqueConstraints = @UniqueConstraint(columnNames = { "CLIENT_IDENTIFIER"}))
public class ClientApplication implements Serializable {

    private static final long serialVersionUID = 2098266256101575627L;

    private Long clientId;
    private String clientIdentifier;
    private String clientName;
    private String clientSecret;
    private Set<TokenGrant> tokenGrants = new HashSet<TokenGrant>();

    public ClientApplication() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "CLIENT_ID")
    public Long getClientId() {
        return clientId;
    }

    @NotNull
    @Size(min = 10, max = 35)
    @Pattern(regexp = "[A-Za-z0-9!_]*", message = "must contain only letters, numbers or the characters ! or _")
    @Column(name = "CLIENT_IDENTIFIER")
    public String getClientIdentifier() {
        return clientIdentifier;
    }

    @NotNull
    @Size(min = 1, max = 20)
    @Pattern(regexp = "[A-Za-z ]*", message = "must contain only letters and spaces")
    @Column(name = "CLIENT_NAME")
    public String getClientName() {
        return clientName;
    }

    @Column(name = "CLIENT_SECRET")
    public String getClientSecret() {
        return clientSecret;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name="CLIENT_TOKEN_GRANT", joinColumns = { @JoinColumn(name = "CLIENT_ID") },
            inverseJoinColumns = { @JoinColumn(name = "TOKEN_GRANT_ID") })
    public Set<TokenGrant> getTokenGrants() {
        return tokenGrants;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setTokenGrants(Set<TokenGrant> tokenGrants) {
        this.tokenGrants = tokenGrants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientApplication)) return false;

        ClientApplication that = (ClientApplication) o;

        if (!clientIdentifier.equals(that.clientIdentifier)) return false;
        if (!clientName.equals(that.clientName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clientIdentifier.hashCode();
        result = 31 * result + clientName.hashCode();
        return result;
    }
}
