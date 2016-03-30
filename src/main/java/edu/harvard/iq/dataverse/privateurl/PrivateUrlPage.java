package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.util.SystemConfig;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
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

    public void init() {
        AuthenticatedUser au = datasetService.getUserFromPrivateUrlToken(token);
        if (au != null) {
            session.setUser(au);
        }
        DatasetVersion dsv = datasetService.getDraftDatasetVersionFromAnonLinkToken(token);
        if (dsv != null) {
            logger.info("redirecting to dataset");
            String url = systemConfig.getDataverseSiteUrl() + "/dataset.xhtml?persistentId=" + dsv.getDataset().getGlobalId() + "&version=DRAFT" + "&faces-redirect=true";
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(url);
            } catch (IOException ex) {
                logger.info("Couldn't redirect: " + ex);
            }
        } else {
            logger.info("Not redirecting. Couldn't find dataset based on token: " + token);
        }

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
