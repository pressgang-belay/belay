package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Set;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;

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
    private Boolean accessTokenExpires;
    private String accessTokenExpiry; // In seconds, from time granted
    private Date grantTimeStamp;
    private ClientApplication grantClient;
    private User grantUser;
    private Boolean grantCurrent;
    private Set<Scope> grantScopes = newHashSet();

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

    @Column(name = "REFRESH_TOKEN")
    public String getRefreshToken() {
        return refreshToken;
    }

    @NotNull
    @Column(name = "ACCESS_TOKEN_EXPIRES")
    public Boolean getAccessTokenExpires() {
        return accessTokenExpires;
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

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TOKEN_GRANT_SCOPE", joinColumns = { @JoinColumn(name = "TOKEN_GRANT_ID") },
            inverseJoinColumns = { @JoinColumn(name = "SCOPE_ID") })
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

    public void setAccessTokenExpires(Boolean accessTokenExpires) {
        this.accessTokenExpires = accessTokenExpires;
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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenGrant)) return false;

        TokenGrant that = (TokenGrant) o;

        return new EqualsBuilder()
                .append(accessToken, that.getAccessToken())
                .append(refreshToken, that.getRefreshToken())
                .append(accessTokenExpires, that.getAccessTokenExpires())
                .append(accessTokenExpiry, that.getAccessTokenExpiry())
                .append(grantTimeStamp, that.getGrantTimeStamp())
                .append(grantClient, that.getGrantClient())
                .append(grantUser, that.getGrantUser())
                .append(grantCurrent, that.getGrantCurrent())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(accessToken)
                .append(refreshToken)
                .append(accessTokenExpires)
                .append(accessTokenExpiry)
                .append(grantTimeStamp)
                .append(grantClient)
                .append(grantUser)
                .append(grantCurrent)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("accessToken", accessToken)
                .append("refreshToken", refreshToken)
                .append("accessTokenExpires", accessTokenExpires)
                .append("accessTokenExpiry", accessTokenExpiry)
                .append("grantTimeStamp", grantTimeStamp)
                .append("grantClient", grantClient)
                .append("grantUser", grantUser)
                .append("grantCurrent", grantCurrent)
                .append("grantScopes", grantScopes)
                .toString();
    }
}
