package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.AfterClass;
import com.jayway.restassured.path.json.JsonPath;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import static com.jayway.restassured.RestAssured.given;
import static junit.framework.Assert.assertEquals;

public class DatasetsIT {

    private static final Logger logger = Logger.getLogger(DatasetsIT.class.getCanonicalName());
    private static String username1;
    private static String apiToken1;
    private static String dataverseAlias1;
    private static Integer datasetId1;

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    @Test
    public void testCreateDataset() {

        Response createUser1 = UtilIT.createRandomUser();
//        createUser1.prettyPrint();
        username1 = UtilIT.getUsernameFromResponse(createUser1);
        apiToken1 = UtilIT.getApiTokenFromResponse(createUser1);

        Response createDataverse1Response = UtilIT.createRandomDataverse(apiToken1);
        createDataverse1Response.prettyPrint();
        dataverseAlias1 = UtilIT.getAliasFromResponse(createDataverse1Response);

        Response createDataset1Response = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias1, apiToken1);
        createDataset1Response.prettyPrint();
        datasetId1 = UtilIT.getDatasetIdFromResponse(createDataset1Response);

    }

    @Test
    public void testAnonLink() {

        Response createUser = UtilIT.createRandomUser();
//        createUser.prettyPrint();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);

        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
        createDataverseResponse.prettyPrint();
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);

        Response createDatasetResponse = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias, apiToken);
        createDatasetResponse.prettyPrint();
        Integer datasetId = JsonPath.from(createDatasetResponse.body().asString()).getInt("data.id");
        System.out.println("dataset id: " + datasetId);

        /**
         * @todo Upload a file.
         */
        Response getDatasetJson = UtilIT.nativeGet(datasetId, apiToken);
        getDatasetJson.prettyPrint();
        Response badApiKeyEmptyString = UtilIT.privateUrlGet(datasetId, "");
        badApiKeyEmptyString.prettyPrint();
        assertEquals(401, badApiKeyEmptyString.getStatusCode());
        Response badApiKeyDoesNotExist = UtilIT.privateUrlGet(datasetId, "junk");
        badApiKeyDoesNotExist.prettyPrint();
        assertEquals(401, badApiKeyDoesNotExist.getStatusCode());
        Response badDatasetId = UtilIT.privateUrlGet(Integer.MAX_VALUE, apiToken);
        badDatasetId.prettyPrint();
        assertEquals(404, badDatasetId.getStatusCode());
        Response pristine = UtilIT.privateUrlGet(datasetId, apiToken);
        pristine.prettyPrint();
        assertEquals(200, pristine.getStatusCode());

        Response createAnonLink = UtilIT.anonLinkRegenerate(datasetId, apiToken);
        createAnonLink.prettyPrint();
        assertEquals(200, createAnonLink.getStatusCode());

        Response createUser2 = UtilIT.createRandomUser();
        String apiToken2 = UtilIT.getApiTokenFromResponse(createUser2);
        Response unAuth = UtilIT.privateUrlGet(datasetId, apiToken2);
        unAuth.prettyPrint();
        boolean securityBugFixed = false;
        if (securityBugFixed) {
            /**
             * @todo Not just anyone should be able to get the token! You need
             * to be able to create datasets, not just have a valid Dataverse
             * account.
             */
            assertEquals(401, unAuth.getStatusCode());
        }
        Response shouldExist = UtilIT.privateUrlGet(datasetId, apiToken);
        shouldExist.prettyPrint();
        assertEquals(200, shouldExist.getStatusCode());

        String apiTokenForTempUser = JsonPath.from(shouldExist.body().asString()).getString("data.apiTokenForTempUser");
        logger.info("anonLinkToken: " + apiTokenForTempUser);

//        Response badAnonLinkTokenEmptyString = UtilIT.nativeGet(datasetId, "");
//        badAnonLinkTokenEmptyString.prettyPrint();
//        assertEquals(401, badAnonLinkTokenEmptyString.getStatusCode());
//        if (true) {
//            return;
//        }
//
//        Response badAnonLinkTokenDoesNotExist = UtilIT.nativeGetAnon(datasetId, "junk");
//        badAnonLinkTokenDoesNotExist.prettyPrint();
//        assertEquals(401, badAnonLinkTokenDoesNotExist.getStatusCode());
//
        Response getWithAnonToken = UtilIT.nativeGet(datasetId, apiTokenForTempUser);
        getWithAnonToken.prettyPrint();
        String protocol = JsonPath.from(getWithAnonToken.getBody().asString()).getString("data.protocol");
        String authority = JsonPath.from(getWithAnonToken.getBody().asString()).getString("data.authority");
        String identifier = JsonPath.from(getWithAnonToken.getBody().asString()).getString("data.identifier");
        assertEquals(200, getWithAnonToken.getStatusCode());

        UtilIT.enableSetting(SettingsServiceBean.Key.SearchApiNonPublicAllowed);

        Response searchResponse = UtilIT.search("*", apiTokenForTempUser);
        searchResponse.prettyPrint();
        String globalIdFromSearch = JsonPath.from(searchResponse.getBody().asString()).getString("data.items[0].global_id");
        logger.info("globalIdFromSearch: " + globalIdFromSearch);
        assertEquals(globalIdFromSearch, protocol + ":" + authority + "/" + identifier);
        UtilIT.deleteSetting(SettingsServiceBean.Key.SearchApiNonPublicAllowed);
        /**
         * @todo Test that you can download the file with the anonLinkToken.
         * Probably it won't work...
         */
        /**
         * @todo Revoke token.
         */
        boolean deltingDatasetRemovesAnonLinkButNotViceVersa = false;
        if (deltingDatasetRemovesAnonLinkButNotViceVersa) {
            Response deleteDatasetResponse = UtilIT.deleteDatasetViaNativeApi(datasetId, apiToken);
            deleteDatasetResponse.prettyPrint();
            assertEquals(200, deleteDatasetResponse.getStatusCode());
        }

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

    @AfterClass
    public static void tearDownClass() {
        boolean disabled = false;

        if (disabled) {
            return;
        }

        Response deleteDatasetResponse = UtilIT.deleteDatasetViaNativeApi(datasetId1, apiToken1);
        deleteDatasetResponse.prettyPrint();
        assertEquals(200, deleteDatasetResponse.getStatusCode());

        Response deleteDataverse1Response = UtilIT.deleteDataverse(dataverseAlias1, apiToken1);
        deleteDataverse1Response.prettyPrint();
        assertEquals(200, deleteDataverse1Response.getStatusCode());

        Response deleteUser1Response = UtilIT.deleteUser(username1);
        deleteUser1Response.prettyPrint();
        assertEquals(200, deleteUser1Response.getStatusCode());

    }

}
