package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import com.jayway.restassured.path.json.JsonPath;

import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.with;
import static junit.framework.Assert.assertEquals;

public class DatasetsIT {

    private static final Logger logger = Logger.getLogger(DatasetsIT.class.getCanonicalName());

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    @Test
    public void testCreateDataset() {

        Response createUser = UtilIT.createRandomUser();
        createUser.prettyPrint();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);

        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
        createDataverseResponse.prettyPrint();
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);

        Response createDatasetResponse = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias, apiToken);
        createDatasetResponse.prettyPrint();
        Integer datasetId = UtilIT.getDatasetIdFromResponse(createDatasetResponse);

        Response deleteDatasetResponse = UtilIT.deleteDatasetViaNativeApi(datasetId, apiToken);
        deleteDatasetResponse.prettyPrint();
        assertEquals(200, deleteDatasetResponse.getStatusCode());

        Response deleteDataverseResponse = UtilIT.deleteDataverse(dataverseAlias, apiToken);
        deleteDataverseResponse.prettyPrint();
        assertEquals(200, deleteDataverseResponse.getStatusCode());

        Response deleteUserResponse = UtilIT.deleteUser(username);
        deleteUserResponse.prettyPrint();
        assertEquals(200, deleteUserResponse.getStatusCode());

    }

    @Test
    public void testCreatePublishDestroyDataset() {

        Response createUser = UtilIT.createRandomUser();
        createUser.prettyPrint();
        assertEquals(200, createUser.getStatusCode());
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);
        Response makeSuperUser = UtilIT.makeSuperUser(username);
        assertEquals(200, makeSuperUser.getStatusCode());

        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
        createDataverseResponse.prettyPrint();
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);

        Response createDatasetResponse = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias, apiToken);
        createDatasetResponse.prettyPrint();
        Integer datasetId = JsonPath.from(createDatasetResponse.body().asString()).getInt("data.id");

        Response publishDataverse = UtilIT.publishDataverseViaSword(dataverseAlias, apiToken);
        assertEquals(200, publishDataverse.getStatusCode());
        Response publishDataset = UtilIT.publishDatasetViaNativeApi(datasetId, "major", apiToken);
        assertEquals(200, publishDataset.getStatusCode());

        Response deleteDatasetResponse = UtilIT.destroyDataset(datasetId, apiToken);
        deleteDatasetResponse.prettyPrint();
        assertEquals(200, deleteDatasetResponse.getStatusCode());

        Response deleteDataverseResponse = UtilIT.deleteDataverse(dataverseAlias, apiToken);
        deleteDataverseResponse.prettyPrint();
        assertEquals(200, deleteDataverseResponse.getStatusCode());

        Response deleteUserResponse = UtilIT.deleteUser(username);
        deleteUserResponse.prettyPrint();
        assertEquals(200, deleteUserResponse.getStatusCode());

    }

    @Test
    public void testGetDdi() {
        String persistentIdentifier = "FIXME";
        String apiToken = "FIXME";
        Response nonDto = getDatasetAsDdiNonDto(persistentIdentifier, apiToken);
        nonDto.prettyPrint();
        assertEquals(403, nonDto.getStatusCode());

        Response dto = getDatasetAsDdiDto(persistentIdentifier, apiToken);
        dto.prettyPrint();
        assertEquals(403, dto.getStatusCode());
    }

    private Response getDatasetAsDdiNonDto(String persistentIdentifier, String apiToken) {
        Response response = given()
                .header(UtilIT.API_TOKEN_HTTP_HEADER, apiToken)
                .get("/api/datasets/ddi?persistentId=" + persistentIdentifier);
        return response;
    }

    private Response getDatasetAsDdiDto(String persistentIdentifier, String apiToken) {
        Response response = given()
                .header(UtilIT.API_TOKEN_HTTP_HEADER, apiToken)
                .get("/api/datasets/ddi?persistentId=" + persistentIdentifier + "&dto=true");
        return response;
    }

    @Test
    public void testPrivateUrl() {

        Response createUser = UtilIT.createRandomUser();
//        createUser.prettyPrint();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);

        Response failToCreateWhenDatasetIdNotFound = UtilIT.privateUrlCreate(Integer.MAX_VALUE, apiToken);
        failToCreateWhenDatasetIdNotFound.prettyPrint();
        assertEquals(NOT_FOUND.getStatusCode(), failToCreateWhenDatasetIdNotFound.getStatusCode());

        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
        createDataverseResponse.prettyPrint();
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);

        Response createDatasetResponse = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias, apiToken);
        createDatasetResponse.prettyPrint();
        Integer datasetId = JsonPath.from(createDatasetResponse.body().asString()).getInt("data.id");
        System.out.println("dataset id: " + datasetId);

        Response getDatasetJson = UtilIT.nativeGet(datasetId, apiToken);
        getDatasetJson.prettyPrint();
        String protocol1 = JsonPath.from(getDatasetJson.getBody().asString()).getString("data.protocol");
        String authority1 = JsonPath.from(getDatasetJson.getBody().asString()).getString("data.authority");
        String identifier1 = JsonPath.from(getDatasetJson.getBody().asString()).getString("data.identifier");
        String dataset1PersistentId = protocol1 + ":" + authority1 + "/" + identifier1;

        Response uploadFileResponse = UtilIT.uploadRandomFile(dataset1PersistentId, apiToken);
        uploadFileResponse.prettyPrint();
        assertEquals(CREATED.getStatusCode(), uploadFileResponse.getStatusCode());

        Response badApiKeyEmptyString = UtilIT.privateUrlGet(datasetId, "");
        badApiKeyEmptyString.prettyPrint();
        assertEquals(UNAUTHORIZED.getStatusCode(), badApiKeyEmptyString.getStatusCode());
        Response badApiKeyDoesNotExist = UtilIT.privateUrlGet(datasetId, "junk");
        badApiKeyDoesNotExist.prettyPrint();
        assertEquals(UNAUTHORIZED.getStatusCode(), badApiKeyDoesNotExist.getStatusCode());
        Response badDatasetId = UtilIT.privateUrlGet(Integer.MAX_VALUE, apiToken);
        badDatasetId.prettyPrint();
        assertEquals(NOT_FOUND.getStatusCode(), badDatasetId.getStatusCode());
        Response pristine = UtilIT.privateUrlGet(datasetId, apiToken);
        pristine.prettyPrint();
        assertEquals(NOT_FOUND.getStatusCode(), pristine.getStatusCode());

        Response createPrivateUrl = UtilIT.privateUrlCreate(datasetId, apiToken);
        createPrivateUrl.prettyPrint();
        assertEquals(OK.getStatusCode(), createPrivateUrl.getStatusCode());

        /**
         * @todo Do a GET of privateurl.xhtml to make sure you get a 200
         * response.
         */
        Response userWithNoRoles = UtilIT.createRandomUser();
        String userWithNoRolesApiToken = UtilIT.getApiTokenFromResponse(userWithNoRoles);
        Response unAuth = UtilIT.privateUrlGet(datasetId, userWithNoRolesApiToken);
        unAuth.prettyPrint();
        assertEquals(UNAUTHORIZED.getStatusCode(), unAuth.getStatusCode());
        Response shouldExist = UtilIT.privateUrlGet(datasetId, apiToken);
        shouldExist.prettyPrint();
        assertEquals(OK.getStatusCode(), shouldExist.getStatusCode());

        String tokenForGuestOfDataset = JsonPath.from(shouldExist.body().asString()).getString("data.generated");
        logger.info("privateUrlToken: " + tokenForGuestOfDataset);
        long roleAssignmentIdFromCreate = JsonPath.from(createPrivateUrl.body().asString()).getLong("data.roleAssignment.id");
        logger.info("roleAssignmentIdFromCreate: " + roleAssignmentIdFromCreate);

        Response badAnonLinkTokenEmptyString = UtilIT.nativeGet(datasetId, "");
        badAnonLinkTokenEmptyString.prettyPrint();
        assertEquals(UNAUTHORIZED.getStatusCode(), badAnonLinkTokenEmptyString.getStatusCode());

        Response getWithPrivateUrlToken = UtilIT.nativeGet(datasetId, tokenForGuestOfDataset);
        assertEquals(OK.getStatusCode(), getWithPrivateUrlToken.getStatusCode());
