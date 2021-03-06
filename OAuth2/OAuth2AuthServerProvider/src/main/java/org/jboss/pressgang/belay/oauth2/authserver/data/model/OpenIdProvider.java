package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Persistence logic for OpenID Providers.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name = "OPENID_PROVIDER")
public class OpenIdProvider implements Serializable {
    private static final long serialVersionUID = -7790803759533859471L;

    private long providerId;
    private String providerName;
    private String providerUrl;

    public OpenIdProvider() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROVIDER_ID")
    public long getProviderId() {
        return providerId;
    }

    @NotNull
    @Column(name = "PROVIDER_NAME", unique = true)
    public String getProviderName() {
        return providerName;
    }

    @NotNull
    @Column(name = "PROVIDER_URL", unique = true)
    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderId(long providerId) {
        this.providerId = providerId;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpenIdProvider)) return false;

        OpenIdProvider that = (OpenIdProvider) o;

        return new EqualsBuilder()
                .append(providerName, that.getProviderName())
                .append(providerUrl, that.getProviderUrl())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(providerName)
                .append(providerUrl)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("providerName", providerName)
                .append("providerUrl", providerUrl)
                .toString();
    }
}
