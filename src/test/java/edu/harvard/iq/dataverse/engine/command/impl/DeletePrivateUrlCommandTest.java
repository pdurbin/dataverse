package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DataverseRoleServiceBean;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import edu.harvard.iq.dataverse.authorization.users.GuestOfDataset;
import edu.harvard.iq.dataverse.engine.TestCommandContext;
import edu.harvard.iq.dataverse.engine.TestDataverseEngine;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.privateurl.PrivateUrl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DeletePrivateUrlCommandTest {

    private TestDataverseEngine testEngine;
    Dataset dataset;
    private final Long noPrivateUrlToDelete = 1l;
    private final Long hasPrivateUrlToDelete = 2l;

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
                        } else if (datasetId.equals(hasPrivateUrlToDelete)) {
                            Dataset dataset = new Dataset();
                            dataset.setId(hasPrivateUrlToDelete);
                            String token = null;
                            GuestOfDataset guestOfDataset = new GuestOfDataset(datasetId);
                            RoleAssignment roleAssignment = new RoleAssignment(null, guestOfDataset, dataset, token);
                            return new PrivateUrl(roleAssignment, dataset, "FIXME");
                        } else {
                            return null;
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
    public void testSuccessfulDelete() {
        dataset = new Dataset();
        dataset.setId(hasPrivateUrlToDelete);
        String actual = null;
        Dataset datasetAfterCommand = null;
        try {
            datasetAfterCommand = testEngine.submit(new DeletePrivateUrlCommand(null, dataset));
        } catch (CommandException ex) {
            actual = ex.getMessage();
        }
        assertNull(actual);
        assertNotNull(datasetAfterCommand);
        /**
         * @todo How would we confirm that the role assignement is actually
         * gone? Really all we're testing above is that there was no
         * IllegalCommandException from submitting the command.
         */
    }

}
