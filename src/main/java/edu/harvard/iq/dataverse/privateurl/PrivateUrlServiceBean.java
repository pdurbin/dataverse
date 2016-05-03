package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
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
    RoleAssigneeServiceBean roleAssigneeService;

    @EJB
    SystemConfig systemConfig;

    public PrivateUrl getPrivateUrl(Long datasetId) {
        if (datasetId == null) {
            return null;
        }
        TypedQuery<RoleAssignment> query = em.createNamedQuery(
                "RoleAssignment.listByAssigneeIdentifier_DefinitionPointId",
                RoleAssignment.class);
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(datasetId);
        String identifier = privateUrlUser.getIdentifier();
        query.setParameter("assigneeIdentifier", identifier);
        query.setParameter("definitionPointId", datasetId);
        RoleAssignment roleAssignment;
        try {
            roleAssignment = query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException ex) {
            return null;
        }
        if (roleAssignment == null) {
            return null;
        }
        Dataset dataset = PrivateUrlUtil.getDatasetFromRoleAssignment(roleAssignment);
        if (dataset != null) {
            PrivateUrl privateUrl = new PrivateUrl(roleAssignment, dataset, systemConfig.getDataverseSiteUrl());
            return privateUrl;
        } else {
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
