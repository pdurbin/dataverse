package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.BeforeClass;
import org.junit.Test;

public class NetcdfIT {

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    @Test
    public void testNmclFromNetcdf() throws IOException {
        Response createUser = UtilIT.createRandomUser();
        createUser.then().assertThat().statusCode(OK.getStatusCode());
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);
        String username = UtilIT.getUsernameFromResponse(createUser);

//        Response createDataverseResponse = UtilIT.createRandomDataverse(apiToken);
//        createDataverseResponse.prettyPrint();
//        createDataverseResponse.then().assertThat()
//                .statusCode(CREATED.getStatusCode());
//
//        String dataverseAlias = UtilIT.getAliasFromResponse(createDataverseResponse);
//        Response createDataset = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias, apiToken);
        Response createDataset = UtilIT.createRandomDatasetViaNativeApi("root", apiToken);
        createDataset.prettyPrint();
        createDataset.then().assertThat()
                .statusCode(CREATED.getStatusCode());

        Integer datasetId = UtilIT.getDatasetIdFromResponse(createDataset);
        String datasetPid = UtilIT.getDatasetPersistentIdFromResponse(createDataset);

//        String pathToJsonFile = "doc/sphinx-guides/source/_static/api/dataset-add-metadata.json";
//        Response addSubjectViaNative = UtilIT.addDatasetMetadataViaNative(datasetPid, pathToJsonFile, apiToken);
//        JsonObjectBuilder jsonUpdateObject = Json.createObjectBuilder().add("fields",
//                Json.createArrayBuilder()
//                        .add(Json.createObjectBuilder()
//                                //                                .add("typeName", "subtitle")
//                                //                                .add("value", "MySubtitle ")
////                                .add("typeName", "author")
////                                .add("value", Json.createArrayBuilder()
////                                        .add(Json.createObjectBuilder()
////                                                .add("authorName", Json.createObjectBuilder()
////                                                        .add("typeName", "authorName")
////                                                        .add("value", "Simpson, Homer")
////                                                )
////                                        )
////                                )
//                                .add("typeName", "geographicCoverage")
//                                .add("value", Json.createArrayBuilder()
//                                        .add(Json.createObjectBuilder()
//                                                .add("city", Json.createObjectBuilder()
//                                                        .add("typeName", "city")
//                                                        .add("value", "Boston")
//                                                )
//                                        )
//                                )
//                        ));
//        JsonObjectBuilder jsonUpdateObject
//                = Json.createObjectBuilder()
//                        .add("typeName", "geographicCoverage")
//                        .add("value", Json.createArrayBuilder()
//                                .add(Json.createObjectBuilder()
//                                        .add("city", Json.createObjectBuilder()
//                                                .add("typeName", "city")
//                                                .add("value", "Boston")
//                                        )
//                                ));
//        String jsonUpdateString = jsonUpdateObject.build().toString();
//        System.out.println("json: " + jsonUpdateString);
//        Path jsonUpdatePath = Paths.get(java.nio.file.Files.createTempDirectory(null) + File.separator + "update.json");
//        java.nio.file.Files.write(jsonUpdatePath, jsonUpdateString.getBytes());
//        Response addSubjectViaNative = UtilIT.addDatasetMetadataViaNative(datasetPid, jsonUpdatePath.toString(), apiToken);
//        addSubjectViaNative.prettyPrint();
//        addSubjectViaNative.then().assertThat().statusCode(OK.getStatusCode());
//        Response getJson1 = UtilIT.nativeGet(datasetId, apiToken);
//        getJson1.prettyPrint();
//        getJson1.then().assertThat()
//                .statusCode(OK.getStatusCode());
//        if (true) {
//            return;
//        }
//        JsonObjectBuilder jsonUpdateObject
//                = Json.createObjectBuilder()
//                        .add("typeName", "geographicCoverage")
//                        .add("value", Json.createArrayBuilder()
//                                .add(Json.createObjectBuilder()
//                                        .add("otherGeographicCoverage", Json.createObjectBuilder()
//                                                .add("typeName", "otherGeographicCoverage")
//                                                .add("value", "New England")
//                                        )
//                                ));
//        String jsonUpdateString = jsonUpdateObject.build().toString();
//        System.out.println("json: " + jsonUpdateString);
//        Path jsonUpdatePath = Paths.get(java.nio.file.Files.createTempDirectory(null) + File.separator + "update.json");
//        java.nio.file.Files.write(jsonUpdatePath, jsonUpdateString.getBytes());
//        Response addSubjectViaNative = UtilIT.addDatasetMetadataViaNative(datasetPid, jsonUpdatePath.toString(), apiToken);
//        addSubjectViaNative.prettyPrint();
//        addSubjectViaNative.then().assertThat().statusCode(OK.getStatusCode());
//        Response getJson1 = UtilIT.nativeGet(datasetId, apiToken);
//        getJson1.prettyPrint();
//        getJson1.then().assertThat()
//                .statusCode(OK.getStatusCode());

