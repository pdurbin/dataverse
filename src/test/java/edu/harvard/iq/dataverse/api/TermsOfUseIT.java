package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.response.Response;
import edu.harvard.iq.dataverse.api.datadeposit.SwordConfigurationImpl;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

public class TermsOfUseIT {

    private static final Logger logger = Logger.getLogger(DatasetsIT.class.getCanonicalName());
    private static final String EMPTY_STRING = "";
    private static SwordConfigurationImpl swordConfiguration = new SwordConfigurationImpl();

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    @Test
    public void testTermsOfUseFileDownload() {

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

        // This is the example XML from http://guides.dataverse.org/en/4.17/api/sword.html#create-a-dataset-with-an-atom-entry
        String xmlIn = "<?xml version=\"1.0\"?>\n"
                + "<entry xmlns=\"http://www.w3.org/2005/Atom\"\n"
                + "       xmlns:dcterms=\"http://purl.org/dc/terms/\">\n"
                + "   <!-- some embedded metadata -->\n"
                + "   <dcterms:title>Roasting at Home</dcterms:title>\n"
                + "   <dcterms:creator>Peets, John</dcterms:creator>\n"
                + "   <dcterms:creator affiliation=\"Coffee Bean State University\">Stumptown, Jane</dcterms:creator>\n"
                + "   <!-- Dataverse controlled vocabulary subject term -->\n"
                + "   <dcterms:subject>Chemistry</dcterms:subject>\n"
                + "   <!-- keywords -->\n"
                + "   <dcterms:subject>coffee</dcterms:subject>\n"
                + "   <dcterms:subject>beverage</dcterms:subject>\n"
                + "   <dcterms:subject>caffeine</dcterms:subject>\n"
                + "   <dcterms:description>Considerations before you start roasting your own coffee at home.</dcterms:description>\n"
                + "   <!-- Producer with financial or admin responsibility of the data -->\n"
                + "   <dcterms:publisher>Coffee Bean State University</dcterms:publisher>\n"
                + "   <dcterms:contributor type=\"Funder\">CaffeineForAll</dcterms:contributor>\n"
                + "   <!-- production date -->\n"
                + "   <dcterms:date>2013-07-11</dcterms:date>\n"
                + "   <!-- kind of data -->\n"
                + "   <dcterms:type>aggregate data</dcterms:type>\n"
                + "   <!-- List of sources of the data collection-->\n"
                + "   <dcterms:source>Stumptown, Jane. 2011. Home Roasting. Coffeemill Press.</dcterms:source>\n"
                + "   <!-- related materials -->\n"
                + "   <dcterms:relation>Peets, John. 2010. Roasting Coffee at the Coffee Shop. Coffeemill Press</dcterms:relation>\n"
                + "   <!-- geographic coverage -->\n"
                + "   <dcterms:coverage>United States</dcterms:coverage>\n"
                + "   <dcterms:coverage>Canada</dcterms:coverage>\n"
                + "   <!-- license and restrictions -->\n"
                + "   <dcterms:license>NONE</dcterms:license>\n"
                + "   <dcterms:rights>Downloader will not use the Materials in any way prohibited by applicable laws.</dcterms:rights>\n"
                + "   <!-- related publications -->\n"
                + "   <dcterms:isReferencedBy holdingsURI=\"http://dx.doi.org/10.1038/dvn333\" agency=\"DOI\" IDNo=\"10.1038/dvn333\">Peets, J., &amp; Stumptown, J. (2013). Roasting at Home. New England Journal of Coffee, 3(1), 22-34.</dcterms:isReferencedBy>\n"
                + "</entry>";

        Response createDataset = given()
                .auth().basic(apiToken, EMPTY_STRING)
                .body(xmlIn)
                .contentType("application/atom+xml")
                .post(swordConfiguration.getBaseUrlPathCurrent() + "/collection/dataverse/" + dataverseAlias);

        createDataset.prettyPrint();
        String datasetPersistentId = UtilIT.getDatasetPersistentIdFromSwordResponse(createDataset);

        Response uploadFile = UtilIT.uploadRandomFile(datasetPersistentId, apiToken);
        uploadFile.then().assertThat()
                .statusCode(201);

        Response publishDataverse = UtilIT.publishDataverseViaSword(dataverseAlias, apiToken);
        assertEquals(200, publishDataverse.getStatusCode());

        Response publishDataset = UtilIT.publishDatasetViaNativeApi(datasetPersistentId, "major", apiToken);
        publishDataset.prettyPrint();
        assertEquals(200, publishDataset.getStatusCode());

        Response swordStatement = UtilIT.getSwordStatement(datasetPersistentId, apiToken);

        Integer fileId = UtilIT.getFileIdFromSwordStatementResponse(swordStatement);

        Response downloadFile = UtilIT.downloadFile(fileId);
        downloadFile.then().assertThat()
                // TODO: When we fix https://github.com/IQSS/dataverse/issues/2911
                // change this to a non 200 response. A download should not be possible
                // because the following terms of use are in place:
                // "Downloader will not use the Materials in any way prohibited by applicable laws."
                .statusCode(200);
    }

}
