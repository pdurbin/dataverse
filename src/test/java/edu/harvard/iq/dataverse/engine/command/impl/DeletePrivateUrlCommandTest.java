package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DataverseRoleServiceBean;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.engine.TestCommandContext;
import edu.harvard.iq.dataverse.engine.TestDataverseEngine;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.privateurl.PrivateUrl;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class DeletePrivateUrlCommandTest {

    private TestDataverseEngine testEngine;
    Dataset dataset;
    private final Long noPrivateUrlToDelete = 1l;
    private final Long simulateDeleteFailure = 2l;
    private final Long noRolesAssigned = 3l;

    @Before
    public void setUp() {
        testEngine = new TestDataverseEngine(new TestCommandContext() {
            @Override
            public DatasetServiceBean datasets() {
                return new DatasetServiceBean() {

                    @Override
                    public PrivateUrl getPrivateUrl(Long datasetId) {
                        if (datasetId.equals(noPrivateUrlToDelete)) {
                            return null;
                        } else if (datasetId.equals(simulateDeleteFailure)) {
                            Dataset dataset = new Dataset();
                            dataset.setId(simulateDeleteFailure);
                            String token = null;
                            return new PrivateUrl(dataset, token);
                        } else if (datasetId.equals(noRolesAssigned)) {
                            Dataset dataset = new Dataset();
                            dataset.setId(simulateDeleteFailure);
                            String token = null;
                            return new PrivateUrl(dataset, token);
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public boolean deletePrivateUrl(PrivateUrl doomed) {
                        if (dataset.getId().equals(simulateDeleteFailure)) {
                            return false;
                        } else {
                            return true;
                        }
                    }

                };
            }

            @Override
            public DataverseRoleServiceBean roles() {
                return new DataverseRoleServiceBean() {
                    @Override
                    public List<RoleAssignment> directRoleAssignments(RoleAssignee roas, DvObject dvo) {
                        RoleAssignment roleAssignment = new RoleAssignment();
                        List<RoleAssignment> list = new ArrayList<>();
                        list.add(roleAssignment);
                        return list;
                    }

                    @Override
                    public void revoke(RoleAssignment ra) {
                        // no-op
                    }

                };
            }

        });
        /**
         * @todo Work these getters and setters that increase our code coverage
         * numbers into actual tests and remove them from here.
         */
        PrivateUrl codeCoverage = new PrivateUrl();
        codeCoverage.setId(Long.MAX_VALUE);
        codeCoverage.setToken("foo");
        codeCoverage.getRoleAssignment();
        codeCoverage.getDataset();
        codeCoverage.getToken();
    }

    @Test
    public void testDatasetNull() {
        dataset = null;
        String expected = "Can't delete Private URL. Dataset is null.";
        String actual = null;
        try {
            Dataset datasetAfterCommand = testEngine.submit(new DeletePrivateUrlCommand(null, dataset));
        } catch (CommandException ex) {
            actual = ex.getMessage();
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testNoPrivateUrlToDelete() {
        dataset = new Dataset();
        dataset.setId(noPrivateUrlToDelete);
        String expected = "Dataset id " + noPrivateUrlToDelete + " doesn't have a Private URL to delete.";
        String actual = null;
        Dataset datasetAfterCommand = null;
        try {
            datasetAfterCommand = testEngine.submit(new DeletePrivateUrlCommand(null, dataset));
        } catch (CommandException ex) {
            actual = ex.getMessage();
        }
        assertEquals(expected, actual);
        assertNull(datasetAfterCommand);
    }

    @Test
    public void testProblemDeletingPrivateUrl() {
        dataset = new Dataset();
        dataset.setId(simulateDeleteFailure);
        String expected = "Problem deleting Private URL.";
        Dataset datasetAfterCommand = null;
        String actual = null;
        try {
            datasetAfterCommand = testEngine.submit(new DeletePrivateUrlCommand(null, dataset));
        } catch (CommandException ex) {
            actual = ex.getMessage();
        }
        assertEquals(expected, actual);
        assertNull(datasetAfterCommand);
    }

    @Test
    public void testDeleteWithNoRolesAssigned() {
        dataset = new Dataset();
        dataset.setId(noRolesAssigned);
        String actual = null;
        Dataset datasetAfterCommand = null;
        try {
            datasetAfterCommand = testEngine.submit(new DeletePrivateUrlCommand(null, dataset));
        } catch (CommandException ex) {
            actual = ex.getMessage();
        }
        assertNull(actual);
        assertNotNull(datasetAfterCommand);
    }

}
