package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.users.PrivateUrlUser;
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
        PrivateUrl doomed = ctxt.privateUrl().getPrivateUrl(dataset.getId());
        if (doomed == null) {
            String message = "Dataset id " + dataset.getId() + " doesn't have a Private URL to delete.";
            logger.info(message);
            throw new IllegalCommandException(message, this);
        }
        PrivateUrlUser privateUrlUser = new PrivateUrlUser(dataset.getId());
        List<RoleAssignment> roleAssignments = ctxt.roles().directRoleAssignments(privateUrlUser, dataset);
        for (RoleAssignment roleAssignment : roleAssignments) {
            ctxt.engine().submit(new RevokeRoleCommand(roleAssignment, getRequest()));
        }
        return dataset;

    }

}
