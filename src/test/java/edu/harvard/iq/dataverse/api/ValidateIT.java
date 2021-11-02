package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

public class ValidateIT {

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    @Test
    public void testValidate() throws IOException {
        Response createUser = UtilIT.createRandomUser();
        createUser.then().assertThat().statusCode(OK.getStatusCode());
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);

        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
        createDataverseResponse.prettyPrint();
        createDataverseResponse.then().assertThat()
                .statusCode(CREATED.getStatusCode());

        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);

        Response createDataset = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias, apiToken);
        createDataset.prettyPrint();
        createDataset.then().assertThat()
                .statusCode(CREATED.getStatusCode());

        Integer datasetId = UtilIT.getDatasetIdFromResponse(createDataset);
        String datasetPid = UtilIT.getDatasetPersistentIdFromResponse(createDataset);
        String badCharacter = "(\f)";
//        badCharacter = "(...)";

//        String datasetPersistentId = "doi:10.5072/FK2/YR38GT";
        JsonObjectBuilder jsonUpdateObject = Json.createObjectBuilder().add("fields",
                Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("typeName", "title")
                                .add("value", "MyTitle " + badCharacter)
                        ));
        String jsonUpdateString = jsonUpdateObject.build().toString();
        Path jsonUpdatePath = Paths.get(java.nio.file.Files.createTempDirectory(null) + File.separator + "update.json");
        java.nio.file.Files.write(jsonUpdatePath, jsonUpdateString.getBytes());
        Response addDataToBadData = UtilIT.updateFieldLevelDatasetMetadataViaNative(datasetPid, jsonUpdatePath.toString(), apiToken);
//        addDataToBadData.prettyPrint();
        addDataToBadData.then().assertThat().statusCode(OK.getStatusCode());
//        Response foo = UtilIT.nativeGet(datasetId, apiToken);
//        foo.prettyPrint();
        Response validateForCharacters = UtilIT.validateDatasetForInvalidCharacters(datasetPid, null);
        validateForCharacters.prettyPrint();
        Response invalidDataset = UtilIT.validateDataset(datasetId.toString());
        invalidDataset.prettyPrint();
        invalidDataset.then().assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("Exception thrown from bean: java.lang.RuntimeException: Field title contains \f"));

        if (true) {
            return;
        }

        Response fixDataset = UtilIT.fixDataset(datasetId.toString());
        fixDataset.prettyPrint();
        fixDataset.then().assertThat().statusCode(OK.getStatusCode());

//        if (true) {
//            return;
//        }
        Response validDataset = UtilIT.validateDataset(datasetId.toString());
        validDataset.prettyPrint();
        validDataset.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("data.message", Matchers.equalTo("valid"));
    }
}
