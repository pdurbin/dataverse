package edu.harvard.iq.dataverse.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.logging.Logger;

import static edu.harvard.iq.dataverse.api.ApiConstants.DS_VERSION_LATEST;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

public class Test12551IT {

    private static final Logger logger = Logger.getLogger(Test12551IT.class.getCanonicalName());

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    // DATAVERSE_FEATURE_DO_NOT_ASSUME_DEFAULT_LICENSE=false (or undefined)
    @Test
    public void testLicenseFlagOff() {
        Response createUser = UtilIT.createRandomUser();
        createUser.prettyPrint();
        createUser.then().assertThat()
                .statusCode(OK.getStatusCode());
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);
        String collectionAlias = UtilIT.createRandomCollectionGetAlias(apiToken);
        Response createDataset = UtilIT.createDataset(collectionAlias, datasetJson, apiToken);
        createDataset.prettyPrint();
        createDataset.then().assertThat().statusCode(CREATED.getStatusCode());
        String datasetPersistentId = UtilIT.getDatasetPersistentIdFromResponse(createDataset);
        Response getDatasetVersion = UtilIT.getDatasetVersion(datasetPersistentId, DS_VERSION_LATEST, apiToken);
        getDatasetVersion.prettyPrint();
        getDatasetVersion.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("data.license.name", equalTo("CC0 1.0"))
                .body("data.license.rightsIdentifier", equalTo("CC0-1.0"));
    }

    // DATAVERSE_FEATURE_DO_NOT_ASSUME_DEFAULT_LICENSE=true
    @Test
    public void testLicenseFlagOn() {
        Response createUser = UtilIT.createRandomUser();
        createUser.prettyPrint();
        createUser.then().assertThat()
                .statusCode(OK.getStatusCode());
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);
        String collectionAlias = UtilIT.createRandomCollectionGetAlias(apiToken);
        Response createDataset = UtilIT.createDataset(collectionAlias, datasetJson, apiToken);
        createDataset.prettyPrint();
        createDataset.then().assertThat().statusCode(CREATED.getStatusCode());
        String datasetPersistentId = UtilIT.getDatasetPersistentIdFromResponse(createDataset);
        Response getDatasetVersion = UtilIT.getDatasetVersion(datasetPersistentId, DS_VERSION_LATEST, apiToken);
        getDatasetVersion.prettyPrint();
        getDatasetVersion.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("data.license", equalTo(null));
    }

    String datasetJson = """
            {
              "datasetVersion": {
                "metadataBlocks": {
                  "citation": {
                    "fields": [
                      {
                        "multiple": false,
                        "typeClass": "primitive",
                        "typeName": "title",
                        "value": "My Dataset"
                      },
                      {
                        "multiple": true,
                        "typeClass": "controlledVocabulary",
                        "typeName": "subject",
                        "value": [
                          "Other"
                        ]
                      },
                      {
                        "multiple": true,
                        "typeClass": "compound",
                        "typeName": "author",
                        "value": [
                          {
                            "authorName": {
                              "multiple": false,
                              "typeClass": "primitive",
                              "typeName": "authorName",
                              "value": "Durbin, Philip"
                            }
                          }
                        ]
                      },
                      {
                        "multiple": true,
                        "typeClass": "compound",
                        "typeName": "datasetContact",
                        "value": [
                          {
                            "datasetContactName": {
                              "multiple": false,
                              "typeClass": "primitive",
                              "typeName": "datasetContactName",
                              "value": "Durbin, Philip"
                            },
                            "datasetContactEmail": {
                              "multiple": false,
                              "typeClass": "primitive",
                              "typeName": "datasetContactEmail",
                              "value": "philip_durbin@harvard.edu"
                            }
                          }
                        ]
                      },
                      {
                        "multiple": true,
                        "typeClass": "compound",
                        "typeName": "dsDescription",
                        "value": [
                          {
                            "dsDescriptionValue": {
                              "multiple": false,
                              "typeClass": "primitive",
                              "typeName": "dsDescriptionValue",
                              "value": "My description."
                            }
                          }
                        ]
                      }
                    ]
                  }
                }
              }
            }
                    """;

}
