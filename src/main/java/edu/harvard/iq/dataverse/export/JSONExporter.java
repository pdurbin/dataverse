
package edu.harvard.iq.dataverse.export;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import edu.harvard.iq.dataverse.util.BundleUtil;
import edu.harvard.iq.dataverse.util.json.JsonUtil;
import edu.harvard.iq.dataverse.util.xml.XmlPrinter;
import jakarta.json.JsonArray;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Optional;

import jakarta.json.JsonObject;
import jakarta.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author skraffmi
 */
@AutoService(Exporter.class)
public class JSONExporter implements Exporter {

    @Override
    public String getFormatName() {
        return "dataverse_json";
    }

    @Override
    public String getDisplayName(Locale locale) {
        String displayName = BundleUtil.getStringFromBundle("dataset.exportBtn.itemLabel.json", locale); 
        return Optional.ofNullable(displayName).orElse("JSON");
    }

    @Override
    public void exportDataset(ExportDataProvider dataProvider, OutputStream outputStream) throws ExportException {
        System.out.println("exporting dataset in JSON");
        // mkdir docker-dev-volumes/app/data/input-for-exporters
        String outputDir = "/dv/input-for-exporters/";

        JsonArray fileDetails = dataProvider.getDatasetFileDetails();
        String fileDetailsString = JsonUtil.prettyPrint(fileDetails);
        try (PrintWriter out = new PrintWriter(outputDir + "datasetFileDetails.json")) {
            out.println(fileDetailsString);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONExporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        JsonObject datasetJson = dataProvider.getDatasetJson();
        String datasetJsonString = JsonUtil.prettyPrint(datasetJson);
        try (PrintWriter out = new PrintWriter(outputDir + "datasetJson.json")) {
            out.println(datasetJsonString);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONExporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        JsonObject datasetOre = dataProvider.getDatasetORE();
        String datasetOreString = JsonUtil.prettyPrint(datasetOre);
        try (PrintWriter out = new PrintWriter(outputDir + "datasetOre.json")) {
            out.println(datasetOreString);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONExporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        JsonObject datasetSchemaDotOrg = dataProvider.getDatasetSchemaDotOrg();
        String datasetSchemaDotOrgString = JsonUtil.prettyPrint(datasetSchemaDotOrg);
        try (PrintWriter out = new PrintWriter(outputDir + "datasetSchemaDotOrg.json")) {
            out.println(datasetSchemaDotOrgString);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONExporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        String dataCiteXml = dataProvider.getDataCiteXml();
        String dataCiteXmlPretty = XmlPrinter.prettyPrintXml(dataCiteXml);
        try (PrintWriter out = new PrintWriter(outputDir + "dataCiteXml.xml")) {
            out.println(dataCiteXmlPretty);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try{
            outputStream.write(dataProvider.getDatasetJson().toString().getBytes("UTF8"));
            outputStream.flush();
        } catch (Exception e){
            throw new ExportException("Unknown exception caught during JSON export.");
        }
    }

    @Override
    public Boolean isHarvestable() {
        return true;
    }
    
    @Override
    public Boolean isAvailableToUsers() {
        return true;
    }

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }
    
}
