package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;

public class TmpIT {

    String title = "Fast & Furious";

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

        Response getDatasetJsonBeforePublishing = UtilIT.nativeGet(datasetId, apiToken);
        getDatasetJsonBeforePublishing.prettyPrint();
        String protocol = JsonPath.from(getDatasetJsonBeforePublishing.getBody().asString()).getString("data.protocol");
        String authority = JsonPath.from(getDatasetJsonBeforePublishing.getBody().asString()).getString("data.authority");
        String identifier = JsonPath.from(getDatasetJsonBeforePublishing.getBody().asString()).getString("data.identifier");
        String datasetPersistentId = protocol + ":" + authority + "/" + identifier;

        Response publishDataverse = UtilIT.publishDataverseViaSword(dataverseAlias, apiToken);
        assertEquals(200, publishDataverse.getStatusCode());
        Response attemptToPublishZeroDotOne = UtilIT.publishDatasetViaNativeApiDeprecated(datasetPersistentId, "minor", apiToken);
        attemptToPublishZeroDotOne.prettyPrint();
        attemptToPublishZeroDotOne.then().assertThat()
                .body("message", equalTo("Cannot publish as minor version. Re-try as major release."))
                .statusCode(403);

        Response publishDataset = UtilIT.publishDatasetViaNativeApi(datasetPersistentId, "major", apiToken);
        assertEquals(200, publishDataset.getStatusCode());

    }
}
