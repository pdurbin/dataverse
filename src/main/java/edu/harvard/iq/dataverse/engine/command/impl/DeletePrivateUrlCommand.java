package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.users.GuestOfDataset;
import edu.harvard.iq.dataverse.engine.command.AbstractCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.exception.IllegalCommandException;
import edu.harvard.iq.dataverse.privateurl.PrivateUrl;
import java.util.List;
import java.util.logging.Logger;

@RequiredPermissions(Permission.ManageDatasetPermissions)
public class DeletePrivateUrlCommand extends AbstractCommand<Dataset> {

    private static final Logger logger = Logger.getLogger(DeletePrivateUrlCommand.class.getCanonicalName());

    final Dataset dataset;

    public DeletePrivateUrlCommand(DataverseRequest aRequest, Dataset theDataset) {
        super(aRequest, theDataset);
        dataset = theDataset;
    }

    @Override
    public Dataset execute(CommandContext ctxt) throws CommandException {
        logger.fine("Executing DeletePrivateUrlCommand....");
        if (dataset == null) {
            /**
             * @todo Internationalize this.
             */
            String message = "Can't delete Private URL. Dataset is null.";
            logger.info(message);
            throw new IllegalCommandException(message, this);
        }
        PrivateUrl doomed = ctxt.datasets().getPrivateUrl(dataset.getId());
        if (doomed == null) {
            String message = "Dataset id " + dataset.getId() + " doesn't have a Private URL to delete.";
            logger.info(message);
            throw new IllegalCommandException(message, this);
        }
        List<RoleAssignment> assignments = ctxt.roles().directRoleAssignments(dataset);
        for (RoleAssignment roleAssignment : assignments) {
            logger.info("all role assignments: " + roleAssignment);
        }
        GuestOfDataset guestOfDataset = new GuestOfDataset(dataset.getId());
        List<RoleAssignment> roleAssignments = ctxt.roles().directRoleAssignments(guestOfDataset, dataset);
        DvObject dvObject = null;
        for (RoleAssignment roleAssignment : roleAssignments) {
            logger.info("In DeletePrivateUrlCommand calling RevokeRoleCommand for role assignment: " + roleAssignment);
            dvObject = ctxt.engine().submit(new RevokeRoleCommand(roleAssignment, getRequest()));
        }
//        boolean privateUrlDeleted = ctxt.datasets().deletePrivateUrl(doomed);
//        if (!privateUrlDeleted) {
//            /**
//             * @todo Internationalize this.
//             */
//            String message = "Problem deleting Private URL.";
//            throw new CommandExecutionException(message, this);
//        }

        if (dvObject instanceof Dataset) {
            return (Dataset) dvObject;
        } else {
            return dataset;
        }
    }
}
