package edu.harvard.iq.dataverse.api.datadeposit;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.EjbDataverseEngine;
import edu.harvard.iq.dataverse.PermissionServiceBean;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.impl.ListDataverseContentCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.swordapp.server.AuthCredentials;
import org.swordapp.server.CollectionListManager;
import org.swordapp.server.SwordAuthException;
import org.swordapp.server.SwordConfiguration;
import org.swordapp.server.SwordError;
import org.swordapp.server.SwordServerException;
import org.swordapp.server.UriRegistry;

public class CollectionListManagerImpl implements CollectionListManager {

    private static final Logger logger = Logger.getLogger(CollectionListManagerImpl.class.getCanonicalName());
    @EJB
    DataverseServiceBean dataverseService;
    @EJB
    DatasetServiceBean datasetService;
    @EJB
    EjbDataverseEngine commandEngine;
    @EJB
    PermissionServiceBean permissionService;
    @Inject
    SwordAuth swordAuth;
    @Inject
    UrlManager urlManager;

    private HttpServletRequest request;

    @Override
    public Feed listCollectionContents(IRI iri, AuthCredentials authCredentials, SwordConfiguration swordConfiguration) throws SwordServerException, SwordAuthException, SwordError {
        AuthenticatedUser user = swordAuth.auth(authCredentials);
        DataverseRequest dvReq = new DataverseRequest(user, request);
        urlManager.processUrl(iri.toString());
        String dvAlias = urlManager.getTargetIdentifier();
        if (urlManager.getTargetType().equals("dataverse") && dvAlias != null) {

            Dataverse dv = dataverseService.findByAlias(dvAlias);

            if (dv != null) {
                if (!permissionService.isUserAllowedOn(user, new ListDataverseContentCommand(dvReq, dv), dv)) {
                    throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "user " + user.getDisplayInfo().getTitle() + " is not authorized to list datasets in dataverse " + dv.getAlias());
                }
                if (swordAuth.hasAccessToModifyDataverse(dvReq, dv)) {
                    Abdera abdera = new Abdera();
                    Feed feed = abdera.newFeed();
                    feed.setTitle(dv.getName());
                    /**
                     * @todo Get rid of these findByOwnerId calls here and in
                     * other places they are used such as DataversePage.java and
                     * SearchIncludeFragment.java. Use the permissions system
                     * instead.
                     */
                    List childDvObjects = dataverseService.findByOwnerId(dv.getId());
                    childDvObjects.addAll(datasetService.findByOwnerId(dv.getId()));
                    /**
                     * @todo Since we are changing the implementation (using
                     * ListDataverseContentCommand), should we add a feature
                     * flag to revert to the old behavior if necessary? Maybe!
                     */
                    if (SwordAuth.experimentalSwordAuthPermChangeForIssue1070Enabled) {
                        try {
                            /**
                             * ListDataverseContentCommand gives us more than we
                             * need since it returns both dataverses and
                             * datasets that are direct children of a dataverse.
                             *
                             * @todo Write a new ListDirectChildDatasetsCommand
                             * or similar? All we want are direct dataset
                             * children since that's how we first implemented it
                             * in DVN 3.x (where there was no tree of
                             * dataverses).
                             */
                            childDvObjects = commandEngine.submit(new ListDataverseContentCommand(dvReq, dv));
                        } catch (CommandException ex) {
                            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "user " + user.getDisplayInfo().getTitle() + " is not authorized to list datasets in dataverse " + dv.getAlias());
                        }
                    }
                    List<Dataset> datasets = new ArrayList<>();
                    for (Object object : childDvObjects) {
                        if (object instanceof Dataset) {
                            datasets.add((Dataset) object);
                        }
                    }
                    String baseUrl = urlManager.getHostnamePlusBaseUrlPath(iri.toString());
                    for (Dataset dataset : datasets) {
                        String editUri = baseUrl + "/edit/study/" + dataset.getGlobalId();
                        String editMediaUri = baseUrl + "/edit-media/study/" + dataset.getGlobalId();
                        Entry entry = feed.addEntry();
                        entry.setId(editUri);
                        entry.setTitle(datasetService.getTitleFromLatestVersion(dataset.getId()));
                        entry.setBaseUri(new IRI(editUri));
                        entry.addLink(editMediaUri, "edit-media");
                        feed.addEntry(entry);
                    }
                    Boolean dvHasBeenReleased = dv.isReleased();
                    feed.addSimpleExtension(new QName(UriRegistry.SWORD_STATE, "dataverseHasBeenReleased"), dvHasBeenReleased.toString());
                    return feed;
                } else {
                    throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "user " + user.getDisplayInfo().getTitle() + " is not authorized to list datasets in dataverse " + dv.getAlias());
                }

            } else {
                throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Could not find dataverse: " + dvAlias);
            }
        } else {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Couldn't determine target type or identifer from URL: " + iri);
        }
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

}
