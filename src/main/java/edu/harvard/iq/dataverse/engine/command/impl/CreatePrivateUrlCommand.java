package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.authorization.DataverseRole;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.users.GuestOfDataset;
import edu.harvard.iq.dataverse.engine.command.AbstractCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.privateurl.PrivateUrl;
import java.util.logging.Logger;

@RequiredPermissions(Permission.ManageDatasetPermissions)
public class CreatePrivateUrlCommand extends AbstractCommand<PrivateUrl> {

    private static final Logger logger = Logger.getLogger(CreatePrivateUrlCommand.class.getCanonicalName());

    final Dataset dataset;

    public CreatePrivateUrlCommand(DataverseRequest dataverseRequest, Dataset theDataset) {
        super(dataverseRequest, theDataset);
        dataset = theDataset;
    }

    @Override
    public PrivateUrl execute(CommandContext ctxt) throws CommandException {
        logger.fine("Executing CreatePrivateUrlCommand...");
        PrivateUrl privateUrl = ctxt.datasets().createPrivateUrl(dataset.getId());
        DataverseRole memberRole = ctxt.roles().findBuiltinRoleByAlias(DataverseRole.MEMBER);
        GuestOfDataset guestOfDataset = new GuestOfDataset(dataset.getId());
        RoleAssignment roleAssignment = ctxt.engine().submit(new AssignRoleCommand(guestOfDataset, memberRole, dataset, getRequest()));
        privateUrl.setRoleAssignment(roleAssignment);
        return privateUrl;
    }

}
