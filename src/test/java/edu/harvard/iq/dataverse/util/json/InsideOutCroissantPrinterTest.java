package edu.harvard.iq.dataverse.util.json;

import static edu.harvard.iq.dataverse.util.json.JsonPrinter.settingsService;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.TermsOfUseAndAccess;
import edu.harvard.iq.dataverse.dataset.DatasetTypeServiceBean;
import edu.harvard.iq.dataverse.license.License;
import edu.harvard.iq.dataverse.license.LicenseServiceBean;
import edu.harvard.iq.dataverse.mocks.MockDatasetFieldSvc;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import io.gdcc.spi.export.ExportDataProvider;
import jakarta.json.JsonObject;

public class InsideOutCroissantPrinterTest {

    private static final MockDatasetFieldSvc datasetFieldTypeSvc = new MockDatasetFieldSvc();
    private static final SettingsServiceBean settingsService = Mockito.mock(SettingsServiceBean.class);
    private static final LicenseServiceBean licenseService = Mockito.mock(LicenseServiceBean.class);
    private static final DatasetTypeServiceBean datasetTypeService = Mockito.mock(DatasetTypeServiceBean.class);

    @Test
    void testGet() throws IOException, JsonParseException, ParseException {

        ExportDataProvider exportDataProviderStub = Mockito.mock(ExportDataProvider.class);

        // File datasetVersionJson = new File("src/test/resources/json/dataset-finch2.json");
        File datasetVersionJson = new File("src/test/java/edu/harvard/iq/dataverse/util/insideOutCroissant/max/in/datasetJson.json");
        String datasetVersionAsJson = new String(Files.readAllBytes(Paths.get(datasetVersionJson.getAbsolutePath())));
        Mockito.when(exportDataProviderStub.getDatasetJson()).thenReturn(JsonUtil.getJsonObject(datasetVersionAsJson));

        File datasetOre = new File("src/test/java/edu/harvard/iq/dataverse/util/insideOutCroissant/max/in/datasetORE.json");
        String datasetOreAsJson = new String(Files.readAllBytes(Paths.get(datasetOre.getAbsolutePath())));
        // todo clean this up, make same as others
        System.out.println("STRING");
        System.out.println(JsonUtil.prettyPrint(datasetOreAsJson));
        JsonObject json = JsonUtil.getJsonObject(datasetOreAsJson);
        System.out.println("JSONOBJECT");
        System.out.println(JsonUtil.prettyPrint(json));
        Mockito.when(exportDataProviderStub.getDatasetORE()).thenReturn(json);

        File datasetSchemaDotOrg = new File("src/test/java/edu/harvard/iq/dataverse/util/insideOutCroissant/max/in/datasetSchemaDotOrg.json");
        String datasetSchemaDotOrgAsJson = new String(Files.readAllBytes(Paths.get(datasetSchemaDotOrg.getAbsolutePath())));
        Mockito.when(exportDataProviderStub.getDatasetSchemaDotOrg()).thenReturn(JsonUtil.getJsonObject(datasetSchemaDotOrgAsJson));

        File datasetFileDetails = new File("src/test/java/edu/harvard/iq/dataverse/util/insideOutCroissant/max/in/datasetFileDetails.json");
        String datasetFileDetailsAsJson = new String(Files.readAllBytes(Paths.get(datasetFileDetails.getAbsolutePath())));
        Mockito.when(exportDataProviderStub.getDatasetFileDetails()).thenReturn(JsonUtil.getJsonArray(datasetFileDetailsAsJson));




        // License license = new License("CC0 1.0",
        //         "You can copy, modify, distribute and perform the work, even for commercial purposes, all without asking permission.",
        //         URI.create("http://creativecommons.org/publicdomain/zero/1.0/"),
        //         URI.create("/resources/images/cc0.png"), true, 1l);
        // license.setDefault(true);
        // JsonParser jsonParser = new JsonParser(datasetFieldTypeSvc, null, settingsService, licenseService,
        //         datasetTypeService);
        // DatasetVersion version = jsonParser
        //         .parseDatasetVersion(exportDataProviderStub.getDatasetJson().getJsonObject("datasetVersion"));
        // version.setVersionState(DatasetVersion.VersionState.RELEASED);
        // SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMdd");
        // Date publicationDate = dateFmt.parse("19551105");
        // version.setReleaseTime(publicationDate);
        // version.setVersionNumber(1l);
        // TermsOfUseAndAccess terms = new TermsOfUseAndAccess();
        // terms.setLicense(license);
        // version.setTermsOfUseAndAccess(terms);

        // Dataset dataset = new Dataset();
        // dataset.setProtocol("doi");
        // dataset.setAuthority("10.5072/FK2");
        // dataset.setIdentifier("IMK5A4");
        // dataset.setPublicationDate(new Timestamp(publicationDate.getTime()));
        // version.setDataset(dataset);
        // Dataverse dataverse = new Dataverse();
        // dataverse.setName("LibraScholar");
        // dataset.setOwner(dataverse);

        // String foo = InsideOutCroissantPrinter.get(dataset);
        String insideOutCroissant = JsonUtil.prettyPrint(InsideOutCroissantPrinter.get(exportDataProviderStub));
        Files.write(Paths.get("/tmp/croissant.json"), insideOutCroissant.getBytes());
        System.out.println(insideOutCroissant);
    }
}
