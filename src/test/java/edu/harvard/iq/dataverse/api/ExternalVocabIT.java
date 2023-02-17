package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExternalVocabIT {

    private static final Logger logger = Logger.getLogger(ExternalVocabIT.class.getCanonicalName());

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    @Test
    public void testFunders() {
        Response createUser = UtilIT.createRandomUser();
        createUser.prettyPrint();
        String username = UtilIT.getUsernameFromResponse(createUser);
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);

        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
        createDataverseResponse.prettyPrint();
        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);

        JsonObjectBuilder datasetJson = Json.createObjectBuilder()
                .add("datasetVersion", Json.createObjectBuilder()
                        .add("metadataBlocks", Json.createObjectBuilder()
                                .add("citation", Json.createObjectBuilder()
                                        .add("fields", Json.createArrayBuilder()
                                                .add(Json.createObjectBuilder()
                                                        .add("typeName", "title")
                                                        .add("value", "FAIR Principles for Geospatial Data")
                                                        .add("typeClass", "primitive")
                                                        .add("multiple", false)
                                                )
                                                .add(Json.createObjectBuilder()
                                                        .add("value", Json.createArrayBuilder()
                                                                .add(Json.createObjectBuilder()
                                                                        .add("authorName",
                                                                                Json.createObjectBuilder()
                                                                                        .add("value", "Trisovic, Ana")
                                                                                        .add("typeClass", "primitive")
                                                                                        .add("multiple", false)
                                                                                        .add("typeName", "authorName"))
                                                                )
                                                        )
                                                        .add("typeClass", "compound")
                                                        .add("multiple", true)
                                                        .add("typeName", "author")
                                                )
                                                .add(Json.createObjectBuilder()
                                                        .add("value", Json.createArrayBuilder()
                                                                .add(Json.createObjectBuilder()
                                                                        .add("datasetContactEmail",
                                                                                Json.createObjectBuilder()
                                                                                        .add("value", "dataverse@mailinator.com")
                                                                                        .add("typeClass", "primitive")
                                                                                        .add("multiple", false)
                                                                                        .add("typeName", "datasetContactEmail"))
                                                                )
                                                        )
                                                        .add("typeClass", "compound")
                                                        .add("multiple", true)
                                                        .add("typeName", "datasetContact")
                                                )
                                                .add(Json.createObjectBuilder()
                                                        .add("value", Json.createArrayBuilder()
                                                                .add(Json.createObjectBuilder()
                                                                        .add("dsDescriptionValue",
                                                                                Json.createObjectBuilder()
                                                                                        .add("value", "An integration with Jupyter Binder that allows exploration and viewing of complex high-dimensional data from Dataverse.")
                                                                                        .add("typeClass", "primitive")
                                                                                        .add("multiple", false)
                                                                                        .add("typeName", "dsDescriptionValue"))
                                                                )
                                                        )
                                                        .add("typeClass", "compound")
                                                        .add("multiple", true)
                                                        .add("typeName", "dsDescription")
                                                )
                                                .add(Json.createObjectBuilder()
                                                        .add("value", Json.createArrayBuilder()
                                                                .add("Other")
                                                        )
                                                        .add("typeClass", "controlledVocabulary")
                                                        .add("multiple", true)
                                                        .add("typeName", "subject")
                                                )
                                                .add(Json.createObjectBuilder()
                                                        .add("value", Json.createArrayBuilder()
                                                                .add(Json.createObjectBuilder()
                                                                        .add("grantNumberAgency",
                                                                                Json.createObjectBuilder()
                                                                                        .add("value", "http://dx.doi.org/10.13039/100000002")
                                                                                        .add("typeClass", "primitive")
                                                                                        .add("multiple", false)
                                                                                        .add("typeName", "grantNumberAgency"))
                                                                        .add("grantNumberValue",
                                                                                Json.createObjectBuilder()
                                                                                        .add("value", "3R01AG066793-02S2")
                                                                                        .add("typeClass", "primitive")
                                                                                        .add("multiple", false)
                                                                                        .add("typeName", "grantNumberValue"))
                                                                )
                                                        )
                                                        .add("typeClass", "compound")
                                                        .add("multiple", true)
                                                        .add("typeName", "grantNumber")
                                                )
                                        )
                                )
                        ));

        Response createDatasetResponse = UtilIT.createDataset(dataverseAlias, datasetJson, apiToken);
        createDatasetResponse.prettyPrint();
        Integer datasetId = UtilIT.getDatasetIdFromResponse(createDatasetResponse);
        String datasetPid = JsonPath.from(createDatasetResponse.getBody().asString()).getString("data.persistentId");

    }

}
