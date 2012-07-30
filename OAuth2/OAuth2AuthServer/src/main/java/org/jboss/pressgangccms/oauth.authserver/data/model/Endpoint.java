package org.jboss.pressgangccms.oauth.authserver.data.model;

import com.google.appengine.api.urlfetch.HTTPMethod;
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
@Table(name="ENDPOINT", uniqueConstraints = @UniqueConstraint(columnNames = { "ENDPOINT_URL_PATTERN", "ENDPOINT_METHOD" }))
public class Endpoint implements Serializable {
    private static final long serialVersionUID = -7272768043550790917L;

    private BigInteger endpointId;
    private String endpointUrlPattern;  // URL String or regex String if URL contains variable parts
    private HTTPMethod endpointMethod;

    public Endpoint() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "ENDPOINT_ID")
    public BigInteger getEndpointId() {
        return endpointId;
    }

    @NotNull
    @Column(name = "ENDPOINT_URL_PATTERN")
    public String getEndpointUrlPattern() {
        return endpointUrlPattern;
    }

    @Enumerated(value = EnumType.STRING)
    @Column(name = "ENDPOINT_METHOD")
    public HTTPMethod getEndpointMethod() {
        return endpointMethod;
    }

    public void setEndpointId(BigInteger endpointId) {
        this.endpointId = endpointId;
    }

    public void setEndpointUrlPattern(String endpointUrlPattern) {
        this.endpointUrlPattern = endpointUrlPattern;
    }

    public void setEndpointMethod(HTTPMethod endpointMethod) {
        this.endpointMethod = endpointMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scope)) return false;

        Endpoint that = (Endpoint) o;

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
