package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Persistence logic for client application user approvals.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name = "CLIENT_APPROVAL")
public class ClientApproval implements Serializable {
    private static final long serialVersionUID = -5130955185279441324L;

    private BigInteger clientApprovalId;
    private ClientApplication clientApplication;
    private User approver;
    private Set<Scope> approvedScopes = newHashSet();

    public ClientApproval() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_APPROVAL_ID")
    public BigInteger getClientApprovalId() {
        return clientApprovalId;
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "CLIENT_ID")
    public ClientApplication getClientApplication() {
        return clientApplication;
    }

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    public User getApprover() {
        return approver;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "CLIENT_APPROVAL_SCOPE", joinColumns = {@JoinColumn(name = "CLIENT_APPROVAL_ID")},
            inverseJoinColumns = {@JoinColumn(name = "SCOPE_ID")})
    public Set<Scope> getApprovedScopes() {
        return approvedScopes;
    }

    public void setClientApprovalId(BigInteger clientApprovalId) {
        this.clientApprovalId = clientApprovalId;
    }

    public void setClientApplication(ClientApplication clientApplication) {
        this.clientApplication = clientApplication;
    }

    public void setApprover(User user) {
        this.approver = user;
    }

    public void setApprovedScopes(Set<Scope> approvedScopes) {
        this.approvedScopes = approvedScopes;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientApproval)) return false;

        ClientApproval that = (ClientApproval) o;

        return new EqualsBuilder()
                .append(clientApplication, that.getClientApplication())
                .append(approver, that.getApprover())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(clientApplication)
                .append(approver)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("clientApplication", clientApplication)
                .append("approver", approver)
                .append("approvedScopes", approvedScopes)
                .toString();
    }
}