        String pathToFile = "src/test/resources/netcdf/madis-raob";
        // https://www.ncei.noaa.gov/data/international-comprehensive-ocean-atmosphere/v3/archive/nrt/ICOADS_R3.0.0_1662-10.nc
        // via https://data.noaa.gov/onestop/collections/details/9bd5c743-0684-4e70-817a-ed977117f80c?f=temporalResolution:1%20Minute%20-%20%3C%201%20Hour;dataFormats:NETCDF
        pathToFile = "src/test/resources/netcdf/ICOADS_R3.0.0_1662-10.nc";
//        pathToFile = "/Users/pdurbin/Downloads/perseus60.fits";

        Response uploadFile = UtilIT.uploadFileViaNative(datasetId.toString(), pathToFile, apiToken);
        uploadFile.prettyPrint();
        uploadFile.then().assertThat().statusCode(OK.getStatusCode());

        Response getJson = UtilIT.nativeGet(datasetId, apiToken);
        getJson.prettyPrint();
        getJson.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("data.latestVersion.metadataBlocks.geospatial.fields[0].value[0]", equalTo("Boston"));
        if (true) {
            System.out.println("done...");
            return;
        }

        long fileId = JsonPath.from(uploadFile.body().asString()).getLong("data.files[0].dataFile.id");
        String tag = "NcML";
        String version = "0.1";

        Response downloadNcml = UtilIT.downloadAuxFile(fileId, tag, version, apiToken);
        //downloadNcml.prettyPrint(); // long output
        downloadNcml.then().assertThat()
                .statusCode(OK.getStatusCode())
                .contentType("text/xml; name=\"madis-raob.ncml_0.1.xml\";charset=UTF-8");

        Response deleteNcml = UtilIT.deleteAuxFile(fileId, tag, version, apiToken);
        deleteNcml.prettyPrint();
        deleteNcml.then().assertThat().statusCode(OK.getStatusCode());

        Response downloadNcmlShouldFail = UtilIT.downloadAuxFile(fileId, tag, version, apiToken);
        downloadNcmlShouldFail.then().assertThat()
                .statusCode(NOT_FOUND.getStatusCode());

        UtilIT.makeSuperUser(username).then().assertThat().statusCode(OK.getStatusCode());

        Response extractNcml = UtilIT.extractNcml(fileId, apiToken);
        extractNcml.prettyPrint();
        extractNcml.then().assertThat()
                .statusCode(OK.getStatusCode());

