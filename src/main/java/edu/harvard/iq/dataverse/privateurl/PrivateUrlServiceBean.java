package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.RoleAssigneeServiceBean;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser;
import edu.harvard.iq.dataverse.util.SystemConfig;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * PrivateUrlServiceBean depends on Glassfish and Postgres being available and
 * it is tested with API tests in DatasetIT. Code that can execute without any
 * runtime dependencies should be put in PrivateUrlUtil so it can be unit
 * tested.
 */
@Stateless
@Named
public class PrivateUrlServiceBean implements Serializable {

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;

    @EJB
    DatasetServiceBean datasetServiceBean;
    @EJB
    RoleAssigneeServiceBean roleAssigneeService;

    @EJB
    SystemConfig systemConfig;

    public PrivateUrl getPrivateUrl(Long datasetId) {
        RoleAssignment roleAssignment = getPrivateUrlRoleAssignment(datasetServiceBean.find(datasetId));
        return PrivateUrlUtil.getPrivateUrlFromRoleAssignment(roleAssignment, systemConfig.getDataverseSiteUrl());
    }

    /**
     * @param dataset A non-null dataset;
     * @return A role assignment for a Private URL, if found, or null.
     *
     * @todo This might be a good place for Optional.
     */
    public RoleAssignment getPrivateUrlRoleAssignment(Dataset dataset) {
        TypedQuery<RoleAssignment> query = em.createNamedQuery(
                "RoleAssignment.listByAssigneeIdentifier_DefinitionPointId",
                RoleAssignment.class);
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(dataset.getId());
        query.setParameter("assigneeIdentifier", privateUrlUser.getIdentifier());
        query.setParameter("definitionPointId", dataset.getId());
        try {
            return query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException ex) {
            return null;
        }
    }

    /**
     * @return A RoleAssignment or null.
     *
     * @todo This might be a good place for Optional.
     */
    private RoleAssignment getRoleAssignmentFromPrivateUrlToken(String privateUrlToken) {
        if (privateUrlToken == null) {
            return null;
        }
        TypedQuery<RoleAssignment> query = em.createNamedQuery(
                "RoleAssignment.listByPrivateUrlToken",
                RoleAssignment.class);
        query.setParameter("privateUrlToken", privateUrlToken);
        try {
            RoleAssignment roleAssignment = query.getSingleResult();
            return roleAssignment;
        } catch (NoResultException | NonUniqueResultException ex) {
            return null;
        }
    }

    public PrivateUrlUser getUserFromPrivateUrlToken(String requestApiKey) {
        RoleAssignment roleAssignment = getRoleAssignmentFromPrivateUrlToken(requestApiKey);
        if (roleAssignment == null) {
            return null;
        }
        RoleAssignee roleAssignee = roleAssigneeService.getRoleAssignee(roleAssignment.getAssigneeIdentifier());
        return PrivateUrlUtil.getPrivateUrlUserFromRoleAssignment(roleAssignment, roleAssignee);
    }

    public DatasetVersion getDraftDatasetVersionFromPrivateUrlToken(String token) {
        return PrivateUrlUtil.getDraftDatasetVersionFromRoleAssignment(getRoleAssignmentFromPrivateUrlToken(token));
    }

    public PrivateUrlRedirectData getPrivateUrlRedirectData(String token) throws Exception {
        return PrivateUrlUtil.getPrivateUrlRedirectData(getRoleAssignmentFromPrivateUrlToken(token));
    }

}
