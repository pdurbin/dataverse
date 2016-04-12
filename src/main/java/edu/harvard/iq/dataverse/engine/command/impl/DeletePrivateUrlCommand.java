package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.users.GuestOfDataset;
import edu.harvard.iq.dataverse.engine.command.AbstractCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.exception.CommandExecutionException;
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
        boolean privateUrlDeleted = ctxt.datasets().deletePrivateUrl(dataset.getId());
        if (!privateUrlDeleted) {
            String message = "Problem deleting Private URL.";
            throw new CommandExecutionException(message, this);
        }
        GuestOfDataset guestOfDataset = new GuestOfDataset(dataset.getId());
        List<RoleAssignment> roleAssignments = ctxt.roles().directRoleAssignments(guestOfDataset, dataset);
        for (RoleAssignment roleAssignment : roleAssignments) {
            ctxt.engine().submit(new RevokeRoleCommand(roleAssignment, getRequest()));
        }
        return dataset;

    }

}
