package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.AfterClass;
import com.jayway.restassured.path.json.JsonPath;
import org.junit.Ignore;
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

    @Ignore
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
        Response badApiKeyEmptyString = UtilIT.anonLinkGet(datasetId, "");
        badApiKeyEmptyString.prettyPrint();
        Response badApiKeyDoesNotExist = UtilIT.anonLinkGet(datasetId, "junk");
        badApiKeyDoesNotExist.prettyPrint();
        Response badDatasetId = UtilIT.anonLinkGet(Integer.MAX_VALUE, apiToken);
        badDatasetId.prettyPrint();
        Response pristine = UtilIT.anonLinkGet(datasetId, apiToken);
        pristine.prettyPrint();

        Response createAnonLink = UtilIT.anonLinkRegenerate(datasetId, apiToken);
        createAnonLink.prettyPrint();

        /**
         * @todo Not just anyone should be able to get the token! You need to be
         * able to create datasets, not just have a valid Dataverse account.
         */
        Response shouldExist = UtilIT.anonLinkGet(datasetId, apiToken);
        shouldExist.prettyPrint();

        /**
         * @todo Rename from "get" to "token" or something.
         */
        String anonLinkToken = JsonPath.from(shouldExist.body().asString()).getString("data.get");
        logger.info("anonLinkToken: " + anonLinkToken);

        Response badAnonLinkTokenEmptyString = UtilIT.nativeGetAnon(datasetId, "");
        badAnonLinkTokenEmptyString.prettyPrint();
        assertEquals(401, badAnonLinkTokenEmptyString.getStatusCode());

        Response badAnonLinkTokenDoesNotExist = UtilIT.nativeGetAnon(datasetId, "junk");
        badAnonLinkTokenDoesNotExist.prettyPrint();
        assertEquals(401, badAnonLinkTokenDoesNotExist.getStatusCode());

        Response getWithAnonToken = UtilIT.nativeGetAnon(datasetId, anonLinkToken);
//        getWithAnonToken.prettyPrint();
        assertEquals(200, getWithAnonToken.getStatusCode());

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

    @Ignore
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
        boolean disabled = true;

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
