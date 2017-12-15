package edu.harvard.iq.dataverse.api;

import edu.harvard.iq.dataverse.DataFile;
import edu.harvard.iq.dataverse.DataFileServiceBean;
import edu.harvard.iq.dataverse.authorization.users.User;
import edu.harvard.iq.dataverse.download.CanDownloadFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("admin/test/ipgroups")
public class IpGroups extends AbstractApiBean {

    @EJB
    CanDownloadFile canDownloadFile;
    @EJB
    DataFileServiceBean dataFileSvc;

    @GET
    @Path("{fileId}")
    public Response getDatasetPublishPopupCustomText(@PathParam("fileId") long fileId) {
        try {
            User user = findUserOrDie();
            DataFile dataFile = dataFileSvc.find(fileId);
            JsonObjectBuilder result = Json.createObjectBuilder();
            boolean canDownloadFileResult = this.canDownloadFile.canDownloadFile(dataFile.getFileMetadata());
            result.add("canDownloadFile", canDownloadFileResult);
            result.add("user", user.toString());
            return ok(result);
        } catch (WrappedResponse ex) {
            return ex.getResponse();
        }
    }

}
