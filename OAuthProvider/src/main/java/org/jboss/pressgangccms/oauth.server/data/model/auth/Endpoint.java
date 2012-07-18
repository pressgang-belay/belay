package org.jboss.pressgangccms.oauth.server.data.model.auth;

import com.google.appengine.api.urlfetch.HTTPMethod;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Persistence logic for application endpoints.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="ENDPOINT", uniqueConstraints = @UniqueConstraint(columnNames = { "ENDPOINT_URL" }))
public class Endpoint implements Serializable {
    private static final long serialVersionUID = -7272768043550790917L;

    private long endpointId;
    private String endpointUrl;
    private HTTPMethod endpointMethod;

    public Endpoint() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "ENDPOINT_ID")
    public long getEndpointId() {
        return endpointId;
    }

    @NotNull
    @Column(name = "ENDPOINT_URL")
    public String getEndpointUrl() {
        return endpointUrl;
    }

    @Enumerated(value = EnumType.STRING)
    @Column(name = "ENDPOINT_METHOD")
    public HTTPMethod getEndpointMethod() {
        return endpointMethod;
    }

    public void setEndpointId(long endpointId) {
        this.endpointId = endpointId;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
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
                .append(endpointUrl, that.getEndpointUrl())
                .append(endpointMethod, that.getEndpointMethod())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(endpointUrl)
                .append(endpointMethod)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("endpointUrl", endpointUrl)
                .append("endpointMethod", endpointMethod)
                .toString();
    }
}
