package edu.harvard.iq.dataverse.authorization.users;

import edu.harvard.iq.dataverse.authorization.RoleAssigneeDisplayInfo;
import edu.harvard.iq.dataverse.util.BundleUtil;

public class GuestOfDataset implements User {

    private final long datasetId;
    public static final String identifierPrefix = ":guestOfDataset";

    public GuestOfDataset(long datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * By returning false here, by defining this user as non-authenticated, we
     * prevent a name from appearing in the corner as well as preventing an
     * account page and MyData from being accessible. The user can still
     * navigate to the home page but can only see published datasets.
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
        /**
         * @todo Internationalize this? Does it matter?
         */
        return identifierPrefix + datasetId;
    }

    @Override
    public RoleAssigneeDisplayInfo getDisplayInfo() {
        String title = BundleUtil.getStringFromBundle("dataset.privateurl.roleassigeeTitle");
        return new RoleAssigneeDisplayInfo(title, null);
    }

}
