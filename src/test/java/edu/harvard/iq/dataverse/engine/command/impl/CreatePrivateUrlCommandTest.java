package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.DataverseRoleServiceBean;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.DataverseRole;
import edu.harvard.iq.dataverse.engine.TestCommandContext;
import edu.harvard.iq.dataverse.engine.TestDataverseEngine;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.exception.IllegalCommandException;
import edu.harvard.iq.dataverse.privateurl.PrivateUrl;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class CreatePrivateUrlCommandTest {

    private TestDataverseEngine testEngine;
    private Dataset dataset;
    private final Long privateUrlAlreadyExists = 1l;
    private final Long latestVersionIsNotDraft = 2l;
    private final Long createDatasetLong = 3l;

    @Before
    public void setUp() {
        dataset = new Dataset();
        testEngine = new TestDataverseEngine(new TestCommandContext() {
            @Override
            public DatasetServiceBean datasets() {
                return new DatasetServiceBean() {

                    @Override
                    public PrivateUrl getPrivateUrl(Long datasetId) {
                        if (datasetId.equals(privateUrlAlreadyExists)) {
                            Dataset dataset = new Dataset();
                            dataset.setId(privateUrlAlreadyExists);
                            String token = null;
                            return new PrivateUrl(dataset, token);
                        } else if (datasetId.equals(latestVersionIsNotDraft)) {
                            return null;
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public PrivateUrl createPrivateUrl(Dataset dataset, String token, RoleAssignment roleAssignment, CreatePrivateUrlCommand command) throws IllegalCommandException {
                        return new PrivateUrl(dataset, token);
                    }

                };
            }

            @Override
            public DataverseRoleServiceBean roles() {
                return new DataverseRoleServiceBean() {

                    @Override
                    public DataverseRole findBuiltinRoleByAlias(String alias) {
                        return new DataverseRole();
                    }

                    @Override
                    public RoleAssignment save(RoleAssignment assignment) {
                        // no-op
                        return assignment;
                    }

                };
            }

        });
    }

    @Test
    public void testDatasetNull() {
        dataset = null;
        String expected = "Can't create Private URL. Dataset is null.";
        String actual = null;
        PrivateUrl privateUrl = null;
        try {
            privateUrl = testEngine.submit(new CreatePrivateUrlCommand(null, dataset));
        } catch (CommandException ex) {
            actual = ex.getMessage();
        }
        assertEquals(expected, actual);
        assertNull(privateUrl);
    }

    @Test
    public void testAlreadyExists() {
        dataset.setId(privateUrlAlreadyExists);
        String expected = "Private URL already exists with id null for dataset id " + privateUrlAlreadyExists + ".";
        String actual = null;
        PrivateUrl privateUrl = null;
        try {
            privateUrl = testEngine.submit(new CreatePrivateUrlCommand(null, dataset));
        } catch (CommandException ex) {
            actual = ex.getMessage();
        }
        assertEquals(expected, actual);
        assertNull(privateUrl);
    }

    @Test
    public void testAttemptCreatePrivateUrlOnNonDraft() {
        dataset = new Dataset();
        List<DatasetVersion> versions = new ArrayList<>();
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setVersionState(DatasetVersion.VersionState.RELEASED);
        versions.add(datasetVersion);
        dataset.setVersions(versions);
        dataset.setId(latestVersionIsNotDraft);
        String expected = "Can't create Private URL because the latest version of dataset id " + latestVersionIsNotDraft + " is not a draft.";
        String actual = null;
        PrivateUrl privateUrl = null;
        try {
            privateUrl = testEngine.submit(new CreatePrivateUrlCommand(null, dataset));
        } catch (CommandException ex) {
            actual = ex.getMessage();
        }
        assertEquals(expected, actual);
        assertNull(privateUrl);
    }

    @Test
    public void testdfdsfAttemptCreatePrivateUrlOnNonDraft() throws CommandException {
        dataset = new Dataset();
        dataset.setId(createDatasetLong);
        PrivateUrl privateUrl = testEngine.submit(new CreatePrivateUrlCommand(null, dataset));
        assertNotNull(privateUrl);
    }

}
