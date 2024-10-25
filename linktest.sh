#!/bin/bash -x
curl -s http://localhost:8080/api/datasets/datasetTypes/dataset -X PUT -d '["geospatial","astrophysics"]' | jq .
curl -s http://localhost:8080/api/datasets/datasetTypes/dataset | jq .
curl -s "http://localhost:8080/api/dataverses/root/metadatablocks?onlyDisplayedOnCreate=true&datasetType=dataset" | jq .
