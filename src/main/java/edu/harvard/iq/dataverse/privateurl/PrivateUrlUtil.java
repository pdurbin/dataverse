package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser;
import static edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser.PREFIX;
import java.util.logging.Logger;

public class PrivateUrlUtil {

    private static final Logger logger = Logger.getLogger(PrivateUrlUtil.class.getCanonicalName());

    /**
     * Use of this method should be limited to
     * RoleAssigneeServiceBean.getRoleAssignee.
     *
     * @param identifier The identifier is expected to start with the PREFIX as
     * defined in this class and end with a number for a dataset,
     * ":privateUrlForDvObjectId42", for example.
     * @return A valid PrivateUrlUser (which is a RoleAssignee) if a valid
     * identifer is provided.
     */
    public static RoleAssignee identifier2roleAssignee(String identifier) {
        String[] parts = identifier.split(PREFIX);
        try {
            long datasetId = new Long(parts[1]);
            return new PrivateUrlUser(datasetId);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
            throw new IllegalArgumentException("Could not find dataset id in '" + identifier + "'");
        }
    }

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
                /**
                 * @todo Investigate why dataset.getGlobalId() yields the String
                 * "null:null/null" when I expect null value. This smells like a
                 * bug.
                 */
                if (!"null:null/null".equals(persistentId)) {
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

    static PrivateUrlUser getPrivateUrlUserFromRoleAssignment(RoleAssignment roleAssignment, RoleAssignee roleAssignee) {
        if (roleAssignment != null) {
            if (roleAssignee instanceof PrivateUrlUser) {
                return (PrivateUrlUser) roleAssignee;
            }
        }
        return null;
    }

}
