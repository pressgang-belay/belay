package org.jboss.pressgangccms.oauth.server.data.model.auth;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistence logic for client applications.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="CLIENT", uniqueConstraints = @UniqueConstraint(columnNames = { "CLIENT_IDENTIFIER"}))
public class ClientApplication implements Serializable {

    private static final long serialVersionUID = 2098266256101575627L;

    private BigInteger clientId;
    private String clientIdentifier;
    private String clientName;
    private String clientSecret;
    private String clientRedirectUri;
    private Set<TokenGrant> tokenGrants = new HashSet<TokenGrant>();

    public ClientApplication() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "CLIENT_ID")
    public BigInteger getClientId() {
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

    @NotNull
    @Column(name = "CLIENT_REDIRECT_URI")
    public String getClientRedirectUri() {
        return clientRedirectUri;
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

    public void setClientId(BigInteger clientId) {
        this.clientId = clientId;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setClientRedirectUri(String clientRedirectUri) {
        this.clientRedirectUri = clientRedirectUri;
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

        return new EqualsBuilder()
                .append(clientIdentifier, that.getClientIdentifier())
                .append(clientName, that.getClientName())
                .append(clientRedirectUri, that.getClientRedirectUri())
                .append(clientSecret, that.getClientSecret())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(clientIdentifier)
                .append(clientName)
                .append(clientRedirectUri)
                .append(clientSecret)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("clientIdentifier", clientIdentifier)
                .append("clientName", clientName)
                .append("clientRedirectUri", clientRedirectUri)
                .append("clientSecret", clientSecret)
                .append("tokenGrants", tokenGrants)
                .toString();
    }
}
