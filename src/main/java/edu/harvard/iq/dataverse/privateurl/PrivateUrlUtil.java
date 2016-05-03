package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser;
import java.util.logging.Logger;

public class PrivateUrlUtil {

    private static final Logger logger = Logger.getLogger(PrivateUrlUtil.class.getCanonicalName());

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

    static PrivateUrlRedirectData getPrivateUrlRedirectData(RoleAssignment roleAssignment) throws Exception {
        PrivateUrlUser privateUrlUser = PrivateUrlUtil.getPrivateUrlUserFromRoleAssignment(roleAssignment);
        String draftDatasetPageToBeRedirectedTo = PrivateUrlUtil.getDraftDatasetPageToBeRedirectedTo(roleAssignment);
        return new PrivateUrlRedirectData(privateUrlUser, draftDatasetPageToBeRedirectedTo);
    }

    static String getDraftDatasetPageToBeRedirectedTo(RoleAssignment roleAssignment) throws Exception {
        return getDraftUrl(getDraftDatasetVersionFromRoleAssignment(roleAssignment));
    }

    static String getDraftUrl(DatasetVersion draft) throws Exception {
        if (draft != null) {
            Dataset dataset = draft.getDataset();
            if (dataset != null) {
                String persistentId = dataset.getGlobalId();
                if (persistentId != null) {
                    String relativeUrl = "/dataset.xhtml?persistentId=" + persistentId + "&version=DRAFT";
                    return relativeUrl;
                }
            }
        }
        throw new Exception();
    }

    static PrivateUrl getPrivateUrlFromRoleAssignment(RoleAssignment roleAssignment, String dataverseSiteUrl) {
        if (dataverseSiteUrl == null) {
            logger.info("dataverseSiteUrl was null. Can not instantiate a PrivateUrl object.");
            return null;
        }
        Dataset dataset = PrivateUrlUtil.getDatasetFromRoleAssignment(roleAssignment);
        if (dataset != null) {
            PrivateUrl privateUrl = new PrivateUrl(roleAssignment, dataset, dataverseSiteUrl);
            return privateUrl;
        } else {
            return null;
        }
    }

}
