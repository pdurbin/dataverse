package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser;

public class PrivateUrlRedirectData {

    private final PrivateUrlUser privateUrlUser;
    private final String draftDatasetPageToBeRedirectedTo;

    public PrivateUrlRedirectData(PrivateUrlUser privateUrlUser, String draftDatasetPageToBeRedirectedTo) throws Exception {
        if (privateUrlUser == null) {
            throw new Exception("PrivateUrlUser cannot be null");
        }
        if (draftDatasetPageToBeRedirectedTo == null) {
            throw new Exception("draftDatasetPageToBeRedirectedTo cannot be null");
        }
        this.privateUrlUser = privateUrlUser;
        this.draftDatasetPageToBeRedirectedTo = draftDatasetPageToBeRedirectedTo;
    }

    public PrivateUrlUser getPrivateUrlUser() {
        return privateUrlUser;
    }

    public String getDraftDatasetPageToBeRedirectedTo() {
        return draftDatasetPageToBeRedirectedTo;
    }

}
