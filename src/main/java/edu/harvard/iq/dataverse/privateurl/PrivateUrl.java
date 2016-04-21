package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.RoleAssignment;

/**
 * Dataset authors can create and send a Private URL to a reviewer to see the
 * lasted draft of their dataset (even if the dataset has never been published)
 * without having to create an account. When the dataset is published, the
 * Private URL is deleted.
 *
 * @todo Should this be called PrivateUrlData instead? It might reduce confusion
 * in other parts of the code.
 */
public class PrivateUrl {

    private final String token;
    private final Dataset dataset;
    private RoleAssignment roleAssignment;

    public String getToken() {
        return token;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public PrivateUrl(Dataset dataset, String token) {
        this.token = token;
        this.dataset = dataset;
    }

    public RoleAssignment getRoleAssignment() {
        return roleAssignment;
    }

    public void setRoleAssignment(RoleAssignment roleAssignment) {
        this.roleAssignment = roleAssignment;
    }

}
