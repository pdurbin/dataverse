package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.MetadataBlock;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.dataset.DatasetType;
import edu.harvard.iq.dataverse.engine.command.AbstractCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Lists the metadata blocks of a {@link Dataverse}.
 *
 * @author michael
 */
// no annotations here, since permissions are dynamically decided
public class ListMetadataBlocksCommand extends AbstractCommand<List<MetadataBlock>> {

    private final Dataverse dataverse;
    private final boolean onlyDisplayedOnCreate;
    private final DatasetType datasetType;

    public ListMetadataBlocksCommand(DataverseRequest request, Dataverse dataverse, boolean onlyDisplayedOnCreate, DatasetType datasetType) {
        super(request, dataverse);
        this.dataverse = dataverse;
        this.onlyDisplayedOnCreate = onlyDisplayedOnCreate;
        this.datasetType = datasetType;
    }

    @Override
    public List<MetadataBlock> execute(CommandContext ctxt) throws CommandException {
        if (onlyDisplayedOnCreate) {
            return listMetadataBlocksDisplayedOnCreate(ctxt, dataverse);
        }
        return dataverse.getMetadataBlocks();
    }

    private List<MetadataBlock> listMetadataBlocksDisplayedOnCreate(CommandContext ctxt, Dataverse dataverse) {
        if (dataverse.isMetadataBlockRoot() || dataverse.getOwner() == null) {
            return ctxt.metadataBlocks().listMetadataBlocksDisplayedOnCreate(dataverse);
        }
//        return listMetadataBlocksDisplayedOnCreate(ctxt, dataverse.getOwner());
        List<MetadataBlock> metadataBlocks = listMetadataBlocksDisplayedOnCreate(ctxt, dataverse.getOwner());
        if (datasetType == null) {
            System.out.println("no dataset type, returning normal list");
            return metadataBlocks;
        } else {
            // Add the metadata blocks based on the dataset type
            System.out.println("yes dataset type, returning extra");
            List<MetadataBlock> extra = datasetType.getMetadataBlocks();
            System.out.println("size of extra: " + extra.size());
            for (MetadataBlock metadataBlock : extra) {
                System.out.println("name: " + metadataBlock.getDisplayName());
            }
            return Stream.concat(metadataBlocks.stream(), extra.stream()).toList();
        }
        
    }

    @Override
    public Map<String, Set<Permission>> getRequiredPermissions() {
        return Collections.singletonMap("",
                dataverse.isReleased() ? Collections.<Permission>emptySet()
                        : Collections.singleton(Permission.ViewUnpublishedDataverse));
    }
}
