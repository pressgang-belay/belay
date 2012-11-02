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
    private Boolean accessTokenExpires;
    private String accessTokenExpiry;
    private Boolean hasRefreshToken;
    private String grantUserPrimaryIdentifier;
    private String grantClientIdentifier;
    private String grantClientName;
    private String grantClientRedirectUri;
    private Boolean grantClientTokensMustExpire;
    private Date grantTimeStamp; // In seconds, from time granted
    private Boolean grantCurrent;
    private Set<String> grantScopeNames;

    TokenGrantInfo() {
    }

    private TokenGrantInfo(TokenGrantInfoBuilder builder) {
        this.accessToken = builder.accessToken;
        this.accessTokenExpires = builder.accessTokenExpires;
        this.accessTokenExpiry = builder.accessTokenExpiry;
        this.hasRefreshToken = builder.hasRefreshToken;
        this.grantUserPrimaryIdentifier = builder.grantUserPrimaryIdentifier;
        this.grantClientIdentifier = builder.grantClientIdentifier;
        this.grantClientName = builder.grantClientName;
        this.grantClientRedirectUri = builder.grantClientRedirectUri;
        this.grantClientTokensMustExpire = builder.grantClientTokensMustExpire;
        this.grantTimeStamp = new Date(builder.grantTimeStamp.getTime());
        this.grantCurrent = builder.grantCurrent;
        this.grantScopeNames = builder.grantScopeNames;
    }

    public String getGrantUserPrimaryIdentifier() {
        return grantUserPrimaryIdentifier;
    }

    public String getGrantClientIdentifier() {
        return grantClientIdentifier;
    }

    public String getGrantClientName() {
        return grantClientName;
    }

    public String getGrantClientRedirectUri() {
        return grantClientRedirectUri;
    }

    public Boolean getGrantClientTokensMustExpire() {
        return grantClientTokensMustExpire;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Boolean getAccessTokenExpires() {
        return accessTokenExpires;
    }

    public String getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public Boolean getHasRefreshToken() {
        return hasRefreshToken;
    }

    public Date getGrantTimeStamp() {
        return new Date(grantTimeStamp.getTime());
    }

    public Boolean getGrantCurrent() {
        return grantCurrent;
    }

    public Set<String> getGrantScopeNames() {
        return grantScopeNames;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenGrantInfo)) return false;

        TokenGrantInfo that = (TokenGrantInfo) o;

        return new EqualsBuilder()
                .append(accessToken, that.getAccessToken())
                .append(accessTokenExpires, that.getAccessTokenExpires())
                .append(accessTokenExpiry, that.getAccessTokenExpiry())
                .append(hasRefreshToken, that.getHasRefreshToken())
                .append(grantUserPrimaryIdentifier, that.getGrantUserPrimaryIdentifier())
                .append(grantClientIdentifier, that.getGrantClientIdentifier())
                .append(grantClientName, that.getGrantClientName())
                .append(grantClientRedirectUri, that.getGrantClientRedirectUri())
                .append(grantClientTokensMustExpire, that.getGrantClientTokensMustExpire())
                .append(grantTimeStamp, that.getGrantTimeStamp())
                .append(grantCurrent, that.getGrantCurrent())
                .append(grantScopeNames, that.getGrantScopeNames())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(accessToken)
                .append(accessTokenExpires)
                .append(accessTokenExpiry)
                .append(hasRefreshToken)
                .append(grantUserPrimaryIdentifier)
                .append(grantClientIdentifier)
                .append(grantClientName)
                .append(grantClientRedirectUri)
                .append(grantClientTokensMustExpire)
                .append(grantTimeStamp)
                .append(grantCurrent)
                .append(grantScopeNames)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("accessToken", accessToken)
                .append("accessTokenExpires", accessTokenExpires)
                .append("accessTokenExpiry", accessTokenExpiry)
                .append("hasRefreshToken", hasRefreshToken)
                .append("grantUserPrimaryIdentifier", grantUserPrimaryIdentifier)
                .append("grantClientIdentifier", grantClientIdentifier)
                .append("grantClientName", grantClientName)
                .append("grantClientRedirectUri", grantClientRedirectUri)
                .append("grantClientTokensMustExpire", grantClientTokensMustExpire)
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
        private boolean accessTokenExpires;
        private String accessTokenExpiry;
        private boolean hasRefreshToken;
        private String grantUserPrimaryIdentifier;
        private String grantClientIdentifier;
        private String grantClientName;
        private String grantClientRedirectUri;
        private boolean grantClientTokensMustExpire;
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

        public TokenGrantInfoBuilder setAccessTokenExpires(boolean accessTokenExpires) {
            this.accessTokenExpires = accessTokenExpires;
            return this;
        }

        public TokenGrantInfoBuilder setAccessTokenExpiry(String accessTokenExpiry) {
            this.accessTokenExpiry = accessTokenExpiry;
            return this;
        }

        public TokenGrantInfoBuilder setHasRefreshToken(boolean hasRefreshToken) {
            this.hasRefreshToken = hasRefreshToken;
            return this;
        }

        public TokenGrantInfoBuilder setGrantUserPrimaryIdentifier(String grantUserPrimaryIdentifier) {
            this.grantUserPrimaryIdentifier = grantUserPrimaryIdentifier;
            return this;
        }

        public TokenGrantInfoBuilder setGrantClientIdentifier(String grantClientIdentifier) {
            this.grantClientIdentifier = grantClientIdentifier;
            return this;
        }

        public TokenGrantInfoBuilder setGrantClientName(String grantClientName) {
            this.grantClientName = grantClientName;
            return this;
        }

        public TokenGrantInfoBuilder setGrantClientRedirectUri(String grantClientRedirectUri) {
            this.grantClientRedirectUri = grantClientRedirectUri;
            return this;
        }

        public TokenGrantInfoBuilder setGrantClientTokensMustExpire(boolean grantClientTokensMustExpire) {
            this.grantClientTokensMustExpire = grantClientTokensMustExpire;
            return this;
        }

        public TokenGrantInfoBuilder setGrantTimeStamp(Date grantTimeStamp) {
            this.grantTimeStamp = new Date(grantTimeStamp.getTime());
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
