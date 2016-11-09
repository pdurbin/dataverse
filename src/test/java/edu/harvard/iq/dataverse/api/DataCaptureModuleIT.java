package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.Matchers.startsWith;

public class DataCaptureModuleIT {

    private static final Logger logger = Logger.getLogger(DatasetsIT.class.getCanonicalName());

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    /**
     * In order for this test to pass you must have the Data Capture Module
     * running: https://github.com/sbgrid/data-capture-module
     *
     * Configure :DataCaptureModuleUrl to point at wherever you are running the
     * DCM.
     */
    @Test
    public void testCreateDatasetWithDcmDependency() {

        Response createUser = UtilIT.createRandomUser();
        createUser.prettyPrint();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);
        long userId = JsonPath.from(createUser.body().asString()).getLong("data.authenticatedUser.id");
        Response urlConfigured = given()
                .header(UtilIT.API_TOKEN_HTTP_HEADER, apiToken)
                .get("/api/admin/settings/" + SettingsServiceBean.Key.DataCaptureModuleUrl.toString());
        if (urlConfigured.getStatusCode() != 200) {
            fail(SettingsServiceBean.Key.DataCaptureModuleUrl + " has not been not configured. This test cannot run without it. If you are running https://github.com/sbgrid/data-capture-module in Vagrant, consider using this: curl http://localhost:8080/api/admin/settings/:DataCaptureModuleUrl -X PUT -d 'http://localhost:8888'");
        }

        /**
         * @todo Query system to see which file upload mechanisms are available.
         */
        String dataverseAlias = UtilIT.getRandomIdentifier();
        List<String> fileUploadMechanismsEnabled = Arrays.asList(Dataset.FileUploadMechanism.RSYNC.toString());
        Response createDataverseResponse = UtilIT.createDataverse(dataverseAlias, fileUploadMechanismsEnabled, apiToken);
        createDataverseResponse.prettyPrint();
        createDataverseResponse.then().assertThat()
                .statusCode(201)
                .body("data.alias", equalTo(dataverseAlias))
                .body("data.fileUploadMechanismsEnabled[0]", equalTo(Dataset.FileUploadMechanism.RSYNC.toString()));

        /**
         * @todo Make this configurable at runtime similar to
         * UtilIT.getRestAssuredBaseUri
         */
        Response createDatasetResponse = UtilIT.createDatasetWithDcmDependency(dataverseAlias, apiToken);
        createDatasetResponse.prettyPrint();
        Integer datasetId = UtilIT.getDatasetIdFromResponse(createDatasetResponse);

        Response getDatasetResponse = given()
                .header(UtilIT.API_TOKEN_HTTP_HEADER, apiToken)
                .get("/api/datasets/" + datasetId);
        getDatasetResponse.prettyPrint();
        getDatasetResponse.then().assertThat()
                .statusCode(200);

//        final List<Map<String, ?>> dataTypeField = JsonPath.with(getDatasetResponse.body().asString())
//                .get("data.latestVersion.metadataBlocks.citation.fields.findAll { it.typeName == 'dataType' }");
//        logger.fine("dataTypeField: " + dataTypeField);
//        assertThat(dataTypeField.size(), equalTo(1));
//        assertEquals("dataType", dataTypeField.get(0).get("typeName"));
//        assertEquals("controlledVocabulary", dataTypeField.get(0).get("typeClass"));
//        assertEquals("X-Ray Diffraction", dataTypeField.get(0).get("value"));
//        assertTrue(dataTypeField.get(0).get("multiple").equals(false));
//
//        /**
//         * @todo Also test user who doesn't have permission.
//         */
//        Response getRsyncScriptPermErrorGuest = given()
//                .get("/api/datasets/" + datasetId + "/dataCaptureModule/rsync");
//        getRsyncScriptPermErrorGuest.prettyPrint();
//        getRsyncScriptPermErrorGuest.then().assertThat()
//                .statusCode(401)
//                .body("message", equalTo("User :guest is not permitted to perform requested action."));
//
//        Response createNoPermsUser = UtilIT.createRandomUser();
//        String noPermsUsername = UtilIT.getUsernameFromResponse(createNoPermsUser);
//        String noPermsApiToken = UtilIT.getApiTokenFromResponse(createNoPermsUser);
//
//        Response getRsyncScriptPermErrorNonGuest = given()
//                .header(UtilIT.API_TOKEN_HTTP_HEADER, noPermsApiToken)
//                .get("/api/datasets/" + datasetId + "/dataCaptureModule/rsync");
//        getRsyncScriptPermErrorNonGuest.then().assertThat()
//                .statusCode(401)
//                .body("message", equalTo("User @" + noPermsUsername + " is not permitted to perform requested action."));

