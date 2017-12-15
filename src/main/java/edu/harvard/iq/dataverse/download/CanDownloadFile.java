package edu.harvard.iq.dataverse.download;

import edu.harvard.iq.dataverse.DataverseRequestServiceBean;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.FileMetadata;
import edu.harvard.iq.dataverse.PermissionServiceBean;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.users.GuestUser;
import edu.harvard.iq.dataverse.authorization.users.User;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

@Stateless
@Named
public class CanDownloadFile implements Serializable {

    @Inject
    DataverseSession session;

    @EJB
    PermissionServiceBean permissionService;

    @Inject
    DataverseRequestServiceBean dvRequestService;

    private final Map<Long, Boolean> fileDownloadPermissionMap = new HashMap<>(); // { FileMetadata.id : Boolean } 

    /**
     * WARNING: Before calling this, make sure the user has download permission
     * for the file!! (See DatasetPage.canDownloadFile())
     *
     * Should there be a Explore WorldMap Button for this file? See table in:
     * https://github.com/IQSS/dataverse/issues/1618
     *
     * (1) Does the file have MapLayerMetadata? (2) Are the proper settings in
     * place
     *
     * @param fm fileMetadata
     * @return boolean
     */
    public boolean canDownloadFile(FileMetadata fileMetadata) {
        if (fileMetadata == null) {
            System.out.println("return 1");
            return false;
        }

        if ((fileMetadata.getId() == null) || (fileMetadata.getDataFile().getId() == null)) {
            System.out.println("return 2");
            return false;
        }

        // --------------------------------------------------------------------        
        // Grab the fileMetadata.id and restriction flag                
        // --------------------------------------------------------------------
        Long fid = fileMetadata.getId();
        //logger.info("calling candownloadfile on filemetadata "+fid);
        boolean isRestrictedFile = fileMetadata.isRestricted();

        // --------------------------------------------------------------------
        // Has this file been checked? Look at the DatasetPage hash
        // --------------------------------------------------------------------
        // Disabling this so we check each time.
        if (false && this.fileDownloadPermissionMap.containsKey(fid)) {
            // Yes, return previous answer
            //logger.info("using cached result for candownloadfile on filemetadata "+fid);
            System.out.println("return 3");
            return this.fileDownloadPermissionMap.get(fid);
        }
        //----------------------------------------------------------------------
        //(0) Before we do any testing - if version is deaccessioned and user
        // does not have edit dataset permission then may download
        //----------------------------------------------------------------------

        if (fileMetadata.getDatasetVersion().isDeaccessioned()) {
            if (this.doesSessionUserHavePermission(Permission.EditDataset, fileMetadata)) {
                // Yes, save answer and return true
                this.fileDownloadPermissionMap.put(fid, true);
                System.out.println("return 4");
                return true;
            } else {
                this.fileDownloadPermissionMap.put(fid, false);
                System.out.println("return 5");
                return false;
            }
        }

        // --------------------------------------------------------------------
        // (1) Is the file Unrestricted ?        
        // --------------------------------------------------------------------
        if (!isRestrictedFile) {
            // Yes, save answer and return true
            this.fileDownloadPermissionMap.put(fid, true);
            System.out.println("return 6");
            return true;
        }

        // --------------------------------------------------------------------
        // Conditions (2) through (4) are for Restricted files
        // --------------------------------------------------------------------
        // --------------------------------------------------------------------
        // (2) A Guest user can only download a restricted file through  membership of an IP Group.
        // --------------------------------------------------------------------
        if (session == null) {
            System.out.println("session is null!!!");
        }
        User user = session.getUser();
        System.out.println("User: " + user);
        DataverseRequest dataverseRequest = dvRequestService.getDataverseRequest();
        System.out.println("IP Address: " + dataverseRequest.getSourceAddress());
        if (session.getUser() instanceof GuestUser) {
            boolean guestHasAccessDueToIpGroup = permissionService.requestOn(dvRequestService.getDataverseRequest(), fileMetadata.getDataFile()).has(Permission.DownloadFile);
            if (guestHasAccessDueToIpGroup) {
                this.fileDownloadPermissionMap.put(fid, true);
                System.out.println("return 7");
                return true;
            } else {
                this.fileDownloadPermissionMap.put(fid, false);
                // FIXME: Is the user always Guest?!?
                System.out.println("return 8");
                return false;
            }
        }

        // --------------------------------------------------------------------
        // (3) Does the User have DownloadFile Permission at the **Dataset** level 
        // --------------------------------------------------------------------
        if (this.doesSessionUserHavePermission(Permission.DownloadFile, fileMetadata)) {
            // Yes, save answer and return true
            this.fileDownloadPermissionMap.put(fid, true);
            System.out.println("return 9");
            return true;
        }

        // --------------------------------------------------------------------
        // (4) Does the user has DownloadFile permission on the DataFile            
        // --------------------------------------------------------------------
        /*
        if (this.permissionService.on(fileMetadata.getDataFile()).has(Permission.DownloadFile)){
            this.fileDownloadPermissionMap.put(fid, true);
            return true;
        }
         */
        // --------------------------------------------------------------------
        // (6) No download....
        // --------------------------------------------------------------------
        this.fileDownloadPermissionMap.put(fid, false);

        System.out.println("return 10");
        return false;
    }

    public boolean doesSessionUserHavePermission(Permission permissionToCheck, FileMetadata fileMetadata) {
        if (permissionToCheck == null) {
            return false;
        }

        DvObject objectToCheck = null;

        if (permissionToCheck.equals(Permission.EditDataset)) {
            objectToCheck = fileMetadata.getDatasetVersion().getDataset();
        } else if (permissionToCheck.equals(Permission.DownloadFile)) {
            objectToCheck = fileMetadata.getDataFile();
        }

        if (objectToCheck == null) {
            return false;
        }

        boolean hasPermission = this.permissionService.userOn(this.session.getUser(), objectToCheck).has(permissionToCheck);

        // return true/false
        return hasPermission;
    }

}
