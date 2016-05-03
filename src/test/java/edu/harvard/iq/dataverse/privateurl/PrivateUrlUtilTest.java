package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.DataverseRole;
import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;

public class PrivateUrlUtilTest {

    @Before
    public void setUp() {
        new PrivateUrlUtil();
    }

    @Test
    public void testGetDatasetFromRoleAssignmentNullRoleAssignment() {
        assertNull(PrivateUrlUtil.getDatasetFromRoleAssignment(null));
    }

    @Test
    public void testGetDatasetFromRoleAssignmentNullDefinitionPoint() {
        DataverseRole aRole = null;
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(42);
        RoleAssignee anAssignee = privateUrlUser;
        DvObject nullDefinitionPoint = null;
        String privateUrlToken = null;
        RoleAssignment ra = new RoleAssignment(aRole, anAssignee, nullDefinitionPoint, privateUrlToken);
        assertNull(PrivateUrlUtil.getDatasetFromRoleAssignment(ra));
    }

    @Test
    public void testGetDatasetFromRoleAssignmentNonDataset() {
        DataverseRole aRole = null;
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(42);
        RoleAssignee anAssignee = privateUrlUser;
        DvObject nonDataset = new Dataverse();
        String privateUrlToken = null;
        RoleAssignment ra = new RoleAssignment(aRole, anAssignee, nonDataset, privateUrlToken);
        assertNull(PrivateUrlUtil.getDatasetFromRoleAssignment(ra));
    }

    @Test
    public void testGetDatasetFromRoleAssignmentSuccess() {
        DataverseRole aRole = null;
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(42);
        RoleAssignee anAssignee = privateUrlUser;
        DvObject dataset = new Dataset();
        String privateUrlToken = null;
        RoleAssignment ra = new RoleAssignment(aRole, anAssignee, dataset, privateUrlToken);
        assertNotNull(PrivateUrlUtil.getDatasetFromRoleAssignment(ra));
        assertEquals(":privateUrlForDvObjectId42", ra.getAssigneeIdentifier());
    }

    @Test
    public void testGetDraftDatasetVersionFromRoleAssignmentNullRoleAssignement() {
        assertNull(PrivateUrlUtil.getDraftDatasetVersionFromRoleAssignment(null));
    }

    @Test
    public void testGetDraftDatasetVersionFromRoleAssignmentNullDataset() {
        DataverseRole aRole = null;
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(42);
        RoleAssignee anAssignee = privateUrlUser;
        DvObject dataset = null;
        String privateUrlToken = null;
        RoleAssignment ra = new RoleAssignment(aRole, anAssignee, dataset, privateUrlToken);
        DatasetVersion datasetVersion = PrivateUrlUtil.getDraftDatasetVersionFromRoleAssignment(ra);
        assertNull(datasetVersion);
    }

    @Test
    public void testGetDraftDatasetVersionFromRoleAssignmentLastestIsNotDraft() {
        DataverseRole aRole = null;
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(42);
        RoleAssignee anAssignee = privateUrlUser;
        Dataset dataset = new Dataset();
        List<DatasetVersion> versions = new ArrayList<>();
        DatasetVersion datasetVersionIn = new DatasetVersion();
        datasetVersionIn.setVersionState(DatasetVersion.VersionState.RELEASED);
        versions.add(datasetVersionIn);
        dataset.setVersions(versions);
        String privateUrlToken = null;
        RoleAssignment ra = new RoleAssignment(aRole, anAssignee, dataset, privateUrlToken);
        DatasetVersion datasetVersionOut = PrivateUrlUtil.getDraftDatasetVersionFromRoleAssignment(ra);
        assertNull(datasetVersionOut);
    }

    @Test
    public void testGetDraftDatasetVersionFromRoleAssignmentSuccess() {
        DataverseRole aRole = null;
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(42);
        RoleAssignee anAssignee = privateUrlUser;
        Dataset dataset = new Dataset();
        List<DatasetVersion> versions = new ArrayList<>();
        DatasetVersion datasetVersionIn = new DatasetVersion();
        datasetVersionIn.setVersionState(DatasetVersion.VersionState.DRAFT);
        versions.add(datasetVersionIn);
        dataset.setVersions(versions);
        String privateUrlToken = null;
        RoleAssignment ra = new RoleAssignment(aRole, anAssignee, dataset, privateUrlToken);
        DatasetVersion datasetVersionOut = PrivateUrlUtil.getDraftDatasetVersionFromRoleAssignment(ra);
        assertNotNull(datasetVersionOut);
        assertEquals(":privateUrlForDvObjectId42", ra.getAssigneeIdentifier());
    }

    @Test
    public void testGetUserFromRoleAssignmentNull() {
        assertNull(PrivateUrlUtil.getPrivateUrlUserFromRoleAssignment(null));
    }

    @Test
    public void testGetUserFromRoleAssignmentNonDataset() {
        DataverseRole aRole = null;
        PrivateUrlUser privateUrlUserIn = new PrivateUrlUser(42);
        RoleAssignee anAssignee = privateUrlUserIn;
        DvObject nonDataset = new Dataverse();
        nonDataset.setId(123l);
        String privateUrlToken = null;
        RoleAssignment ra = new RoleAssignment(aRole, anAssignee, nonDataset, privateUrlToken);
        PrivateUrlUser privateUrlUserOut = PrivateUrlUtil.getPrivateUrlUserFromRoleAssignment(ra);
        assertNull(privateUrlUserOut);
    }

    @Test
    public void testGetUserFromRoleAssignmentSucess() {
        DataverseRole aRole = null;
        PrivateUrlUser privateUrlUserIn = new PrivateUrlUser(42);
        RoleAssignee anAssignee = privateUrlUserIn;
        DvObject dataset = new Dataset();
        dataset.setId(123l);
        String privateUrlToken = null;
        RoleAssignment ra = new RoleAssignment(aRole, anAssignee, dataset, privateUrlToken);
        PrivateUrlUser privateUrlUserOut = PrivateUrlUtil.getPrivateUrlUserFromRoleAssignment(ra);
        assertNotNull(privateUrlUserOut);
    }

}
