package org.jboss.pressgangccms.oauth2.shared.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Shared domain class encapsulating access token expiry information
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@XmlRootElement
public class AccessTokenExpiryInfo implements Serializable {
    private static final long serialVersionUID = 3817123211329702059L;

    // Both values in seconds
    private String accessTokenExpiry; // From time of original grant
    private String accessTokenTimeRemaining; // Approximate, from when extended

    AccessTokenExpiryInfo() {
    }

    public AccessTokenExpiryInfo(String accessTokenExpiry, String accessTokenExpiryTimeRemaining) {
        this.accessTokenExpiry = accessTokenExpiry;
        this.accessTokenTimeRemaining = accessTokenExpiryTimeRemaining;
    }

    public String getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public String getAccessTokenTimeRemaining() {
        return accessTokenTimeRemaining;
    }

    public void setAccessTokenExpiry(String accessTokenExpiry) {
        this.accessTokenExpiry = accessTokenExpiry;
    }

    public void setAccessTokenTimeRemaining(String accessTokenTimeRemaining) {
        this.accessTokenTimeRemaining = accessTokenTimeRemaining;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(accessTokenExpiry)
                .append(accessTokenTimeRemaining)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityInfo)) return false;

        AccessTokenExpiryInfo that = (AccessTokenExpiryInfo)o;

        return new EqualsBuilder()
                .append(accessTokenExpiry, that.getAccessTokenExpiry())
                .append(accessTokenTimeRemaining, that.getAccessTokenTimeRemaining())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("accessTokenExpiry", accessTokenExpiry)
                .append("accessTokenTimeRemaining", accessTokenTimeRemaining)
                .toString();
    }
}
