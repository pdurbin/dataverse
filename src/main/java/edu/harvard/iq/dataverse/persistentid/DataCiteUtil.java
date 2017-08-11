package edu.harvard.iq.dataverse.persistentid;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetAuthor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class DataCiteUtil {

    private static final Logger logger = Logger.getLogger(DataCiteUtil.class.getCanonicalName());

    private static String template;

    static {
        try (InputStream in = DataCiteUtil.class.getResourceAsStream("/tmp/datacite_metadata_template.xml")) {
            template = Util.readAndClose(in, "utf-8");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "datacite metadata template load error");
            logger.log(Level.SEVERE, "String " + e.toString());
            logger.log(Level.SEVERE, "localized message " + e.getLocalizedMessage());
            logger.log(Level.SEVERE, "cause " + e.getCause());
            logger.log(Level.SEVERE, "message " + e.getMessage());
        }
    }

    private String xmlMetadata;
    private String identifier;
    private List<String> creators;
    private String title;
    private String publisher;
    private String publisherYear;
    private List<DatasetAuthor> authors;
    private String description;
    private List<String[]> contacts;
    private List<String[]> producers;

    public List<String[]> getProducers() {
        return producers;
    }

    public void setProducers(List<String[]> producers) {
        this.producers = producers;
    }

    public List<String[]> getContacts() {
        return contacts;
    }

    public void setContacts(List<String[]> contacts) {
        this.contacts = contacts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DatasetAuthor> getAuthors() {
        return authors;
    }

    public void setAuthors(List<DatasetAuthor> authors) {
        this.authors = authors;
    }

    public DataCiteUtil(String xmlMetaData) {
        this.xmlMetadata = xmlMetaData;
        Document doc = Jsoup.parseBodyFragment(xmlMetaData);
        Elements identifierElements = doc.select("identifier");
        if (identifierElements.size() > 0) {
            identifier = identifierElements.get(0).html();
        }
        Elements creatorElements = doc.select("creatorName");
        creators = new ArrayList<>();
        for (Element creatorElement : creatorElements) {
            creators.add(creatorElement.html());
        }
        Elements titleElements = doc.select("title");
        if (titleElements.size() > 0) {
            title = titleElements.get(0).html();
        }
        Elements publisherElements = doc.select("publisher");
        if (publisherElements.size() > 0) {
            publisher = publisherElements.get(0).html();
        }
        Elements publisherYearElements = doc.select("publicationYear");
        if (publisherYearElements.size() > 0) {
            publisherYear = publisherYearElements.get(0).html();
        }
    }

    public String generateXML() {
        try {
            template = new String(Files.readAllBytes(Paths.get("/tmp/datacite_metadata_template.xml")));
        } catch (IOException ex) {
            Logger.getLogger(DataCiteUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (template == null) {
            logger.info("template was null!");
            return null;
        }
        logger.info("template: " + template);

        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("datacite.creator", "datacite.creator");
        Dataset dataset = new Dataset();
        setIdentifier("myIdentifier");
        setCreators(Util.getListFromStr(metadata.get("datacite.creator")));
        setAuthors(dataset.getLatestVersion().getDatasetAuthors());
//        setDescription(dataset.getLatestVersion().getDescription());
        setDescription("myDescription <br></br>");
        setContacts(dataset.getLatestVersion().getDatasetContacts());
        setProducers(dataset.getLatestVersion().getDatasetProducers());
//        setTitle(dataset.getLatestVersion().getTitle());
        setTitle("myTitle");
//        setPublisher(metadata.get("datacite.publisher"));
        setPublisher("myPublisher");
//        setPublisherYear(metadata.get("datacite.publicationyear"));
        setPublisherYear("2017");

        xmlMetadata = template.replace("${identifier}", this.identifier.trim())
                .replace("${title}", this.title)
                .replace("${publisher}", this.publisher)
                .replace("${publisherYear}", this.publisherYear)
                .replace("${description}", this.description);
        StringBuilder creatorsElement = new StringBuilder();
        for (DatasetAuthor author : authors) {
            creatorsElement.append("<creator><creatorName>");
            creatorsElement.append(author.getName().getDisplayValue());
            creatorsElement.append("</creatorName>");

            if (author.getIdType() != null && author.getIdValue() != null && !author.getIdType().isEmpty() && !author.getIdValue().isEmpty() && author.getAffiliation() != null && !author.getAffiliation().getDisplayValue().isEmpty()) {

                if (author.getIdType().equals("ORCID")) {
                    creatorsElement.append("<nameIdentifier schemeURI=\"http://orcid.org/\" nameIdentifierScheme=\"ORCID\">" + author.getIdValue() + "</nameIdentifier>");
                }
                if (author.getIdType().equals("ISNI")) {
                    creatorsElement.append("<nameIdentifier schemeURI=\"http://isni.org/isni/\" nameIdentifierScheme=\"ISNI\">" + author.getIdValue() + "</nameIdentifier>");
                }
                if (author.getIdType().equals("LCNA")) {
                    creatorsElement.append("<nameIdentifier schemeURI=\"http://id.loc.gov/authorities/names/\" nameIdentifierScheme=\"LCNA\">" + author.getIdValue() + "</nameIdentifier>");
                }
            }
            if (author.getAffiliation() != null && !author.getAffiliation().getDisplayValue().isEmpty()) {
                creatorsElement.append("<affiliation>" + author.getAffiliation().getDisplayValue() + "</affiliation>");
            }
            creatorsElement.append("</creator>");
        }
        xmlMetadata = xmlMetadata.replace("${creators}", creatorsElement.toString());

        StringBuilder contributorsElement = new StringBuilder();
        for (String[] contact : this.getContacts()) {
            contributorsElement.append("<contributor contributorType=\"ContactPerson\"><contributorName>" + contact[0] + "</contributorName>");
            if (!contact[1].isEmpty()) {
                contributorsElement.append("<affiliation>" + contact[1] + "</affiliation>");
            }
            contributorsElement.append("</contributor>");
        }
        for (String[] producer : this.getProducers()) {
            contributorsElement.append("<contributor contributorType=\"Producer\"><contributorName>" + producer[0] + "</contributorName>");
            if (!producer[1].isEmpty()) {
                contributorsElement.append("<affiliation>" + producer[1] + "</affiliation>");
            }
            contributorsElement.append("</contributor>");
        }
        xmlMetadata = xmlMetadata.replace("{$contributors}", contributorsElement.toString());
        return xmlMetadata;
    }

    public static String getTemplate() {
        return template;
    }

    public static void setTemplate(String template) {
        DataCiteUtil.template = template;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getCreators() {
        return creators;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisherYear() {
        return publisherYear;
    }

    public void setPublisherYear(String publisherYear) {
        this.publisherYear = publisherYear;
    }

}

class Util {

    public static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException("Fail to close InputStream");
            }
        }
    }

    public static String readAndClose(InputStream inStream, String encoding) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buf = new byte[128];
        String data;
        try {
            int cnt;
            while ((cnt = inStream.read(buf)) >= 0) {
                outStream.write(buf, 0, cnt);
            }
            data = outStream.toString(encoding);
        } catch (IOException ioe) {
            throw new RuntimeException("IOException");
        } finally {
            close(inStream);
        }
        return data;
    }

    public static List<String> getListFromStr(String str) {
        return Arrays.asList(str.split("; "));
//        List<String> authors = new ArrayList();
//        int preIdx = 0;
//        for(int i=0;i<str.length();i++){
//            if(str.charAt(i)==';'){
//                authors.add(str.substring(preIdx,i).trim());
//                preIdx = i+1;
//            }
//        }
//        return authors;
    }

    public static String getStrFromList(List<String> authors) {
        StringBuilder str = new StringBuilder();
        for (String author : authors) {
            if (str.length() > 0) {
                str.append("; ");
            }
            str.append(author);
        }
        return str.toString();
    }
}
