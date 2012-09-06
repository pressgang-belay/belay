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
@Table(name = "RS_ENDPOINT", uniqueConstraints = @UniqueConstraint(columnNames = {"ENDPOINT_URL_PATTERN", "ENDPOINT_METHOD"}))
public class OAuth2RSEndpoint implements Serializable {
    private static final long serialVersionUID = -7272768043550790917L;

    private BigInteger endpointId;
    private String endpointUrlPattern;  // URL String or regex String if URL contains variable parts
    private String endpointMethod;

    public OAuth2RSEndpoint() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ENDPOINT_ID")
    public BigInteger getEndpointId() {
        return endpointId;
    }

    @NotNull
    @Column(name = "ENDPOINT_URL_PATTERN")
    public String getEndpointUrlPattern() {
        return endpointUrlPattern;
    }

    @Column(name = "ENDPOINT_METHOD")
    public String getEndpointMethod() {
        return endpointMethod;
    }

    public void setEndpointId(BigInteger endpointId) {
        this.endpointId = endpointId;
    }

    public void setEndpointUrlPattern(String endpointUrlPattern) {
        this.endpointUrlPattern = endpointUrlPattern;
    }

    public void setEndpointMethod(String endpointMethod) {
        this.endpointMethod = endpointMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuth2RSEndpoint)) return false;

        OAuth2RSEndpoint that = (OAuth2RSEndpoint) o;

        return new EqualsBuilder()
                .append(endpointUrlPattern, that.getEndpointUrlPattern())
                .append(endpointMethod, that.getEndpointMethod())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(endpointUrlPattern)
                .append(endpointMethod)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("endpointUrlPattern", endpointUrlPattern)
                .append("endpointMethod", endpointMethod)
                .toString();
    }
}
