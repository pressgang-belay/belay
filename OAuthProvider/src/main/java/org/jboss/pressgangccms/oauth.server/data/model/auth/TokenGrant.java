package org.jboss.pressgangccms.oauth.server.data.model.auth;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistence logic for OAuth tokens granted.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="TOKEN_GRANT")
public class TokenGrant implements Serializable {

    private static final long serialVersionUID = -2401887340233987092L;

    private BigInteger tokenGrantId;
    private String accessToken;
    private String refreshToken;
    private String accessTokenExpiry; // In seconds, from time granted
    private Date grantTimeStamp;
    private ClientApplication grantClient;
    private User grantUser;
    private Boolean grantCurrent;
    private Set<Scope> grantScopes = new HashSet<Scope>();

    public TokenGrant() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "TOKEN_GRANT_ID")
    public BigInteger getTokenGrantId() {
        return tokenGrantId;
    }

    @NotNull
    @Column(name = "ACCESS_TOKEN")
    public String getAccessToken() {
        return accessToken;
    }

    @NotNull
    @Column(name = "REFRESH_TOKEN")
    public String getRefreshToken() {
        return refreshToken;
    }

    @NotNull
    @Column(name = "ACCESS_TOKEN_EXPIRY")
    public String getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "TOKEN_GRANT_TIME_STAMP")
    public Date getGrantTimeStamp() {
        return grantTimeStamp;
    }

    @NotNull
    @ManyToOne
    public ClientApplication getGrantClient() {
        return grantClient;
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "OPENID_USER_USER_ID")
    public User getGrantUser() {
        return grantUser;
    }

    @NotNull
    @Column(name = "TOKEN_GRANT_CURRENT")
    public Boolean getGrantCurrent() {
        return grantCurrent;
    }

    @OneToMany
    @JoinColumn(name = "SCOPE_SCOPE_ID")
    public Set<Scope> getGrantScopes() {
        return grantScopes;
    }

    public void setTokenGrantId(BigInteger tokenGrantId) {
        this.tokenGrantId = tokenGrantId;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setAccessTokenExpiry(String accessTokenExpiry) {
        this.accessTokenExpiry = accessTokenExpiry;
    }

    public void setGrantTimeStamp(Date grantTimeStamp) {
        this.grantTimeStamp = grantTimeStamp;
    }

    public void setGrantClient(ClientApplication grantClient) {
        this.grantClient = grantClient;
    }

    public void setGrantUser(User grantUser) {
        this.grantUser = grantUser;
    }

    public void setGrantCurrent(Boolean grantCurrent) {
        this.grantCurrent = grantCurrent;
    }

    public void setGrantScopes(Set<Scope> grantScopes) {
        this.grantScopes = grantScopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenGrant)) return false;

        TokenGrant that = (TokenGrant) o;

        return new EqualsBuilder()
                .append(accessToken, that.getAccessToken())
                .append(refreshToken, that.getRefreshToken())
                .append(accessTokenExpiry, that.getAccessTokenExpiry())
                .append(grantTimeStamp, that.getGrantTimeStamp())
                .append(grantClient, that.getGrantClient())
                .append(grantUser, that.getGrantUser())
                .append(grantCurrent, that.getGrantCurrent())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(accessToken)
                .append(refreshToken)
                .append(accessTokenExpiry)
                .append(grantTimeStamp)
                .append(grantClient)
                .append(grantUser)
                .append(grantCurrent)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accessToken", accessToken)
                .append("refreshToken", refreshToken)
                .append("accessTokenExpiry", accessTokenExpiry)
                .append("grantTimeStamp", grantTimeStamp)
                .append("grantClient", grantClient)
                .append("grantUser", grantUser)
                .append("grantCurrent", grantCurrent)
                .append("grantScopes", grantScopes)
                .toString();
    }
}
