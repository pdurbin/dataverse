package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.MetadataBlock;
import edu.harvard.iq.dataverse.dataset.DatasetType;
import edu.harvard.iq.dataverse.engine.command.AbstractCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.exception.PermissionException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// inspired by UpdateDataverseMetadataBlocksCommand and DeactivateUserCommand
@RequiredPermissions({})
public class UpdateMetadataBlockDatasetTypeAssociations extends AbstractCommand<MetadataBlock> {

    private static final Logger logger = Logger.getLogger(UpdateMetadataBlockDatasetTypeAssociations.class.getCanonicalName());

    private DataverseRequest dataverseRequest;
    private MetadataBlock metadataBlock;
    private List<DatasetType> datasetTypes;

    public UpdateMetadataBlockDatasetTypeAssociations(DataverseRequest dataverseRequest, MetadataBlock metadataBlock, List<DatasetType> datasetTypes) {
        super(dataverseRequest, (DvObject) null);
        this.dataverseRequest = dataverseRequest;
        this.metadataBlock = metadataBlock;
        this.datasetTypes = datasetTypes;
    }

    @Override
    public MetadataBlock execute(CommandContext ctxt) throws CommandException {
        if (true) {
            logger.info("exiting early");
//            metadataBlock.setDatasetTypes(datasetTypes);
            MetadataBlock savedMetadataBlock = ctxt.em().merge(metadataBlock);
            return savedMetadataBlock;
        }
        if (!getUser().isSuperuser()) {
            throw new PermissionException("Command can only be called by superusers.", this, null, null);
        }
//        logger.info("before changing anything, block " + metadataBlock.getName() + " has these associations: "
//                + metadataBlock.getDatasetTypes().stream()
//                        .map(DatasetType::getName)
//                        .collect(Collectors.joining(", ")));
//        logger.info("about to set these types: " + datasetTypes.stream()
//                .map(DatasetType::getName)
//                .collect(Collectors.joining(", ")));
//        metadataBlock.setDatasetTypes(datasetTypes);
//        MetadataBlock savedMetadataBlock = ctxt.em().merge(metadataBlock);
        if (datasetTypes.isEmpty()) {
            // clear out all dataset types from metadata block
            logger.info("dataset types is empty! clearing out");
//            for (DatasetType datasetType : datasetTypes) {
//                List<MetadataBlock> existing = datasetType.getMetadataBlocks();
//                List<MetadataBlock> minusOne = existing;
//                for (MetadataBlock mdb : existing) {
//                    if (mdb.equals(metadataBlock)) {
//                        minusOne.remove(mdb);
//                        datasetType.setMetadataBlocks(minusOne);
//                        if (true) {
//                            // just a test
//                            logger.info("just a test... sets to empty list");
//                            datasetType.setMetadataBlocks(new ArrayList<>());
//                            ctxt.em().merge(datasetType);
//                        }
//                    }
//                }
//            }
//            MetadataBlock savedMetadataBlock2 = ctxt.em().merge(savedMetadataBlock);
            logger.info("returning from is empty");
            return null;
//            return savedMetadataBlock2;
        } else {
            // set incoming dataset types for this metadatablock
            logger.info("datasetTypes was not empty");
            return null;

//            MetadataBlock savedMetadataBlock = ctxt.em().merge(metadataBlock);
//        ctxt.em().flush();
//            for (DatasetType datasetType : savedMetadataBlock.getDatasetTypes()) {
//                System.out.println("type: " + datasetType.getName());
//                List<MetadataBlock> blocks = datasetType.getMetadataBlocks();
//                blocks.add(metadataBlock);
//                // We filter the list through a set, so that all blocks are distinct.
//                datasetType.setMetadataBlocks(new LinkedList<>(new HashSet<>(blocks)));
//                ctxt.em().merge(datasetType);
//            }
//            // TODO save the block one more time?
////            return savedMetadataBlock;
//            MetadataBlock savedMetadataBlock2 = ctxt.em().merge(savedMetadataBlock);
//            logger.info("returning from not empty");
//            return savedMetadataBlock2;
        }
//        MetadataBlock savedMetadataBlock = ctxt.em().merge(metadataBlock);
////        ctxt.em().flush();
//        for (DatasetType datasetType : savedMetadataBlock.getDatasetTypes()) {
//            System.out.println("type: " + datasetType.getName());
//            List<MetadataBlock> blocks = datasetType.getMetadataBlocks();
//            blocks.add(metadataBlock);
//            // We filter the list through a set, so that all blocks are distinct.
//            datasetType.setMetadataBlocks(new LinkedList<>(new HashSet<>(blocks)));
//            ctxt.em().merge(datasetType);
//        }
//        // TODO save the block one more time?
//        return savedMetadataBlock;
    }

}
