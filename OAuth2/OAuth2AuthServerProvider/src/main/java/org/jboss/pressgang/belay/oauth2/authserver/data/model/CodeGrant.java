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

import static com.google.common.collect.Sets.newHashSet;

/**
 * Persistence logic for OAuth authorization codes granted.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name = "CODE_GRANT", uniqueConstraints = {@UniqueConstraint(columnNames = {"AUTH_CODE", "CODE_GRANT_TIMESTAMP"})})
public class CodeGrant implements Serializable {
    private static final long serialVersionUID = -3556571368290708467L;

    private BigInteger codeGrantId;
    private String authCode;
    private String codeExpiry; // In seconds, from time granted
    private Date grantTimeStamp;
    private ClientApplication grantClient;
    private User grantUser;
    private Boolean grantCurrent;
    private Set<Scope> grantScopes = newHashSet();

    public CodeGrant() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CODE_GRANT_ID")
    public BigInteger getCodeGrantId() {
        return codeGrantId;
    }

    @NotNull
    @Column(name = "AUTH_CODE")
    public String getAuthCode() {
        return authCode;
    }

    @NotNull
    @Column(name = "CODE_EXPIRY")
    public String getCodeExpiry() {
        return codeExpiry;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "CODE_GRANT_TIMESTAMP")
    public Date getGrantTimeStamp() {
        return new Date(grantTimeStamp.getTime());
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "CLIENT_ID")
    public ClientApplication getGrantClient() {
        return grantClient;
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    public User getGrantUser() {
        return grantUser;
    }

    @NotNull
    @Column(name = "CODE_GRANT_CURRENT")
    public Boolean getGrantCurrent() {
        return grantCurrent;
    }

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "CODE_GRANT_SCOPE", joinColumns = {@JoinColumn(name = "CODE_GRANT_ID")},
            inverseJoinColumns = {@JoinColumn(name = "SCOPE_ID")})
    public Set<Scope> getGrantScopes() {
        return grantScopes;
    }

    public void setCodeGrantId(BigInteger codeGrantId) {
        this.codeGrantId = codeGrantId;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setCodeExpiry(String codeExpiry) {
        this.codeExpiry = codeExpiry;
    }

    public void setGrantTimeStamp(Date grantTimeStamp) {
        this.grantTimeStamp = new Date(grantTimeStamp.getTime());
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
        if (!(o instanceof CodeGrant)) return false;

        CodeGrant that = (CodeGrant) o;

        return new EqualsBuilder()
                .append(authCode, that.getAuthCode())
                .append(codeExpiry, that.getCodeExpiry())
                .append(grantTimeStamp, that.getGrantTimeStamp())
                .append(grantClient, that.getGrantClient())
                .append(grantUser, that.getGrantUser())
                .append(grantCurrent, that.getGrantCurrent())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(authCode)
                .append(codeExpiry)
                .append(grantTimeStamp)
                .append(grantClient)
                .append(grantUser)
                .append(grantCurrent)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("authCode", authCode)
                .append("codeExpiry", codeExpiry)
                .append("grantTimeStamp", grantTimeStamp)
                .append("grantClient", grantClient)
                .append("grantUser", grantUser)
                .append("grantCurrent", grantCurrent)
                .append("grantScopes", grantScopes)
                .toString();
    }
}
