package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser;

public class PrivateUrlUtil {

    /**
     * @todo If there is a use case for this outside the context of Private URL,
     * move this method to somewhere more centralized.
     */
    static Dataset getDatasetFromRoleAssignment(RoleAssignment roleAssignment) {
        if (roleAssignment == null) {
            return null;
        }
        DvObject dvObject = roleAssignment.getDefinitionPoint();
        if (dvObject == null) {
            return null;
        }
        if (dvObject instanceof Dataset) {
            return (Dataset) roleAssignment.getDefinitionPoint();
        } else {
            return null;
        }
    }

    /**
     * @todo If there is a use case for this outside the context of Private URL,
     * move this method to somewhere more centralized.
     */
    static public DatasetVersion getDraftDatasetVersionFromRoleAssignment(RoleAssignment roleAssignment) {
        if (roleAssignment == null) {
            return null;
        }
        Dataset dataset = getDatasetFromRoleAssignment(roleAssignment);
        if (dataset != null) {
            DatasetVersion latestVersion = dataset.getLatestVersion();
            if (latestVersion.isDraft()) {
                return latestVersion;
            }
        }
        return null;
    }

    static public PrivateUrlUser getPrivateUrlUserFromRoleAssignment(RoleAssignment roleAssignment) {
        if (roleAssignment == null) {
            return null;
        }
        Dataset dataset = getDatasetFromRoleAssignment(roleAssignment);
        if (dataset != null) {
            PrivateUrlUser privateUrlUser = new PrivateUrlUser(dataset.getId());
            return privateUrlUser;
        }
        return null;
    }

}
