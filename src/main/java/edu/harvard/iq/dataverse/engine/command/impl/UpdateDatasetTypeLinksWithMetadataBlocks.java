package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.MetadataBlock;
import edu.harvard.iq.dataverse.dataset.DatasetType;
import edu.harvard.iq.dataverse.engine.command.AbstractVoidCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import java.util.List;

// inspired by UpdateDataverseMetadataBlocksCommand
@RequiredPermissions({})
public class UpdateDatasetTypeLinksWithMetadataBlocks extends AbstractVoidCommand {
    // rename to "to"? UpdateDatasetTypeLinksToMetadataBlocks

    final DatasetType datasetType;
    List<MetadataBlock> metadataBlocks;

    public UpdateDatasetTypeLinksWithMetadataBlocks(DataverseRequest dataverseRequest, DatasetType datasetType, List<MetadataBlock> metadataBlocks) {
        super(dataverseRequest, (DvObject) null);
        this.datasetType = datasetType;
        this.metadataBlocks = metadataBlocks;
    }

    @Override
    protected void executeImpl(CommandContext ctxt) throws CommandException {
        datasetType.setMetadataBlocks(metadataBlocks);
        ctxt.em().merge(datasetType);
    }

}
