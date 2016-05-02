package edu.harvard.iq.dataverse.authorization.users;

import edu.harvard.iq.dataverse.authorization.RoleAssigneeDisplayInfo;
import edu.harvard.iq.dataverse.util.BundleUtil;

/**
 * A PrivateUrlUser is virtual in the sense that it does not have a row in the
 * authenticateduser table. It exists so when a Private URL is enabled for a
 * dataset, we can assign a read-only role ("member") to the identifier for the
 * PrivateUrlUser. (We will make no attempt to internationalize the identifier,
 * which is stored in the roleassignment table.)
 */
public class PrivateUrlUser implements User {

    private final long datasetId;
    public static final String PREFIX = ":privateUrlForDvObjectId";

    public PrivateUrlUser(long datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * @return By always returning false for isAuthenticated(), we prevent a
     * name from appearing in the corner as well as preventing an account page
     * and MyData from being accessible. The user can still navigate to the home
     * page but can only see published datasets.
     *
     * @todo Consider casting the user to Guest when navigating to the home
     * page.
     */
    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public boolean isBuiltInUser() {
        return false;
    }

    @Override
    public boolean isSuperuser() {
        return false;
    }

    @Override
    public String getIdentifier() {
        return PREFIX + datasetId;
    }

    @Override
    public RoleAssigneeDisplayInfo getDisplayInfo() {
        String title = BundleUtil.getStringFromBundle("dataset.privateurl.roleassigeeTitle");
        return new RoleAssigneeDisplayInfo(title, null);
    }

}
