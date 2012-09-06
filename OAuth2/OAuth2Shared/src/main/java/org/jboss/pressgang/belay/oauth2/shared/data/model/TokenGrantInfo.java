package org.jboss.pressgang.belay.oauth2.shared.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Shared domain class encapsulating token grant information.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@XmlRootElement
public class TokenGrantInfo implements Serializable {
    private static final long serialVersionUID = -2251511153184544618L;

    private String accessToken;
    private String accessTokenExpiry;
    private Boolean hasRefreshToken;
    private String grantIdentityIdentifier;
    private String grantClientIdentifier;
    private Date grantTimeStamp; // In seconds, from time granted
    private Boolean grantCurrent;
    private Set<String> grantScopeNames;

    TokenGrantInfo() {
    }

    private TokenGrantInfo(TokenGrantInfoBuilder builder) {
        this.accessToken = builder.accessToken;
        this.accessTokenExpiry = builder.accessTokenExpiry;
        this.hasRefreshToken = builder.hasRefreshToken;
        this.grantIdentityIdentifier = builder.grantIdentityIdentifier;
        this.grantClientIdentifier = builder.grantClientIdentifier;
        this.grantTimeStamp = builder.grantTimeStamp;
        this.grantCurrent = builder.grantCurrent;
        this.grantScopeNames = builder.grantScopeNames;
    }

    public String getGrantClientIdentifier() {
        return grantClientIdentifier;
    }

    public String getGrantIdentityIdentifier() {
        return grantIdentityIdentifier;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public Boolean getHasRefreshToken() {
        return hasRefreshToken;
    }

    public Date getGrantTimeStamp() {
        return grantTimeStamp;
    }

    public Boolean getGrantCurrent() {
        return grantCurrent;
    }

    public Set<String> getGrantScopeNames() {
        return grantScopeNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityInfo)) return false;

        TokenGrantInfo that = (TokenGrantInfo) o;

        return new EqualsBuilder()
                .append(accessToken, that.getAccessToken())
                .append(accessTokenExpiry, that.getAccessTokenExpiry())
                .append(hasRefreshToken, that.getHasRefreshToken())
                .append(grantIdentityIdentifier, that.getGrantIdentityIdentifier())
                .append(grantClientIdentifier, that.getGrantClientIdentifier())
                .append(grantTimeStamp, that.getGrantTimeStamp())
                .append(grantCurrent, that.getGrantCurrent())
                .append(grantScopeNames, that.getGrantScopeNames())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(accessToken)
                .append(accessTokenExpiry)
                .append(hasRefreshToken)
                .append(grantIdentityIdentifier)
                .append(grantClientIdentifier)
                .append(grantTimeStamp)
                .append(grantCurrent)
                .append(grantScopeNames)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accessToken", accessToken)
                .append("accessTokenExpiry", accessTokenExpiry)
                .append("hasRefreshToken", hasRefreshToken)
                .append("grantIdentityIdentifier", grantIdentityIdentifier)
                .append("grantClientIdentifier", grantClientIdentifier)
                .append("grantTimeStamp", grantTimeStamp)
                .append("grantCurrent", grantCurrent)
                .append("grantScopeNames", grantScopeNames)
                .toString();
    }

    /**
     * Builder class to assist with creating TokenGrantInfo objects. Implements builder interface.
     */
    public static class TokenGrantInfoBuilder implements Builder<TokenGrantInfo> {
        private String accessToken;
        private String accessTokenExpiry;
        private boolean hasRefreshToken;
        private String grantIdentityIdentifier;
        private String grantClientIdentifier;
        private Date grantTimeStamp;
        private boolean grantCurrent;
        private Set<String> grantScopeNames;

        TokenGrantInfoBuilder() {
        }

        public static TokenGrantInfoBuilder tokenGrantInfoBuilder(String accessToken) {
            TokenGrantInfoBuilder builder = new TokenGrantInfoBuilder();
            builder.accessToken = accessToken;
            return builder;
        }

        public TokenGrantInfoBuilder setAccessTokenExpiry(String accessTokenExpiry) {
            this.accessTokenExpiry = accessTokenExpiry;
            return this;
        }

        public TokenGrantInfoBuilder setHasRefreshToken(boolean hasRefreshToken) {
            this.hasRefreshToken = hasRefreshToken;
            return this;
        }

        public TokenGrantInfoBuilder setGrantIdentityIdentifier(String grantIdentityIdentifier) {
            this.grantIdentityIdentifier = grantIdentityIdentifier;
            return this;
        }

        public TokenGrantInfoBuilder setGrantClientIdentifier(String grantClientIdentifier) {
            this.grantClientIdentifier = grantClientIdentifier;
            return this;
        }

        public TokenGrantInfoBuilder setGrantTimeStamp(Date grantTimeStamp) {
            this.grantTimeStamp = grantTimeStamp;
            return this;
        }

        public TokenGrantInfoBuilder setGrantCurrent(boolean grantCurrent) {
            this.grantCurrent = grantCurrent;
            return this;
        }

        public TokenGrantInfoBuilder setGrantScopeNames(Set<String> grantScopeNames) {
            this.grantScopeNames = grantScopeNames;
            return this;
        }

        @Override
        public TokenGrantInfo build() {
            return new TokenGrantInfo(this);
        }
    }
}
