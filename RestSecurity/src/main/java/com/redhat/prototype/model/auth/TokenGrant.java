package com.redhat.prototype.model.auth;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@XmlRootElement
@Table(name="TOKEN_GRANT")
public class TokenGrant implements Serializable {

    private static final long serialVersionUID = -2401887340233987092L;

    private Long tokenGrantId;
    private String accessToken;
    private String refreshToken;
    private String accessTokenExpiry;
    private Date grantTimeStamp;
    private ClientApplication grantClient;
    private User grantUser;
    private Set<Scope> grantScopes;

    protected TokenGrant() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "TOKEN_GRANT_ID")
    public Long getTokenGrantId() {
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
    @ManyToOne(cascade = CascadeType.ALL)
    public ClientApplication getGrantClient() {
        return grantClient;
    }

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    public User getGrantUser() {
        return grantUser;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name="TOKEN_GRANT_SCOPE", joinColumns = { @JoinColumn(name = "TOKEN_GRANT_ID") },
            inverseJoinColumns = { @JoinColumn(name = "SCOPE_ID") })
    public Set<Scope> getGrantScopes() {
        return grantScopes;
    }

    public void setTokenGrantId(Long tokenGrantId) {
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

    public void setGrantScopes(Set<Scope> grantScopes) {
        this.grantScopes = grantScopes;
    }
}