//        getWithPrivateUrlToken.prettyPrint();
        logger.info("http://localhost:8080/privateurl.xhtml?token=" + tokenForGuestOfDataset);
        Response swordStatement = UtilIT.getSwordStatement(dataset1PersistentId, apiToken);
        assertEquals(OK.getStatusCode(), swordStatement.getStatusCode());
        Integer fileId = UtilIT.getFileIdFromSwordStatementResponse(swordStatement);
        Response downloadFile = UtilIT.downloadFile(fileId, tokenForGuestOfDataset);
        assertEquals(OK.getStatusCode(), downloadFile.getStatusCode());
        Response downloadFileBadToken = UtilIT.downloadFile(fileId, "junk");
        assertEquals(FORBIDDEN.getStatusCode(), downloadFileBadToken.getStatusCode());
        Response notPermittedToListRoleAssignment = UtilIT.getRoleAssignmentsOnDataset(datasetId.toString(), null, userWithNoRolesApiToken);
        assertEquals(UNAUTHORIZED.getStatusCode(), notPermittedToListRoleAssignment.getStatusCode());
        Response roleAssignments = UtilIT.getRoleAssignmentsOnDataset(datasetId.toString(), null, apiToken);
        roleAssignments.prettyPrint();
        assertEquals(OK.getStatusCode(), roleAssignments.getStatusCode());
        List<JsonObject> assignments = with(roleAssignments.body().asString()).param("member", "member").getJsonObject("data.findAll { data -> data._roleAlias == member }");
        assertEquals(1, assignments.size());
        Map roleAssignment = assignments.get(0);
        int roleAssignmentId = (int) roleAssignment.get("id");
        logger.info("role assignment id: " + roleAssignmentId);
        assertEquals(roleAssignmentIdFromCreate, roleAssignmentId);
        Response revoke = UtilIT.revokeRole(dataverseAlias, roleAssignmentId, apiToken);
        revoke.prettyPrint();
        assertEquals(OK.getStatusCode(), revoke.getStatusCode());

        Response shouldNoLongerExist = UtilIT.privateUrlGet(datasetId, apiToken);
        shouldNoLongerExist.prettyPrint();
        assertEquals(NOT_FOUND.getStatusCode(), shouldNoLongerExist.getStatusCode());

        Response createPrivateUrlUnauth = UtilIT.privateUrlCreate(datasetId, userWithNoRolesApiToken);
        createPrivateUrlUnauth.prettyPrint();
        assertEquals(UNAUTHORIZED.getStatusCode(), createPrivateUrlUnauth.getStatusCode());

        Response createPrivateUrlAgain = UtilIT.privateUrlCreate(datasetId, apiToken);
        createPrivateUrlAgain.prettyPrint();
        assertEquals(OK.getStatusCode(), createPrivateUrlAgain.getStatusCode());

        Response shouldNotDeletePrivateUrl = UtilIT.privateUrlDelete(datasetId, userWithNoRolesApiToken);
        shouldNotDeletePrivateUrl.prettyPrint();
        assertEquals(UNAUTHORIZED.getStatusCode(), shouldNotDeletePrivateUrl.getStatusCode());

        Response deletePrivateUrlResponse = UtilIT.privateUrlDelete(datasetId, apiToken);
        deletePrivateUrlResponse.prettyPrint();
        assertEquals(OK.getStatusCode(), deletePrivateUrlResponse.getStatusCode());

        Response tryToDeleteAlreadyDeletedPrivateUrl = UtilIT.privateUrlDelete(datasetId, apiToken);
        tryToDeleteAlreadyDeletedPrivateUrl.prettyPrint();
        assertEquals(NOT_FOUND.getStatusCode(), tryToDeleteAlreadyDeletedPrivateUrl.getStatusCode());

        Response createPrivateUrlOnceAgain = UtilIT.privateUrlCreate(datasetId, apiToken);
        createPrivateUrlOnceAgain.prettyPrint();
        assertEquals(OK.getStatusCode(), createPrivateUrlOnceAgain.getStatusCode());

        Response tryToCreatePrivateUrlWhenExisting = UtilIT.privateUrlCreate(datasetId, apiToken);
        tryToCreatePrivateUrlWhenExisting.prettyPrint();
        assertEquals(BAD_REQUEST.getStatusCode(), tryToCreatePrivateUrlWhenExisting.getStatusCode());

        Response publishDataverse = UtilIT.publishDataverseViaSword(dataverseAlias, apiToken);
        assertEquals(OK.getStatusCode(), publishDataverse.getStatusCode());
        Response publishDataset = UtilIT.publishDatasetViaSword(dataset1PersistentId, apiToken);
        assertEquals(OK.getStatusCode(), publishDataset.getStatusCode());
        Response privateUrlTokenShouldBeDeletedOnPublish = UtilIT.privateUrlGet(datasetId, apiToken);
        privateUrlTokenShouldBeDeletedOnPublish.prettyPrint();
        assertEquals(NOT_FOUND.getStatusCode(), privateUrlTokenShouldBeDeletedOnPublish.getStatusCode());

        Response getRoleAssignmentsOnDatasetShouldFailUnauthorized = UtilIT.getRoleAssignmentsOnDataset(datasetId.toString(), null, userWithNoRolesApiToken);
        assertEquals(UNAUTHORIZED.getStatusCode(), getRoleAssignmentsOnDatasetShouldFailUnauthorized.getStatusCode());
        Response publishingShouldHaveRemovedRoleAssignmentForGuestOfDataset = UtilIT.getRoleAssignmentsOnDataset(datasetId.toString(), null, apiToken);
        publishingShouldHaveRemovedRoleAssignmentForGuestOfDataset.prettyPrint();
        List<JsonObject> noAssignmentsForGuestOfDataset = with(publishingShouldHaveRemovedRoleAssignmentForGuestOfDataset.body().asString()).param("member", "member").getJsonObject("data.findAll { data -> data._roleAlias == member }");
        assertEquals(0, noAssignmentsForGuestOfDataset.size());

        Response tryToCreatePrivateUrlToPublishedVersion = UtilIT.privateUrlCreate(datasetId, apiToken);
        tryToCreatePrivateUrlToPublishedVersion.prettyPrint();
        assertEquals(BAD_REQUEST.getStatusCode(), tryToCreatePrivateUrlToPublishedVersion.getStatusCode());

        String newTitle = "I am changing the title";
        Response updatedMetadataResponse = UtilIT.updateDatasetTitleViaSword(dataset1PersistentId, newTitle, apiToken);
        updatedMetadataResponse.prettyPrint();
        assertEquals(OK.getStatusCode(), updatedMetadataResponse.getStatusCode());

        Response createPrivateUrlForPostVersionOneDraft = UtilIT.privateUrlCreate(datasetId, apiToken);
        createPrivateUrlForPostVersionOneDraft.prettyPrint();
        assertEquals(OK.getStatusCode(), createPrivateUrlForPostVersionOneDraft.getStatusCode());

        Response makeSuperUser = UtilIT.makeSuperUser(username);
        assertEquals(200, makeSuperUser.getStatusCode());

        Response destroyDatasetResponse = UtilIT.destroyDataset(datasetId, apiToken);
        destroyDatasetResponse.prettyPrint();
        assertEquals(200, destroyDatasetResponse.getStatusCode());

        Response deleteDataverseResponse = UtilIT.deleteDataverse(dataverseAlias, apiToken);
        deleteDataverseResponse.prettyPrint();
        assertEquals(200, deleteDataverseResponse.getStatusCode());

        Response deleteUserResponse = UtilIT.deleteUser(username);
        deleteUserResponse.prettyPrint();
        assertEquals(200, deleteUserResponse.getStatusCode());
        /**
         * @todo Should the Search API work with the Private URL token?
         */
    }

}
