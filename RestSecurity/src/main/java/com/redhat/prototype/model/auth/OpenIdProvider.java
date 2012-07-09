package com.redhat.prototype.model.auth;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
@Table(name="OPENID_PROVIDER", uniqueConstraints = @UniqueConstraint(columnNames = { "PROVIDER_URL"} ))
public class OpenIdProvider implements Serializable {
    private static final long serialVersionUID = -7790803759533859471L;

    private long providerId;
    private String providerName;
    private String providerUrl;

    public OpenIdProvider() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "PROVIDER_ID")
    public long getProviderId() {
        return providerId;
    }

    @NotNull
    @Column(name = "PROVIDER_NAME")
    public String getProviderName() {
        return providerName;
    }

    @NotNull
    @Column(name = "PROVIDER_URL")
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpenIdProvider)) return false;

        OpenIdProvider that = (OpenIdProvider) o;

        if (!providerName.equals(that.providerName)) return false;
        if (!providerUrl.equals(that.providerUrl)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = providerName.hashCode();
        result = 31 * result + providerUrl.hashCode();
        return result;
    }
}
