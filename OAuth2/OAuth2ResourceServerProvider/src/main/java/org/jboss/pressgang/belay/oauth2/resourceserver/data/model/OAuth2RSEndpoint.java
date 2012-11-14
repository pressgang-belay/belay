package org.jboss.pressgang.belay.oauth2.resourceserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * Persistence logic for application endpoints.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name = "RS_ENDPOINT", uniqueConstraints = @UniqueConstraint(columnNames = {"ENDPOINT_URL", "ENDPOINT_METHOD"}))
public class OAuth2RSEndpoint implements Serializable {
    private static final long serialVersionUID = -7272768043550790917L;

    private BigInteger endpointId;
    private String endpointUrl;  // URL String or Java regex String if URL contains variable parts
    private String endpointMethod;
    private Boolean urlRegularExpression; // True if the endpoint URL is a regular expression, false if it is an exact URL

    public OAuth2RSEndpoint() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ENDPOINT_ID")
    public BigInteger getEndpointId() {
        return endpointId;
    }

    @NotNull
    @Column(name = "ENDPOINT_URL")
    public String getEndpointUrl() {
        return endpointUrl;
    }

    @Column(name = "ENDPOINT_METHOD")
    public String getEndpointMethod() {
        return endpointMethod;
    }

    @NotNull
    @Column(name = "URL_REGEX")
    public Boolean getUrlRegularExpression() {
        return urlRegularExpression;
    }

    public void setEndpointId(BigInteger endpointId) {
        this.endpointId = endpointId;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public void setEndpointMethod(String endpointMethod) {
        this.endpointMethod = endpointMethod;
    }

    public void setUrlRegularExpression(Boolean urlRegularExpression) {
        this.urlRegularExpression = urlRegularExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuth2RSEndpoint)) return false;

        OAuth2RSEndpoint that = (OAuth2RSEndpoint) o;

        return new EqualsBuilder()
                .append(endpointUrl, that.getEndpointUrl())
                .append(endpointMethod, that.getEndpointMethod())
                .append(urlRegularExpression, that.getUrlRegularExpression())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(endpointUrl)
                .append(endpointMethod)
                .append(urlRegularExpression)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("endpointUrl", endpointUrl)
                .append("endpointMethod", endpointMethod)
                .append("urlRegularExpression", urlRegularExpression)
                .toString();
    }
}
