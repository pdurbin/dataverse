package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import edu.harvard.iq.dataverse.api.datadeposit.SwordAuth;
import edu.harvard.iq.dataverse.api.datadeposit.SwordConfigurationImpl;
import java.util.List;
import java.util.logging.Logger;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class SwordIT {

    private static final Logger logger = Logger.getLogger(SwordIT.class.getCanonicalName());
    private static String superuser;

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
        Response createUser = UtilIT.createRandomUser();
        superuser = UtilIT.getUsernameFromResponse(createUser);
        String apitoken = UtilIT.getApiTokenFromResponse(createUser);
        UtilIT.makeSuperUser(superuser).then().assertThat().statusCode(OK.getStatusCode());
        Response checkRootDataverse = UtilIT.listDatasetsViaSword(rootDataverseAlias, apitoken);
        checkRootDataverse.prettyPrint();
        checkRootDataverse.then().assertThat()
                .statusCode(OK.getStatusCode());
        boolean rootDataverseHasBeenReleased = checkRootDataverse.getBody().xmlPath().getBoolean("feed.dataverseHasBeenReleased");
        if (!rootDataverseHasBeenReleased) {
            logger.info("Many of these SWORD tests require that the root dataverse has been published. Publish the root dataverse and then re-run these tests.");
            System.exit(666);
        }

    }

    private static final String rootDataverseAlias = "root";

    @Test
    public void testServiceDocument() {
        Response createUser = UtilIT.createRandomUser();

        String username = UtilIT.getUsernameFromResponse(createUser);
        String apitoken = UtilIT.getApiTokenFromResponse(createUser);

        Response serviceDocumentResponse = UtilIT.getServiceDocument(apitoken);
        serviceDocumentResponse.prettyPrint();

        SwordConfigurationImpl swordConfiguration = new SwordConfigurationImpl();

        serviceDocumentResponse.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("service.version", equalTo(swordConfiguration.generatorVersion()));

        Response deleteUser1Response = UtilIT.deleteUser(username);
        deleteUser1Response.prettyPrint();
        boolean issue2825Resolved = false;
        if (issue2825Resolved) {
            /**
             * We can't delete this user because in some cases the user has
             * released the root dataverse:
             * https://github.com/IQSS/dataverse/issues/2825
             */
            assertEquals(200, deleteUser1Response.getStatusCode());
        }
        UtilIT.deleteUser(username);

    }

    @Test
    public void testServiceDocumentWithInvalidApiToken() {

        Response serviceDocumentResponse = UtilIT.getServiceDocument("invalidApiToken");
//        serviceDocumentResponse.prettyPrint();

        serviceDocumentResponse.then().assertThat()
                .statusCode(FORBIDDEN.getStatusCode());
    }

    @Test
    public void testCreateDataverseCreateDatasetUploadFileDownloadFileEditTitle() {

        Response createUser = UtilIT.createRandomUser();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);

        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
        createDataverseResponse.prettyPrint();
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);
        assertEquals(CREATED.getStatusCode(), createDataverseResponse.getStatusCode());

        String initialDatasetTitle = "My Working Title";

        Response randomUnprivUser = UtilIT.createRandomUser();
        String apiTokenNoPrivs = UtilIT.getApiTokenFromResponse(randomUnprivUser);
        String usernameNoPrivs = UtilIT.getUsernameFromResponse(randomUnprivUser);

        Response createDatasetShouldFail = UtilIT.createDatasetViaSwordApi(dataverseAlias, initialDatasetTitle, apiTokenNoPrivs);
        createDatasetShouldFail.prettyPrint();
        createDatasetShouldFail.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode());
        String createDatasetError = createDatasetShouldFail.getBody().xmlPath().get("error.summary");
