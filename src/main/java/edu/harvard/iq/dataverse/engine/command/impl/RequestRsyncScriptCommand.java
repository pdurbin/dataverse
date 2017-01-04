package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.authorization.users.User;
import edu.harvard.iq.dataverse.engine.command.AbstractCommand;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.exception.CommandExecutionException;
import edu.harvard.iq.dataverse.engine.command.exception.PermissionException;
import edu.harvard.iq.dataverse.util.json.NullSafeJsonBuilder;
import org.apache.commons.lang.StringUtils;

import static edu.harvard.iq.dataverse.util.json.NullSafeJsonBuilder.jsonObjectBuilder;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

/**
 * Always catch a RuntimeException when calling this command, which may occur on
 * any problem contacting the Data Capture Module! We have to throw a
 * RuntimeException because otherwise ctxt.engine().submit() will put "OK" for
 * "actiontype" in the actionlogrecord rather than "InternalError" if you throw
 * a CommandExecutionException.
 *
 * @todo Who is responsible for knowing when it's appropriate to create an rsync
 * script for a dataset, Dataverse or the Data Capture Module? For now the DCM
 * will always create an rsync script, which may not be what we want.
 */
@RequiredPermissions(Permission.AddDataset)
public class RequestRsyncScriptCommand extends AbstractCommand<JsonObjectBuilder> {

    private static final Logger logger = Logger.getLogger(RequestRsyncScriptCommand.class.getCanonicalName());

    private final Dataset dataset;
    private final DataverseRequest request;
    private long millisecondsToSleep = 10000;

    public RequestRsyncScriptCommand(DataverseRequest requestArg, Dataset datasetArg) {
        super(requestArg, datasetArg);
        request = requestArg;
        dataset = datasetArg;
    }

    public RequestRsyncScriptCommand(DataverseRequest requestArg, Dataset datasetArg, long sleepArg) {
        super(requestArg, datasetArg);
        request = requestArg;
        dataset = datasetArg;
        millisecondsToSleep = sleepArg;
    }

    @Override
    public JsonObjectBuilder execute(CommandContext ctxt) throws CommandException {

        int statusCode;
        HttpResponse<JsonNode> response;
        User user = request.getUser();
        AuthenticatedUser authenticatedUser;
        Dataset updatedDataset;

        Long datasetId = dataset.getId();
        String userId = user.getIdentifier();
        String datasetIdentifier = dataset.getIdentifier();

        /**
         * @todo get Permission.AddDataset from above somehow rather than
         * duplicating it here.
         */
        if (user instanceof AuthenticatedUser) {
            authenticatedUser = (AuthenticatedUser) user;
        } else {
            throw new PermissionException("The DCM command can only be called by an AuthenticatedUser, not " + user,
                    this, Collections.singleton(Permission.AddDataset), dataset);
        }

        /**
         * @todo Refactor this building of JSON to make it testable.
         */
        JsonObjectBuilder jab = Json.createObjectBuilder();
        jab.add("userId", authenticatedUser.getIdentifier());
        jab.add("datasetId", dataset.getId());
        jab.add("datasetIdentifier", dataset.getIdentifier());
        String dcmRequest = jab.build().toString();

        // [1] Make DCM upload request and check text response
        try {
            HttpResponse<String> answer1 = ctxt.dataCaptureModule().requestRsyncScriptCreation(dcmRequest);
            if (answer1.getStatus() != 200) {
                String message = "Is the Data Capture Module (DCM) down? Instead of 200, the response code from DCM was "
                        + answer1.getStatus() + " (" + answer1.getStatusText() + ").";
                logger.info("Problem running " + RequestAccessCommand.class.getName() + ". " + message);
                throw new CommandExecutionException(message, this);
            }
            String answer = answer1.getBody().trim();
            if (answer.equalsIgnoreCase("recieved")) {
                answer = "received";
            }
            if (answer.equalsIgnoreCase("received")) {
                logger.log(Level.INFO, "DCM upload request succeeded: " + answer);
            } else {
                throw new RuntimeException("DCM upload request failed: " + dcmRequest + " Response:" + answer);
            }
        } catch (UnirestException ex) {
            throw new CommandExecutionException("DCM upload request failed: " + dcmRequest + ex.getLocalizedMessage(), ex, this);
        }

        // [2] set the script as pending
        updatedDataset = ctxt.dataCaptureModule().persistRsyncScript(dataset, "pending");

        // don't wait, for now run this: count=0; while true; echo $((++count)); do /usr/local/dcm/gen/usg-wrap.bash; sleep 2; done
        // [3] wait for cron job
        try {
            Thread.sleep(millisecondsToSleep);
        } catch (InterruptedException ex) {
            updatedDataset = ctxt.dataCaptureModule().persistRsyncScript(dataset, "interrupted");
            throw new RuntimeException("DCM request: " + dcmRequest + " Unable to wait " + millisecondsToSleep
                    + " milliseconds: " + ex.getLocalizedMessage());
        }

        // [4] make rsync script request
        try {
            response = ctxt.dataCaptureModule().retreiveRequestedRsyncScript(dataset);
            statusCode = response.getStatus();
            logger.log(Level.INFO, "sr.py status code: " + statusCode);
            if (statusCode != 200) {
                throw new RuntimeException("DCM request: " + dcmRequest + "Status: " + statusCode + " Response:"
                        + response.getBody());
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "sr.py exeception: " + ex.getMessage());
            updatedDataset = ctxt.dataCaptureModule().persistRsyncScript(dataset, "failed");
//            if (true) {
//                throw new CommandExecutionException("millisecondsToSleep: " + millisecondsToSleep, this);
//            }
            throw new RuntimeException("DCM json request: " + dcmRequest + " Problem retrieving rsync script: "
                    + ex.getLocalizedMessage());
        }

        String script = response.getBody().getObject().getString("script");

        logger.log(Level.INFO, "sr.py: datasetId: " + datasetId);

        if (StringUtils.isEmpty(script) || StringUtils.isEmpty(userId) || StringUtils.isEmpty(datasetIdentifier)) {
            throw new RuntimeException("DCM request: " + dcmRequest
                    + " Missing required json values from the DCM script request response: "
                    + response.getBody().toString());
        }
        if (datasetId != dataset.getId()) {
            throw new RuntimeException("DCM request: " + dcmRequest
                    + " The dataset Id doesn't match: " + Long.toString(datasetId) + " - "
                    + Long.toString(dataset.getId()));
        }

        // [5] save the dataset's script in the database
        updatedDataset = ctxt.dataCaptureModule().persistRsyncScript(dataset, script);
        logger.log(Level.INFO, "script for dataset " + datasetId + ": " + script);

        // [6] return the json
        NullSafeJsonBuilder nullSafeJsonBuilder = jsonObjectBuilder()
                .add("userId", userId)
                .add("datasetId", datasetId)
                .add("datasetIdentifier", datasetIdentifier)
                .add("script", script);

        return nullSafeJsonBuilder;

//        NullSafeJsonBuilder nullSafeJsonBuilder = jsonObjectBuilder()
//                .add("userId", userId)
//                .add("datasetId", datasetId)
//                .add("datasetIdentifier", datasetIdentifier)
//                .add("message", "script requested");
//
//        return nullSafeJsonBuilder;
    }

}