        Response downloadNcmlShouldWork = UtilIT.downloadAuxFile(fileId, tag, version, apiToken);
        downloadNcmlShouldWork.then().assertThat()
                .statusCode(OK.getStatusCode());

    }

    @Test
    public void testNmclFromNetcdfErrorChecking() throws IOException {
        Response createUser = UtilIT.createRandomUser();
        createUser.then().assertThat().statusCode(OK.getStatusCode());
        String apiToken = UtilIT.getApiTokenFromResponse(createUser);
        String username = UtilIT.getUsernameFromResponse(createUser);

        Response createUserRandom = UtilIT.createRandomUser();
        createUserRandom.then().assertThat().statusCode(OK.getStatusCode());
        String apiTokenRandom = UtilIT.getApiTokenFromResponse(createUserRandom);

        String apiTokenNull = null;

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

        String pathToFile = "src/test/resources/netcdf/madis-raob";

        Response uploadFile = UtilIT.uploadFileViaNative(datasetId.toString(), pathToFile, apiToken);
        uploadFile.prettyPrint();
        uploadFile.then().assertThat().statusCode(OK.getStatusCode());

        long fileId = JsonPath.from(uploadFile.body().asString()).getLong("data.files[0].dataFile.id");
        String tag = "NcML";
        String version = "0.1";

        Response downloadNcmlFail = UtilIT.downloadAuxFile(fileId, tag, version, apiTokenNull);
        downloadNcmlFail.then().assertThat()
                .statusCode(FORBIDDEN.getStatusCode());

        Response downloadNcml = UtilIT.downloadAuxFile(fileId, tag, version, apiToken);
        downloadNcml.then().assertThat()
                .statusCode(OK.getStatusCode())
                .contentType("text/xml; name=\"madis-raob.ncml_0.1.xml\";charset=UTF-8");

        Response deleteNcml = UtilIT.deleteAuxFile(fileId, tag, version, apiToken);
        deleteNcml.prettyPrint();
        deleteNcml.then().assertThat().statusCode(OK.getStatusCode());

        Response downloadNcmlShouldFail = UtilIT.downloadAuxFile(fileId, tag, version, apiToken);
        downloadNcmlShouldFail.then().assertThat()
                .statusCode(NOT_FOUND.getStatusCode());

        Response extractNcmlFailRandomUser = UtilIT.extractNcml(fileId, apiTokenRandom);
        extractNcmlFailRandomUser.prettyPrint();
        extractNcmlFailRandomUser.then().assertThat()
                .statusCode(FORBIDDEN.getStatusCode());

        UtilIT.makeSuperUser(username).then().assertThat().statusCode(OK.getStatusCode());

        Response extractNcml = UtilIT.extractNcml(fileId, apiToken);
        extractNcml.prettyPrint();
        extractNcml.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("data.result", CoreMatchers.equalTo(true));

        Response downloadNcmlShouldWork = UtilIT.downloadAuxFile(fileId, tag, version, apiToken);
        downloadNcmlShouldWork.then().assertThat()
                .statusCode(OK.getStatusCode());

        Response extractNcmlFailExistsAlready = UtilIT.extractNcml(fileId, apiToken);
        extractNcmlFailExistsAlready.prettyPrint();
        extractNcmlFailExistsAlready.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("data.result", CoreMatchers.equalTo(false));

        Path pathToTxt = Paths.get(java.nio.file.Files.createTempDirectory(null) + File.separator + "file.txt");
        String contentOfTxt = "Just a text file. Don't expect NcML out!";
        java.nio.file.Files.write(pathToTxt, contentOfTxt.getBytes());

        Response uploadFileTxt = UtilIT.uploadFileViaNative(datasetId.toString(), pathToTxt.toString(), apiToken);
        uploadFileTxt.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("data.files[0].label", equalTo("file.txt"));

        long fileIdTxt = JsonPath.from(uploadFileTxt.body().asString()).getLong("data.files[0].dataFile.id");

        Response extractNcmlFailText = UtilIT.extractNcml(fileIdTxt, apiToken);
        extractNcmlFailText.prettyPrint();
        extractNcmlFailText.then().assertThat()
                .statusCode(OK.getStatusCode())
                .body("data.result", CoreMatchers.equalTo(false));

    }

}
