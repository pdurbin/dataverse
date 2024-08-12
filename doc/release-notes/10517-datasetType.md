### Initial Support for Dataset Types

Out of the box, all datasets have the type "dataset" but superusers can add additional types such as "software" or "workflow" to have those types sent to DataCite when the dataset is published.

For details see <https://dataverse-guide--10694.org.readthedocs.build/en/10694/user/dataset-management.html#dataset-types> and #10517. Please note that this feature is highly experimental and is expected to evolve.

Upgrade instructions
--------------------

Add the following line to your Solr schema.xml file and do a full reindex:

```
<field name="datasetType" type="string" stored="true" indexed="true" multiValued="false"/>
```
