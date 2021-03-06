package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Persistence logic for client applications.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name = "CLIENT")
public class ClientApplication implements Serializable {

    private static final long serialVersionUID = 2098266256101575627L;

    private long clientId;
    private String clientIdentifier;
    private String clientName;
    private String clientSecret;
    private String clientRedirectUri;
    private Boolean tokenGrantsMustExpire;
    private Set<TokenGrant> tokenGrants = newHashSet();
    private Set<CodeGrant> codeGrants = newHashSet();

    public ClientApplication() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_ID")
    public long getClientId() {
        return clientId;
    }

    @NotNull
    @Column(name = "CLIENT_IDENTIFIER", unique = true)
    public String getClientIdentifier() {
        return clientIdentifier;
    }

    @NotNull
    @Column(name = "CLIENT_NAME", unique = true)
    public String getClientName() {
        return clientName;
    }

    @NotNull
    @Column(name = "CLIENT_REDIRECT_URI", unique = true)
    public String getClientRedirectUri() {
        return clientRedirectUri;
    }

    @Column(name = "CLIENT_SECRET")
    public String getClientSecret() {
        return clientSecret;
    }

    @NotNull
    @Column(name = "GRANTS_MUST_EXPIRE")
    public Boolean getTokenGrantsMustExpire() {
        return tokenGrantsMustExpire;
    }

    @OneToMany(mappedBy = "grantClient")
    public Set<TokenGrant> getTokenGrants() {
        return tokenGrants;
    }

    @OneToMany(mappedBy = "grantClient")
    public Set<CodeGrant> getCodeGrants() {
        return codeGrants;
    }

    public void setClientId(long clientId) {
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

    public void setTokenGrantsMustExpire(Boolean tokenGrantsMustExpire) {
        this.tokenGrantsMustExpire = tokenGrantsMustExpire;
    }

    public void setTokenGrants(Set<TokenGrant> tokenGrants) {
        this.tokenGrants = tokenGrants;
    }

    public void setCodeGrants(Set<CodeGrant> codeGrants) {
        this.codeGrants = codeGrants;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientApplication)) return false;

        ClientApplication that = (ClientApplication) o;

        return new EqualsBuilder()
                .append(clientIdentifier, that.getClientIdentifier())
                .append(clientName, that.getClientName())
                .append(clientRedirectUri, that.getClientRedirectUri())
                .append(clientSecret, that.getClientSecret())
                .append(tokenGrantsMustExpire, that.getTokenGrantsMustExpire())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(clientIdentifier)
                .append(clientName)
                .append(clientRedirectUri)
                .append(clientSecret)
                .append(tokenGrantsMustExpire)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("clientIdentifier", clientIdentifier)
                .append("clientName", clientName)
                .append("clientRedirectUri", clientRedirectUri)
                .append("clientSecret", clientSecret)
                .append("tokenGrantsMustExpire", tokenGrantsMustExpire)
                .toString();
    }
}
