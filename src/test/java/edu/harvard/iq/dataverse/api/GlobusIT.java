package edu.harvard.iq.dataverse.api;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import static edu.harvard.iq.dataverse.api.S3AccessIT.BUCKET_NAME;
import static edu.harvard.iq.dataverse.api.S3AccessIT.s3localstack;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GlobusIT {

    @BeforeAll
    public static void setUp() {
//        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
        RestAssured.baseURI = "http://ec2-34-204-169-194.compute-1.amazonaws.com";
    }

    @Test
    public void testGlobus() {
        String apiToken = "";
        String dataverseAlias = "hdcdiskpoc";

        Response createDatasetResponse = UtilIT.createRandomDatasetViaNativeApi(dataverseAlias, apiToken);
        createDatasetResponse.prettyPrint();
        createDatasetResponse.then().assertThat().statusCode(201);
        Integer datasetId = JsonPath.from(createDatasetResponse.body().asString()).getInt("data.id");
        String datasetPid = JsonPath.from(createDatasetResponse.body().asString()).getString("data.persistentId");
        String datasetStorageIdentifier = datasetPid.substring(4);

        Response getDatasetMetadata = UtilIT.nativeGet(datasetId, apiToken);
        getDatasetMetadata.prettyPrint();
        getDatasetMetadata.then().assertThat().statusCode(200);

//        //upload a tabular file via native, check storage id prefix for driverId
//        String pathToFile = "scripts/search/data/tabular/1char";
//        Response addFileResponse = UtilIT.uploadFileViaNative(datasetId.toString(), pathToFile, apiToken);
//        addFileResponse.prettyPrint();
//        addFileResponse.then().assertThat()
//                .statusCode(200)
//                .body("data.files[0].dataFile.storageIdentifier", startsWith(driverId + "://"));
//
//        String fileId = JsonPath.from(addFileResponse.body().asString()).getString("data.files[0].dataFile.id");
        long size = 1000000000l;
        // curl -H "X-Dataverse-key:$API_TOKEN" "$SERVER_URL/api/datasets/:persistentId/globusUploadParameters?locale=$LOCALE"
        String locale = "en";
//        Response getGlobusUploadParams = UtilIT.getGlobusUploadParameters(datasetPid, locale, apiToken);
        Response getGlobusUploadParams = UtilIT.getDatasetGlobusUploadParameters(datasetId, locale, apiToken);
        getGlobusUploadParams.prettyPrint();
        getGlobusUploadParams.then().assertThat()
                .statusCode(200);

        // id of globus endpoint:        "endpoint": "545f40d6-140f-404f-a482-7594ac0ddd6a"
        //        "managed": "true",
        String globus1 = """
{
   "status": "OK",
   "data": {
     "queryParameters": {
       "datasetId": 77,
       "siteUrl": "http://ec2-34-204-169-194.compute-1.amazonaws.com",
       "datasetVersion": ":draft",
       "dvLocale": "en",
       "datasetPid": "doi:10.5072/FK2/INDEFV",
       "managed": "true",
       "endpoint": "545f40d6-140f-404f-a482-7594ac0ddd6a"
     },
     "signedUrls": [
       {
         "name": "requestGlobusTransferPaths",
         "httpMethod": "POST",
         "signedUrl": "http://ec2-34-204-169-194.compute-1.amazonaws.com/api/v1/datasets/77/requestGlobusUploadPaths?until=2024-01-10T15:49:29.402&user=dataverseAdmin&method=POST&token=a15079b62beeca919ff46330f78e284e41207d47d45359c2626f1e945f07cab331d6f8afc618bd710e6afdbe3e65b5142f17d1b6adc2bfdaee7482363c89b400",
         "timeOut": 5
       },
       {
         "name": "addGlobusFiles",
         "httpMethod": "POST",
         "signedUrl": "http://ec2-34-204-169-194.compute-1.amazonaws.com/api/v1/datasets/77/addGlobusFiles?until=2024-01-10T15:49:29.403&user=dataverseAdmin&method=POST&token=67f9e6ae99ea534e19b1e2be7c91911edf20ed44cb8bf32623325c57b9637b705e270c2df1e56f1c5632942bfb2835f84bc0b0847b02c47b05ce77b29f363544",
         "timeOut": 5
       },
       {
         "name": "getDatasetMetadata",
         "httpMethod": "GET",
         "signedUrl": "http://ec2-34-204-169-194.compute-1.amazonaws.com/api/v1/datasets/77/versions/:draft?until=2024-01-10T15:49:29.404&user=dataverseAdmin&method=GET&token=fcf84747e64372fa38d4a2d57572d23662abe584b598d4d9b161dc6e358292640f98bf665127adfbc3af3d28aa04705e8a4b617ba55293bbf027dba7090ec426",
         "timeOut": 5
       },
       {
         "name": "getFileListing",
         "httpMethod": "GET",
         "signedUrl": "http://ec2-34-204-169-194.compute-1.amazonaws.com/api/v1/datasets/77/versions/:draft/files?until=2024-01-10T15:49:29.405&user=dataverseAdmin&method=GET&token=fb625be8dfade207724491f0d2b9f4156b1f127b6a1f6a977851cec4db5a00500a11c565453bc41102dc87e18f7fff983cddff228250c66ceb652d826dd22879",
         "timeOut": 5
       }
     ]
   }
 }
""";
        String principalOfGlobusUser = "aecd33cd-6bf5-4459-989c-3a744832b96f";

        JsonObjectBuilder uploadRequestJson = Json.createObjectBuilder()
                .add("principal", principalOfGlobusUser)
                .add("numberOfFiles", 1);
        
        Response requestGlobusUpload = UtilIT.requestGlobusUploadPaths(datasetId, uploadRequestJson.build(), apiToken);
        requestGlobusUpload.prettyPrint();
        requestGlobusUpload.then().assertThat()
                .statusCode(667);
//{
//    "status": "OK",
//    "data": {
//        "globusHDC://18cf967ce75-9b92c9a5d91e": "/DVTesting/10.5072/FK2/CIDEC3/18cf967ce75-9b92c9a5d91e"
//    }
//}
//         "globusHDC://18cfa2e66a3-e111a18479ac": "/DVTesting/10.5072/FK2/IYBOI7/18cfa2e66a3-e111a18479ac"

// - create file in pdurbin/globus01/uuid
// - in globus gui:
// "endpoint": "545f40d6-140f-404f-a482-7594ac0ddd6a"
// path: /DVTesting/10.5072/FK2/CIDEC3
// drag/transfer file
// gives you a task id
/*
export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
export SERVER_URL=https://demo.dataverse.org
export PERSISTENT_IDENTIFIER=doi:10.5072/FK27U7YBV
export JSON_DATA='{ "taskIdentifier": "f565fada-b0bf-11ee-be73-f11924dc2d22", "files": [ { "description": "My description.", "directoryLabel": "data/subdir1", "categories": [ "Data" ], "restrict": "false", "storageIdentifier": "globusHDC://18cfa351a55-b445cb66a71b", "fileName": "file1.txt", "mimeType": "text/plain", "checksum": { "@type": "MD5", "@value": "1234" } } ] }'

AAaa

curl -H "X-Dataverse-key:FIXME" -H "Content-type:multipart/form-data" -X POST "http://ec2-34-204-169-194.compute-1.amazonaws.com/api/datasets/83/addGlobusFiles" -F "jsonData=$JSON_DATA"

{"status":"OK","data":{"message":"Async call to Globus Upload started "}}HMDC-beamish:dataverse pdurbin$ 

*/
        if (true) {
            System.out.println("returning early");
            return;
        }
        
        Response getUploadUrls = UtilIT.getUploadUrls(datasetPid, size, apiToken);
        getUploadUrls.prettyPrint();
        getUploadUrls.then().assertThat().statusCode(200);

        String url = JsonPath.from(getUploadUrls.asString()).getString("data.url");
        String partSize = JsonPath.from(getUploadUrls.asString()).getString("data.partSize");
        String storageIdentifier = JsonPath.from(getUploadUrls.asString()).getString("data.storageIdentifier");
        System.out.println("url: " + url);
        System.out.println("partSize: " + partSize);
        System.out.println("storageIdentifier: " + storageIdentifier);

        System.out.println("uploading file via direct upload");
        String decodedUrl = null;
        try {
            decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
        }

        // change to localhost because LocalStack is running in a container locally
        String localhostUrl = decodedUrl.replace("http://localstack", "http://localhost");
        String contentsOfFile = "foobar";

        InputStream inputStream = new ByteArrayInputStream(contentsOfFile.getBytes(StandardCharsets.UTF_8));
        Response uploadFileDirect = UtilIT.uploadFileDirect(localhostUrl, inputStream);
        uploadFileDirect.prettyPrint();
        /*
        Direct upload to MinIO is failing with errors like this:
        <Error>
          <Code>SignatureDoesNotMatch</Code>
          <Message>The request signature we calculated does not match the signature you provided. Check your key and signing method.</Message>
          <Key>10.5072/FK2/KGFCEJ/18b8c06688c-21b8320a3ee5</Key>
          <BucketName>mybucket</BucketName>
          <Resource>/mybucket/10.5072/FK2/KGFCEJ/18b8c06688c-21b8320a3ee5</Resource>
          <RequestId>1793915CCC5BC95C</RequestId>
          <HostId>dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8</HostId>
        </Error>
         */
        uploadFileDirect.then().assertThat().statusCode(200);

        // TODO: Use MD5 or whatever Dataverse is configured for and
        // actually calculate it.
        String jsonData = """
{
    "description": "My description.",
    "directoryLabel": "data/subdir1",
    "categories": [
      "Data"
    ],
    "restrict": "false",
    "storageIdentifier": "%s",
    "fileName": "file1.txt",
    "mimeType": "text/plain",
    "checksum": {
      "@type": "SHA-1",
      "@value": "123456"
    }
}
""".formatted(storageIdentifier);

        // "There was an error when trying to add the new file. File size must be explicitly specified when creating DataFiles with Direct Upload"
        Response addRemoteFile = UtilIT.addRemoteFile(datasetId.toString(), jsonData, apiToken);
        addRemoteFile.prettyPrint();
        addRemoteFile.then().assertThat()
                .statusCode(200);

        String fileId = JsonPath.from(addRemoteFile.asString()).getString("data.files[0].dataFile.id");
        Response getfileMetadata = UtilIT.getFileData(fileId, apiToken);
        getfileMetadata.prettyPrint();
        getfileMetadata.then().assertThat().statusCode(200);

//        String storageIdentifier = JsonPath.from(addFileResponse.body().asString()).getString("data.files[0].dataFile.storageIdentifier");
        String keyInDataverse = storageIdentifier.split(":")[2];
//        Assertions.assertEquals(driverId + "://" + BUCKET_NAME + ":" + keyInDataverse, storageIdentifier);

        String keyInS3 = datasetStorageIdentifier + "/" + keyInDataverse;
        String s3Object = s3localstack.getObjectAsString(BUCKET_NAME, keyInS3);
        System.out.println("s3Object: " + s3Object);

//        assertEquals(contentsOfFile.trim(), s3Object.trim());
        assertEquals(contentsOfFile, s3Object);

        System.out.println("direct download...");
        Response getHeaders = UtilIT.downloadFileNoRedirect(Integer.valueOf(fileId), apiToken);
        for (Header header : getHeaders.getHeaders()) {
            System.out.println("direct download header: " + header);
        }
        getHeaders.then().assertThat().statusCode(303);

        String urlFromResponse = getHeaders.getHeader("Location");
        String localhostDownloadUrl = urlFromResponse.replace("localstack", "localhost");
        String decodedDownloadUrl = null;
        try {
            decodedDownloadUrl = URLDecoder.decode(localhostDownloadUrl, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
        }

        Response downloadFile = UtilIT.downloadFromUrl(decodedDownloadUrl);
        downloadFile.prettyPrint();
        downloadFile.then().assertThat().statusCode(200);

        String contentsOfDownloadedFile = downloadFile.getBody().asString();
        assertEquals(contentsOfFile, contentsOfDownloadedFile);

        Response deleteFile = UtilIT.deleteFileApi(Integer.parseInt(fileId), apiToken);
        deleteFile.prettyPrint();
        deleteFile.then().assertThat().statusCode(200);

        AmazonS3Exception expectedException = null;
        try {
            s3localstack.getObjectAsString(BUCKET_NAME, keyInS3);
        } catch (AmazonS3Exception ex) {
            expectedException = ex;
        }
        assertNotNull(expectedException);
        // 404 because the file has been sucessfully deleted
        assertEquals(404, expectedException.getStatusCode());

    }


}
