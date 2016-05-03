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
        if (roleAssignment != null) {
            RoleAssignee roleAssignee = roleAssigneeService.getRoleAssignee(roleAssignment.getAssigneeIdentifier());
            if (roleAssignee instanceof PrivateUrlUser) {
                return (PrivateUrlUser) roleAssignee;
            }
        }
        return null;
    }

    public DatasetVersion getDraftDatasetVersionFromPrivateUrlToken(String token) {
        return PrivateUrlUtil.getDraftDatasetVersionFromRoleAssignment(getRoleAssignmentFromPrivateUrlToken(token));
    }

    public PrivateUrlRedirectData getPrivateUrlRedirectData(String token) throws Exception {
        return PrivateUrlUtil.getPrivateUrlRedirectData(getRoleAssignmentFromPrivateUrlToken(token));
    }

}