//        assertTrue(createDatasetError.endsWith(" is missing permissions [AddDataset] on Object " + dataverseAlias));
        assertTrue(createDatasetError.equals("user " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to create a dataset in this dataverse."));

        /**
         * "Clients MUST NOT require a Collection Feed Document for deposit
         * operation." -- 6.2 Listing Collections
         * http://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html#protocoloperations_listingcollections
         */
        Response createDatasetResponse = UtilIT.createDatasetViaSwordApi(dataverseAlias, initialDatasetTitle, apiToken);
        createDatasetResponse.prettyPrint();
        assertEquals(CREATED.getStatusCode(), createDatasetResponse.getStatusCode());
        String persistentId = UtilIT.getDatasetPersistentIdFromResponse(createDatasetResponse);
        logger.info("persistent id: " + persistentId);

        Response atomEntryUnAuth = UtilIT.getSwordAtomEntry(persistentId, apiTokenNoPrivs);
        atomEntryUnAuth.prettyPrint();
        atomEntryUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode())
                .body("error.summary", equalTo("User " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to retrieve entry for " + persistentId));

        Response atomEntry = UtilIT.getSwordAtomEntry(persistentId, apiToken);
        atomEntry.prettyPrint();
        atomEntry.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("entry.treatment", equalTo("no treatment information available"));

        Response listDatasetsUnAuth = UtilIT.listDatasetsViaSword(dataverseAlias, apiTokenNoPrivs);
        listDatasetsUnAuth.prettyPrint();
        listDatasetsUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode())
                .body("error.summary", equalTo("user " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to list datasets in dataverse " + dataverseAlias));

        Response listDatasetsResponse = UtilIT.listDatasetsViaSword(dataverseAlias, apiToken);
        listDatasetsResponse.prettyPrint();
        listDatasetsResponse.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("feed.dataverseHasBeenReleased", equalTo("false"))
                .body("feed.entry[0].title", equalTo(initialDatasetTitle));

        Response uploadFileUnAuth = UtilIT.uploadRandomFile(persistentId, apiTokenNoPrivs);
        uploadFileUnAuth.prettyPrint();
        uploadFileUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode());
        String uploadFileError = uploadFileUnAuth.getBody().xmlPath().get("error.summary");
        System.out.println("errro: " + uploadFileError);
        if (SwordAuth.experimentalSwordAuthPermChangeForIssue1070Enabled) {
            assertTrue(uploadFileError.endsWith(" is missing permissions [EditDataset] on Object " + initialDatasetTitle));
        } else {
            assertTrue(uploadFileError.equals("user " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to modify dataset with global ID " + persistentId));
        }

        Response uploadFile1 = UtilIT.uploadRandomFile(persistentId, apiToken);
        uploadFile1.prettyPrint();
        assertEquals(CREATED.getStatusCode(), uploadFile1.getStatusCode());

        Response swordStatementUnAuth = UtilIT.getSwordStatement(persistentId, apiTokenNoPrivs);
        swordStatementUnAuth.prettyPrint();
        swordStatementUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .body("error.summary", equalTo("user " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to view dataset with global ID " + persistentId))
                .statusCode(BAD_REQUEST.getStatusCode());

        Response swordStatement = UtilIT.getSwordStatement(persistentId, apiToken);
        swordStatement.prettyPrint();
        String title = UtilIT.getTitleFromSwordStatementResponse(swordStatement);
        assertEquals(initialDatasetTitle, title);
        Integer fileId = UtilIT.getFileIdFromSwordStatementResponse(swordStatement);
        assertNotNull(fileId);
        assertEquals(Integer.class, fileId.getClass());

        logger.info("Id of uploaded file: " + fileId);
        String filename = UtilIT.getFilenameFromSwordStatementResponse(swordStatement);
        assertNotNull(filename);
        assertEquals(String.class, filename.getClass());
        logger.info("Filename of uploaded file: " + filename);
        assertEquals("trees.png", filename);

        Response attemptToDownloadUnpublishedFileWithoutApiToken = UtilIT.downloadFile(fileId);
        attemptToDownloadUnpublishedFileWithoutApiToken.then().assertThat()
                .body("html.head.title", equalTo("GlassFish Server Open Source Edition  4.1  - Error report"))
                .statusCode(FORBIDDEN.getStatusCode());

        Response attemptToDownloadUnpublishedFileUnauthApiToken = UtilIT.downloadFile(fileId, apiTokenNoPrivs);
        attemptToDownloadUnpublishedFileUnauthApiToken.prettyPrint();
        attemptToDownloadUnpublishedFileUnauthApiToken.then().assertThat()
                .body("html.head.title", equalTo("GlassFish Server Open Source Edition  4.1  - Error report"))
                .statusCode(FORBIDDEN.getStatusCode());

        Response downloadUnpublishedFileWithValidApiToken = UtilIT.downloadFile(fileId, apiToken);
        assertEquals(OK.getStatusCode(), downloadUnpublishedFileWithValidApiToken.getStatusCode());
        logger.info("downloaded " + downloadUnpublishedFileWithValidApiToken.getContentType() + " (" + downloadUnpublishedFileWithValidApiToken.asByteArray().length + " bytes)");

        Response deleteFileUnAuth = UtilIT.deleteFile(fileId, apiTokenNoPrivs);
        deleteFileUnAuth.prettyPrint();
        deleteFileUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode());

        String deleteFileError = uploadFileUnAuth.getBody().xmlPath().get("error.summary");
        if (SwordAuth.experimentalSwordAuthPermChangeForIssue1070Enabled) {
            assertTrue(deleteFileError.endsWith(" is missing permissions [EditDataset] on Object " + initialDatasetTitle));
        } else {
            assertTrue(deleteFileError.equals("user " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to modify dataset with global ID " + persistentId));
        }

        Response deleteFile = UtilIT.deleteFile(fileId, apiToken);
        deleteFile.prettyPrint();
        deleteFile.then().assertThat()
                .statusCode(NO_CONTENT.getStatusCode());

        Response downloadDeletedFileWithValidApiToken = UtilIT.downloadFile(fileId, apiToken);
        assertEquals(NOT_FOUND.getStatusCode(), downloadDeletedFileWithValidApiToken.getStatusCode());

        String newTitle = "My Awesome Dataset";
        Response updatedMetadataUnAuth = UtilIT.updateDatasetTitleViaSword(persistentId, newTitle, apiTokenNoPrivs);
        updatedMetadataUnAuth.prettyPrint();
        updatedMetadataUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode())
                /**
                 * @todo This error doesn't make a ton of sense. Something like
                 * "not authorized to modify dataset metadata" would be better.
                 */
                .body("error.summary", equalTo("User " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to modify dataverse " + dataverseAlias));

        Response updatedMetadataResponse = UtilIT.updateDatasetTitleViaSword(persistentId, newTitle, apiToken);
        updatedMetadataResponse.prettyPrint();
        swordStatement = UtilIT.getSwordStatement(persistentId, apiToken);
        title = UtilIT.getTitleFromSwordStatementResponse(swordStatement);
        assertEquals(newTitle, title);
        logger.info("Title updated from \"" + initialDatasetTitle + "\" to \"" + newTitle + "\".");

        Response deleteDatasetUnAuth = UtilIT.deleteLatestDatasetVersionViaSwordApi(persistentId, apiTokenNoPrivs);
        deleteDatasetUnAuth.prettyPrint();
        deleteDatasetUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode());

        String deleteDatasetUnauth = deleteDatasetUnAuth.getBody().xmlPath().get("error.summary");
        if (SwordAuth.experimentalSwordAuthPermChangeForIssue1070Enabled) {
            assertTrue(deleteDatasetUnauth.endsWith(" is missing permissions [DeleteDatasetDraft] on Object " + newTitle));
        } else {
            assertTrue(deleteDatasetUnauth.equals("User " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to modify " + dataverseAlias));
        }

        Response deleteDatasetResponse = UtilIT.deleteLatestDatasetVersionViaSwordApi(persistentId, apiToken);
        deleteDatasetResponse.prettyPrint();
        assertEquals(204, deleteDatasetResponse.getStatusCode());

        Response deleteDataverse1Response = UtilIT.deleteDataverse(dataverseAlias, apiToken);
        deleteDataverse1Response.prettyPrint();
        assertEquals(200, deleteDataverse1Response.getStatusCode());

        UtilIT.deleteUser(username);
        UtilIT.deleteUser(usernameNoPrivs);

    }

    /**
     * This test requires the root dataverse to have been published already.
     */
    @Test
    public void testCreateDatasetPublishDestroy() {
        Response createUser = UtilIT.createRandomUser();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);

        Response createDataverse = UtilIT.createRandomDataverse(apiToken);
        createDataverse.prettyPrint();
        createDataverse.then().assertThat()
                .statusCode(CREATED.getStatusCode());
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverse);

        String datasetTitle = "Publish or Perist";
        Response createDataset = UtilIT.createDatasetViaSwordApi(dataverseAlias, datasetTitle, apiToken);
        createDataset.prettyPrint();
        createDataset.then().assertThat()
                .statusCode(CREATED.getStatusCode());
        String persistentId = UtilIT.getDatasetPersistentIdFromResponse(createDataset);

        Response attemptToPublishDatasetInUnpublishedDataverse = UtilIT.publishDatasetViaSword(persistentId, apiToken);
        attemptToPublishDatasetInUnpublishedDataverse.prettyPrint();
        attemptToPublishDatasetInUnpublishedDataverse.then().assertThat()
                .statusCode(BAD_REQUEST.getStatusCode());

        Response listDatasetsResponse = UtilIT.listDatasetsViaSword(rootDataverseAlias, apiToken);
        System.out.println("BEGIN");
        listDatasetsResponse.prettyPrint();
        System.out.println("END");
        if (SwordAuth.experimentalSwordAuthPermChangeForIssue1070Enabled) {
            listDatasetsResponse.then().assertThat()
                    .statusCode(OK.getStatusCode())
                    .body("feed.dataverseHasBeenReleased", equalTo("true"));
        } else {
            /**
             * See also SWORD: not authorized when dataset is in root dataverse
             * https://github.com/IQSS/dataverse/issues/2495 and "SWORD access
             * failed - not authorized" at
             * https://groups.google.com/d/msg/dataverse-community/G9TMVnhab5A/Z7fffd0fCgAJ
             */
            listDatasetsResponse.then().assertThat()
                    .statusCode(BAD_REQUEST.getStatusCode())
                    .body("error.summary", equalTo("user " + username + " " + username + " is not authorized to list datasets in dataverse " + rootDataverseAlias));
        }

        Response randomUnprivUser = UtilIT.createRandomUser();
        String apiTokenNoPrivs = UtilIT.getApiTokenFromResponse(randomUnprivUser);
        String usernameNoPrivs = UtilIT.getUsernameFromResponse(randomUnprivUser);

        Response publishDataverseUnAuth = UtilIT.publishDataverseViaSword(dataverseAlias, apiTokenNoPrivs);
        publishDataverseUnAuth.prettyPrint();
        publishDataverseUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode());
        String publishDataverseError = publishDataverseUnAuth.getBody().xmlPath().get("error.summary");
        if (SwordAuth.experimentalSwordAuthPermChangeForIssue1070Enabled) {
            assertTrue(publishDataverseError.endsWith(" is missing permissions [PublishDataverse] on Object " + dataverseAlias));
        } else {
            assertTrue(publishDataverseError.equals("User " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to modify dataverse " + dataverseAlias));
        }

        Response publishDataverse = UtilIT.publishDataverseViaSword(dataverseAlias, apiToken);
        publishDataverse.prettyPrint();
        publishDataverse.then().assertThat()
                .statusCode(OK.getStatusCode());

        Response publishDatsetUnAuth = UtilIT.publishDatasetViaSword(persistentId, apiTokenNoPrivs);
        System.out.println("BEGIN");
        publishDatsetUnAuth.prettyPrint();
        System.out.println("END");
        publishDatsetUnAuth.then().assertThat()
                /**
                 * @todo It would be nice if this could be UNAUTHORIZED or
                 * FORBIDDEN rather than BAD_REQUEST.
                 */
                .statusCode(BAD_REQUEST.getStatusCode());
        String publishDatasetError = publishDatsetUnAuth.getBody().xmlPath().get("error.summary");
        if (SwordAuth.experimentalSwordAuthPermChangeForIssue1070Enabled) {
            assertTrue(publishDatasetError.endsWith(" is missing permissions [PublishDataset] on Object " + datasetTitle));
        } else {
            assertTrue(publishDatasetError.equals("User " + usernameNoPrivs + " " + usernameNoPrivs + " is not authorized to modify dataverse " + dataverseAlias));
        }

        Response publishDataset = UtilIT.publishDatasetViaSword(persistentId, apiToken);
        publishDataset.prettyPrint();
        publishDataset.then().assertThat()
                .statusCode(OK.getStatusCode());

        Response attemptToDeletePublishedDataset = UtilIT.deleteLatestDatasetVersionViaSwordApi(persistentId, apiToken);
        attemptToDeletePublishedDataset.prettyPrint();
        attemptToDeletePublishedDataset.then().assertThat()
                .statusCode(METHOD_NOT_ALLOWED.getStatusCode());

        /**
         * @todo This can probably be removed now that
         * https://github.com/IQSS/dataverse/issues/1837 has been fixed.
         */
        Response reindexDatasetToFindDatabaseId = UtilIT.reindexDataset(persistentId);
        reindexDatasetToFindDatabaseId.prettyPrint();
        reindexDatasetToFindDatabaseId.then().assertThat()
                .statusCode(OK.getStatusCode());

        Integer datasetId = JsonPath.from(reindexDatasetToFindDatabaseId.asString()).getInt("data.id");

        /**
         * @todo The "destroy" endpoint should accept a persistentId:
         * https://github.com/IQSS/dataverse/issues/1837
         */
        Response makeSuperuserRespone = UtilIT.makeSuperUser(username);
        makeSuperuserRespone.then().assertThat()
                .statusCode(OK.getStatusCode());

        Response listDatasetsResponseAsRoot = UtilIT.listDatasetsViaSword(rootDataverseAlias, apiToken);
        /**
         * @todo Why is it that even a superuser can't see any datsets in the
         * root?
         */
        listDatasetsResponseAsRoot.prettyPrint();

        Response destroyDataset = UtilIT.destroyDataset(datasetId, apiToken);
        destroyDataset.prettyPrint();
        destroyDataset.then().assertThat()
                .statusCode(OK.getStatusCode());

        Response atomEntryDestroyed = UtilIT.getSwordAtomEntry(persistentId, apiToken);
        atomEntryDestroyed.prettyPrint();
        atomEntryDestroyed.then().statusCode(400);

        Response deleteDataverseResponse = UtilIT.deleteDataverse(dataverseAlias, apiToken);
        deleteDataverseResponse.prettyPrint();
        assertEquals(200, deleteDataverseResponse.getStatusCode());

        UtilIT.deleteUser(username);
        UtilIT.deleteUser(usernameNoPrivs);

    }

    /**
     * This test requires the root dataverse to have been published already.
     *
     * Test the following issues:
     *
     * - https://github.com/IQSS/dataverse/issues/1784
     *
     * - https://github.com/IQSS/dataverse/issues/2222
     *
     * - https://github.com/IQSS/dataverse/issues/2464
     */
    @Test
    public void testDeleteFiles() {
        Response createUser = UtilIT.createRandomUser();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);

        Response createDataverse = UtilIT.createRandomDataverse(apiToken);
        createDataverse.prettyPrint();
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverse);

        Response createDataset = UtilIT.createRandomDatasetViaSwordApi(dataverseAlias, apiToken);
        createDataset.prettyPrint();
        String datasetPersistentId = UtilIT.getDatasetPersistentIdFromResponse(createDataset);

        Response uploadZip = UtilIT.uploadFile(datasetPersistentId, "3files.zip", apiToken);
        uploadZip.prettyPrint();
        assertEquals(CREATED.getStatusCode(), uploadZip.getStatusCode());
        Response statement1 = UtilIT.getSwordStatement(datasetPersistentId, apiToken);
        statement1.prettyPrint();
        String index0a = statement1.getBody().xmlPath().get("feed.entry[0].id").toString().split("/")[10];
        String index1a = statement1.getBody().xmlPath().get("feed.entry[1].id").toString().split("/")[10];
        String index2a = statement1.getBody().xmlPath().get("feed.entry[2].id").toString().split("/")[10];

        List<String> fileList = statement1.getBody().xmlPath().getList("feed.entry.id");
        logger.info("Dataset contains file ids: " + index0a + " " + index1a + " " + index2a + " (" + fileList.size() + ") files");

        Response deleteIndex0a = UtilIT.deleteFile(Integer.parseInt(index0a), apiToken);
//        deleteIndex0a.prettyPrint();
        deleteIndex0a.then().assertThat()
                .statusCode(NO_CONTENT.getStatusCode());
        logger.info("Deleted file id " + index0a + " from draft of unpublished dataset.");

        Response statement2 = UtilIT.getSwordStatement(datasetPersistentId, apiToken);
        statement2.prettyPrint();
        String index0b = statement2.getBody().xmlPath().get("feed.entry[0].id").toString().split("/")[10];
        String index1b = statement2.getBody().xmlPath().get("feed.entry[1].id").toString().split("/")[10];
        try {
            String index2b = statement2.getBody().xmlPath().get("feed.entry[2].id").toString().split("/")[10];
        } catch (ArrayIndexOutOfBoundsException ex) {
            // expected since file has been deleted
        }
        logger.info("Dataset contains file ids: " + index0b + " " + index1b);
        List<String> twoFilesLeftInV2Draft = statement2.getBody().xmlPath().getList("feed.entry.id");
        logger.info("Number of files remaining in this draft:" + twoFilesLeftInV2Draft.size());
        assertEquals(2, twoFilesLeftInV2Draft.size());

        Response publishDataverse = UtilIT.publishDataverseViaSword(dataverseAlias, apiToken);
//        publishDataverse.prettyPrint();
        publishDataverse.then().assertThat()
                .statusCode(OK.getStatusCode());

        logger.info("dataset has not yet been published:");
        Response atomEntryUnpublished = UtilIT.getSwordAtomEntry(datasetPersistentId, apiToken);
        atomEntryUnpublished.prettyPrint();

        Response publishDataset = UtilIT.publishDatasetViaSword(datasetPersistentId, apiToken);
//        publishDataset.prettyPrint();
        publishDataset.then().assertThat()
                .statusCode(OK.getStatusCode());

        logger.info("dataset has been published:");
        Response atomEntryPublishedV1 = UtilIT.getSwordAtomEntry(datasetPersistentId, apiToken);
        atomEntryPublishedV1.prettyPrint();

        Response deleteIndex0b = UtilIT.deleteFile(Integer.parseInt(index0b), apiToken);
//        deleteIndex0b.prettyPrint();
        deleteIndex0b.then().assertThat()
                .statusCode(NO_CONTENT.getStatusCode());
        logger.info("Deleted file id " + index0b + " from published dataset (should create draft).");
        Response statement3 = UtilIT.getSwordStatement(datasetPersistentId, apiToken);
        statement3.prettyPrint();

        logger.info("draft created from published dataset because a file was deleted:");
        Response atomEntryDraftV2 = UtilIT.getSwordAtomEntry(datasetPersistentId, apiToken);
        atomEntryDraftV2.prettyPrint();
        String citation = atomEntryDraftV2.body().xmlPath().getString("entry.bibliographicCitation");
        logger.info("citation (should contain 'DRAFT'): " + citation);
        boolean draftStringFoundInCitation = citation.matches(".*DRAFT.*");
        assertEquals(true, draftStringFoundInCitation);

        List<String> oneFileLeftInV2Draft = statement3.getBody().xmlPath().getList("feed.entry.id");
        logger.info("Number of files remaining in this post version 1 draft:" + oneFileLeftInV2Draft.size());
        assertEquals(1, oneFileLeftInV2Draft.size());

        Response deleteIndex1b = UtilIT.deleteFile(Integer.parseInt(index1b), apiToken);
        deleteIndex1b.then().assertThat()
                .statusCode(NO_CONTENT.getStatusCode());
        logger.info("Deleted file id " + index1b + " from draft version of a published dataset.");

        Response statement4 = UtilIT.getSwordStatement(datasetPersistentId, apiToken);
        statement4.prettyPrint();

        List<String> fileListEmpty = statement4.getBody().xmlPath().getList("feed.entry.id");
        logger.info("Number of files remaining:" + fileListEmpty.size());
        assertEquals(0, fileListEmpty.size());

        Response deleteDatasetDraft = UtilIT.deleteLatestDatasetVersionViaSwordApi(datasetPersistentId, apiToken);
        deleteDatasetDraft.prettyPrint();

        Response statement5 = UtilIT.getSwordStatement(datasetPersistentId, apiToken);
        statement5.prettyPrint();
        List<String> twoFilesinV1published = statement5.getBody().xmlPath().getList("feed.entry.id");
        logger.info("Number of files in V1 (draft has been deleted)" + twoFilesinV1published.size());
        assertEquals(2, twoFilesinV1published.size());

        /**
         * @todo The "destroy" endpoint should accept a persistentId:
         * https://github.com/IQSS/dataverse/issues/1837
         */
        Response reindexDatasetToFindDatabaseId = UtilIT.reindexDataset(datasetPersistentId);
        reindexDatasetToFindDatabaseId.prettyPrint();
        reindexDatasetToFindDatabaseId.then().assertThat()
                .statusCode(OK.getStatusCode());
        Integer datasetId3 = JsonPath.from(reindexDatasetToFindDatabaseId.asString()).getInt("data.id");
        Response makeSuperuserRespone = UtilIT.makeSuperUser(username);
        makeSuperuserRespone.then().assertThat()
                .statusCode(OK.getStatusCode());
        Response destroyDataset = UtilIT.destroyDataset(datasetId3, apiToken);
        destroyDataset.prettyPrint();
        destroyDataset.then().assertThat()
                .statusCode(OK.getStatusCode());
        logger.info("Dataset has been destroyed: " + datasetPersistentId + " id " + datasetId3 + " but let's double check we can't access it...");

        Response atomEntryDestroyed = UtilIT.getSwordAtomEntry(datasetPersistentId, apiToken);
        atomEntryDestroyed.prettyPrint();
        atomEntryDestroyed.then().statusCode(400);

        Response createDataset4 = UtilIT.createRandomDatasetViaSwordApi(dataverseAlias, apiToken);
        createDataset4.prettyPrint();
        String datasetPersistentId4 = UtilIT.getDatasetPersistentIdFromResponse(createDataset4);

        Response uploadZipToDataset4 = UtilIT.uploadFile(datasetPersistentId4, "3files.zip", apiToken);
        uploadZipToDataset4.prettyPrint();
        assertEquals(CREATED.getStatusCode(), uploadZipToDataset4.getStatusCode());
        Response publishDataset4 = UtilIT.publishDatasetViaSword(datasetPersistentId4, apiToken);
//        publishDataset4.prettyPrint();
        publishDataset4.then().assertThat()
                .statusCode(OK.getStatusCode());

        Response statement4a = UtilIT.getSwordStatement(datasetPersistentId4, apiToken);
        statement4a.prettyPrint();
        List<String> threePublishedFiles = statement4a.getBody().xmlPath().getList("feed.entry.id");
        logger.info("Number of files in lastest version (v1 published) of " + datasetPersistentId4 + threePublishedFiles.size());
        assertEquals(3, threePublishedFiles.size());
        String dataset4FileIndex0 = statement4a.getBody().xmlPath().get("feed.entry[0].id").toString().split("/")[10];
        String dataset4FileIndex1 = statement4a.getBody().xmlPath().get("feed.entry[1].id").toString().split("/")[10];
        String dataset4FileIndex2 = statement4a.getBody().xmlPath().get("feed.entry[2].id").toString().split("/")[10];
        /**
         * @todo Fix https://github.com/IQSS/dataverse/issues/2464 so that *any*
         * file can be deleted via SWORD and not just the first file (zero
         * index). Attempting to delete dataset4FileIndex1 or dataset4FileIndex2
         * will exercise the bug. Attempting to delete dataset4FileIndex0 will
         * not.
         */
        String fileToDeleteFromDataset4 = dataset4FileIndex1;

        Response deleteFileFromDataset4 = UtilIT.deleteFile(Integer.parseInt(fileToDeleteFromDataset4), apiToken);
        deleteFileFromDataset4.then().assertThat()
                .statusCode(NO_CONTENT.getStatusCode());
        logger.info("Deleted file id " + fileToDeleteFromDataset4 + " from " + datasetPersistentId4 + " which should move it from published to draft.");
        Response statement4b = UtilIT.getSwordStatement(datasetPersistentId4, apiToken);
        statement4b.prettyPrint();

        boolean issue2464fixed = false;
        if (issue2464fixed) {
            List<String> datasetMovedToDraftWithTwoFilesLeft = statement4b.getBody().xmlPath().getList("feed.entry.id");
            logger.info("Number of files left in " + datasetPersistentId4 + ": " + datasetMovedToDraftWithTwoFilesLeft.size());
            assertEquals(2, datasetMovedToDraftWithTwoFilesLeft.size());
        } else {
            List<String> issue2464NotFixedYetSoAllThreeFilesRemain = statement4b.getBody().xmlPath().getList("feed.entry.id");
            logger.info("Number of files left in " + datasetPersistentId4 + ": " + issue2464NotFixedYetSoAllThreeFilesRemain.size());
        }

        /**
         * @todo The "destroy" endpoint should accept a persistentId:
         * https://github.com/IQSS/dataverse/issues/1837
         */
        Response reindexDataset4ToFindDatabaseId = UtilIT.reindexDataset(datasetPersistentId4);
        reindexDataset4ToFindDatabaseId.prettyPrint();
        reindexDataset4ToFindDatabaseId.then().assertThat()
                .statusCode(OK.getStatusCode());
        Integer datasetId4 = JsonPath.from(reindexDataset4ToFindDatabaseId.asString()).getInt("data.id");

        Response destroyDataset4 = UtilIT.destroyDataset(datasetId4, apiToken);
        destroyDataset4.prettyPrint();
        destroyDataset4.then().assertThat()
                .statusCode(OK.getStatusCode());

        Response deleteDataverse3Response = UtilIT.deleteDataverse(dataverseAlias, apiToken);
        deleteDataverse3Response.prettyPrint();
        assertEquals(200, deleteDataverse3Response.getStatusCode());

        UtilIT.deleteUser(username);

    }

    @AfterClass
    public static void tearDownClass() {
        UtilIT.deleteUser(superuser);
    }

}
