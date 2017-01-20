curl -H "X-Dataverse-key: $API_TOKEN" "http://localhost:8080/api/batch/migrate/?dv=root&path=/Users/pdurbin/NetBeansProjects/dataverse/scripts/issues/907/batchImportDv&createDv=true"

http://localhost:8080/dataset.xhtml?persistentId=doi:10.7281/T1J10120

curl -X DELETE -H "X-Dataverse-key: $API_TOKEN" http://localhost:8080/api/datasets/:persistentId/destroy?persistentId=doi:10.7281/T1J10120
