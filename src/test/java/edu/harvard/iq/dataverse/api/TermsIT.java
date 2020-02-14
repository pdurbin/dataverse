package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.response.Response;
import javax.json.Json;
import javax.json.JsonObject;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.BeforeClass;
import org.junit.Test;

public class TermsIT {

    @BeforeClass
    public static void setUpClass() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    @Test
    public void testGetTerms() {
        String apiToken = "f976d31b-a0cf-4f44-8b12-d4e0b340ded1";
        String idOrPersistentIdOfDataset = null;
        idOrPersistentIdOfDataset = "doi:10.5072/FK2/E2MORS";
        Response terms = UtilIT.getTerms(idOrPersistentIdOfDataset, null, apiToken);
        terms.prettyPrint();
    }

    @Test
    public void testSetDisclaimer() {
        String apiToken = "f976d31b-a0cf-4f44-8b12-d4e0b340ded1";
        String idOrPersistentIdOfDataset = null;
        idOrPersistentIdOfDataset = "doi:10.5072/FK2/E2MORS";
        Response terms = UtilIT.getTerms(idOrPersistentIdOfDataset, null, apiToken);
        terms.prettyPrint();

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("disclaimer", "myDisclaimer").build();
        Response updateTerms = UtilIT.updateTerms(idOrPersistentIdOfDataset, jsonObject, apiToken);
        updateTerms.prettyPrint();

    }

    @Test
    public void testRemoveDisclaimer() {
        String apiToken = "f976d31b-a0cf-4f44-8b12-d4e0b340ded1";
        String idOrPersistentIdOfDataset = null;
        idOrPersistentIdOfDataset = "doi:10.5072/FK2/E2MORS";
        Response termsBefore = UtilIT.getTerms(idOrPersistentIdOfDataset, null, apiToken);
        termsBefore.prettyPrint();

        String termToDelete = "disclaimer";
        Response updateTerms = UtilIT.removeTerms(idOrPersistentIdOfDataset, termToDelete, apiToken);
        updateTerms.prettyPrint();

        Response termsAfter = UtilIT.getTerms(idOrPersistentIdOfDataset, null, apiToken);
        termsAfter.prettyPrint();

    }

}
