package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser;
import edu.harvard.iq.dataverse.util.SystemConfig;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ViewScoped
@Named("PrivateUrlPage")
public class PrivateUrlPage implements Serializable {

    private static final Logger logger = Logger.getLogger(PrivateUrlPage.class.getCanonicalName());

    @EJB
    DatasetServiceBean datasetService;
    @EJB
    SystemConfig systemConfig;
    @Inject
    DataverseSession session;

    /**
     * The unique string used to look up a user and automatically log that user
     * in.
     */
    String token;

    public String init() {
        PrivateUrlUser privateUrlUser = datasetService.getUserFromPrivateUrlToken(token);
        if (privateUrlUser != null) {
            session.setUser(privateUrlUser);
            DatasetVersion draft = datasetService.getDraftDatasetVersionFromPrivateUrlToken(token);
            if (draft != null) {
                Dataset dataset = draft.getDataset();
                if (dataset != null) {
                    String persistentId = dataset.getGlobalId();
                    if (persistentId != null) {
                        String relativeUrl = "/dataset.xhtml?persistentId=" + persistentId + "&version=DRAFT" + "&faces-redirect=true";
                        logger.fine("Redirecting " + privateUrlUser.getIdentifier() + " to " + relativeUrl);
                        return relativeUrl;
                    }
                }
            }
        }
        logger.info("Not redirecting. Couldn't find draft dataset version based on token: " + token);
        return "/404.xhtml";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
