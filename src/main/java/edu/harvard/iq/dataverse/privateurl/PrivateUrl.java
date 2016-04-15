package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.RoleAssignment;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Dataset authors can create and send a Private URL to a reviewer to see the
 * lasted draft of their dataset (even if the dataset has never been published)
 * without the reviewer having to create an account. When the dataset is
 * published, the Private URL is deleted. Creating a Public URL creates a role
 * assignment (read only access) and when that role assignments is revoked, the
 * Public URL is deleted.
 *
 * @todo Should this be called PrivateUrlData instead? It might reduce confusion
 * in other parts of the code.
 */
@Entity
public class PrivateUrl implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "dataset_id", nullable = false, unique = true)
    private Dataset dataset;

    @OneToOne
    @JoinColumn(name = "roleassignment_id", nullable = false, unique = true)
    private RoleAssignment roleAssignment;

    /**
     * Don't use this constructor. It only exists to get JPA to stop complaining
     * about the following.
     *
     * Exception Description: The instance creation method
     * [edu.harvard.iq.dataverse.privateurl.PrivateUrl.<Default Constructor>],
     * with no parameters, does not exist, or is not accessible.
     */
    @Deprecated
    public PrivateUrl() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Deprecated
    public PrivateUrl(Dataset dataset, String token) {
        this.token = token;
        this.dataset = dataset;
    }    
    
    public PrivateUrl(String token, Dataset dataset, RoleAssignment roleAssignment) {
        this.token = token;
        this.dataset = dataset;
        this.roleAssignment = roleAssignment;
    }

    public RoleAssignment getRoleAssignment() {
        return roleAssignment;
    }

    public void setRoleAssignment(RoleAssignment roleAssignment) {
        this.roleAssignment = roleAssignment;
    }

}
