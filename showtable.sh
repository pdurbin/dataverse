#!/bin/sh
DOCKER_CLI_HINTS=false docker exec -it postgres-1 bash -c "psql -h localhost -U dataverse dataverse -c 'select * from datasettype_metadatablock'"
