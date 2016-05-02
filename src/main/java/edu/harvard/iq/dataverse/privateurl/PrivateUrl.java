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

    /**
     * @todo Do we even need a Dataset field? Instead we could use
     * roleAssignment.getDefinitionPoint() and cast to Dataset. Should Private
     * URL be more generic anyway? Will we want to use it on other DvObjects
     * some day such as Dataverses and DataFiles?
     */
    private final Dataset dataset;
    private final RoleAssignment roleAssignment;
    /**
     * @todo Should the token be exposed here as a separate field? It's nice and
     * convenient. The token is also available at
     * roleAssignment.getPrivateUrlToken().
     */
    private final String token;
    /**
     * @todo This link should probably be some sort of URL object rather than a
     * String.
     */
    private final String link;

    public PrivateUrl(RoleAssignment roleAssignment, Dataset dataset, String dataverseSiteUrl) {
        this.token = roleAssignment.getPrivateUrlToken();
        this.link = dataverseSiteUrl + "/privateurl.xhtml?token=" + token;
        this.dataset = dataset;
        this.roleAssignment = roleAssignment;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public RoleAssignment getRoleAssignment() {
        return roleAssignment;
    }

    public String getToken() {
        return token;
    }

    public String getLink() {
        return link;
    }

}