        Response getRsyncScript = given()
                .header(UtilIT.API_TOKEN_HTTP_HEADER, apiToken)
                .get("/api/datasets/" + datasetId + "/dataCaptureModule/rsync?sleep"+"3000");
//                .get("/api/datasets/" + datasetId + "/dataCaptureModule/rsync?sleep"+"0");
        logger.info("BEGIN");
        getRsyncScript.prettyPrint();
        logger.info("END");
        getRsyncScript.then().assertThat()
                .statusCode(200)
                .body("data.datasetId", equalTo(datasetId))
                .body("data.script", startsWith("#!"));

//        Response attmeptToGetRsyncScriptForNonRsyncDataset = given()
//                .header(UtilIT.API_TOKEN_HTTP_HEADER, apiToken1)
//                .get("/api/datasets/" + datasetId1 + "/dataCaptureModule/rsync");
//        attmeptToGetRsyncScriptForNonRsyncDataset.prettyPrint();
//        attmeptToGetRsyncScriptForNonRsyncDataset.then().assertThat()
//                .statusCode(404)
//                .body("message", equalTo("An rsync script was not found for dataset id " + datasetId1));
//
//        /**
//         * Here we are pretending to be the Data Capture Module reporting on if
//         * checksum validation success or failure. Don't notify the user on
//         * success (too chatty) but do notify on failure.
//         *
//         * @todo On success a process should be kicked off to crawl the files so
//         * they are imported into Dataverse. Once the crawling and importing is
//         * complete, notify the user.
//         *
//         * @todo What authentication should be used here? The API token of the
//         * user? (If so, pass the token in the initial upload request payload.)
//         * This is suboptimal because of the security risk of having the Data
//         * Capture Module store the API token. Or should Dataverse be able to be
//         * configured so that it only will receive these messages from trusted
//         * IP addresses? Should there be a shared secret that's used for *all*
//         * requests from the Data Capture Module to Dataverse?
//         */
//        JsonObjectBuilder wrongDataset = Json.createObjectBuilder();
//        wrongDataset.add("userId", userId);
//        wrongDataset.add("datasetId", datasetId1);
//        wrongDataset.add("status", "validation passed");
//        Response datasetWithoutRsyncScript = given()
//                .body(wrongDataset.build().toString())
//                .contentType(ContentType.JSON)
//                .post("/api/datasets/" + datasetId1 + "/dataCaptureModule/checksumValidation");
//        datasetWithoutRsyncScript.prettyPrint();
//
//        datasetWithoutRsyncScript.then().assertThat()
//                .statusCode(400)
//                .body("message", equalTo("Dataset id " + datasetId1 + " does not have an rsync script."));
        JsonObjectBuilder badNews = Json.createObjectBuilder();
        badNews.add("userId", userId);
        badNews.add("datasetId", datasetId);
        // Status options are documented at https://github.com/sbgrid/data-capture-module/blob/master/doc/api.md#post-upload
        badNews.add("status", "validation failed");
        Response uploadFailed = given()
                .body(badNews.build().toString())
                .contentType(ContentType.JSON)
                .post("/api/datasets/" + datasetId + "/dataCaptureModule/checksumValidation");
        uploadFailed.prettyPrint();

        uploadFailed.then().assertThat()
                /**
                 * @todo Double check that we're ok with 200 here. We're saying
                 * "Ok, the bad news was delivered."
                 */
                .statusCode(200)
                .body("data.message", equalTo("User notified about checksum validation failure."));

        /**
         * @todo How can we test what the checksum validation notification looks
         * like in the GUI? There is no API for retrieving notifications.
         *
         * @todo How can we test that the email notification looks ok?
         */
//        System.out.println("try logging in with " + username);
        // Meanwhile, the user trys uploading again...
        JsonObjectBuilder goodNews = Json.createObjectBuilder();
        goodNews.add("userId", userId);
        goodNews.add("datasetId", datasetId);
        goodNews.add("status", "validation passed");
        Response uploadSuccessful = given()
                .body(goodNews.build().toString())
                .contentType(ContentType.JSON)
                .post("/api/datasets/" + datasetId + "/dataCaptureModule/checksumValidation");
        uploadSuccessful.prettyPrint();

        uploadSuccessful.then().assertThat()
                .statusCode(200)
                .body("data.message", startsWith("Next we will write code to kick off crawling and importing of files"));

        Response deleteDatasetResponse = UtilIT.deleteDatasetViaNativeApi(datasetId, apiToken);
        deleteDatasetResponse.prettyPrint();
        assertEquals(200, deleteDatasetResponse.getStatusCode());

        Response deleteDataverseResponse = UtilIT.deleteDataverse(dataverseAlias, apiToken);
        deleteDataverseResponse.prettyPrint();
        assertEquals(200, deleteDataverseResponse.getStatusCode());

        Response deleteUserResponse = UtilIT.deleteUser(username);
        deleteUserResponse.prettyPrint();
        assertEquals(200, deleteUserResponse.getStatusCode());

//        UtilIT.deleteUser(noPermsUsername);

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
}
