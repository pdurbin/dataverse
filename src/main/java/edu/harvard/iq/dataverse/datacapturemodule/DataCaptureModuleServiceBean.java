package edu.harvard.iq.dataverse.datacapturemodule;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;

import static edu.harvard.iq.dataverse.settings.SettingsServiceBean.Key.DataCaptureModuleUrl;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This class contains all the methods that have external runtime dependencies
 * such as the Data Capture Module itself and PostgreSQL.
 */
@Stateless
@Named
public class DataCaptureModuleServiceBean implements Serializable {

    private static final Logger logger = Logger.getLogger(DataCaptureModuleServiceBean.class.getCanonicalName());

    private static final String UploadRequestPath = "/ur.py";
    private static final String ScriptRequestPath = "/sr.py?datasetIdentifier=";

    @EJB
    SettingsServiceBean settingsService;

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;

    /**
     *
     * @param json the JSON request body to send to the DCM implementation
     * @return Unirest response as JSON or null.
     * @throws Exception if Data Capture Module URL hasn't been configured or if
     * the POST failed for any reason.
     */
//    public String requestRsyncScriptCreation(String json) throws Exception {
    public HttpResponse<String> requestRsyncScriptCreation(String json) throws UnirestException {

        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        org.apache.http.HttpResponse response = factory.newHttpResponse(
                new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);

        // make sure we have a DCM URL defined first
        String dcmBaseUrl = settingsService.getValueForKey(DataCaptureModuleUrl);
//        if (dcmBaseUrl == null) {
//            throw new Exception("Problem POSTing JSON to Data Capture Module. The '" + DataCaptureModuleUrl
//                    + "' setting has not been configured.");
//        }

        // mock response
//        if (dcmBaseUrl.equalsIgnoreCase("mock")) {
//            return "received";
//        }

        // get DCM's text response (e.g., "received") and return it as json
        RequestBodyEntity rbe = Unirest.post(dcmBaseUrl + UploadRequestPath).body(json);
        HttpResponse<String> httpResponse = rbe.asString();
        return httpResponse;
//        logger.log(Level.INFO, "[DCM] upload request response: " + httpResponse.getBody());
//        int status = httpResponse.getStatus();
//        if (status == 403) {
//            throw new NullPointerException("Caught 403");
//        }
//        return httpResponse.getBody();
    }

    /**
     *
     * @param dataset the dataset object
     * @return Unirest response as JSON or null.
     * @throws Exception
     */
    public HttpResponse<JsonNode> retreiveRequestedRsyncScript(Dataset dataset) throws Exception {

        // make sure we have a DCM URL defined first
        String dcmBaseUrl = settingsService.getValueForKey(DataCaptureModuleUrl);
        if (dcmBaseUrl == null) {
            throw new Exception("Problem GETing JSON to Data Capture Module for dataset " + dataset.getId()
                    + " The '" + DataCaptureModuleUrl + "' setting has not been configured.");
        }

        // mock response
        if (dcmBaseUrl.equalsIgnoreCase("mock")) {
            HttpResponseFactory factory = new DefaultHttpResponseFactory();
            org.apache.http.HttpResponse response = factory.newHttpResponse(
                    new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
            response.setEntity(new StringEntity("{'datasetId' : " + dataset.getId()
                    + ", 'userId' : '@dataverseAdmin', 'datasetIdentifier' : '" + dataset.getIdentifier()
                    + "', 'script':'script goes here'}"));
            com.mashape.unirest.http.HttpResponse<JsonNode> httpResponse
                    = new HttpResponse<>(response, JsonNode.class);
            return httpResponse;
        }

        logger.log(Level.INFO, "Rsync script request URL: " + dcmBaseUrl + ScriptRequestPath + dataset.getIdentifier());
        HttpResponse<JsonNode> jsNode = Unirest.get(dcmBaseUrl + ScriptRequestPath + dataset.getIdentifier()).asJson();
        logger.log(Level.INFO, "sr.py status: " + jsNode.getStatusText() + " json: " + jsNode.getBody().toString());
        return jsNode;
    }

    /**
     *
     * @param dataset
     * @param script
     * @return
     */
    public Dataset persistRsyncScript(Dataset dataset, String script) {
        dataset.setRsyncScript(script);
        return em.merge(dataset);
    }

}
