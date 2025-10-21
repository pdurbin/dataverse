package edu.harvard.iq.dataverse.dataset;

import edu.harvard.iq.dataverse.MetadataBlock;
import edu.harvard.iq.dataverse.license.License;
import edu.harvard.iq.dataverse.util.BundleUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NamedQueries({
    @NamedQuery(name = "DatasetType.findAll",
            query = "SELECT d FROM DatasetType d"),
    @NamedQuery(name = "DatasetType.findById",
            query = "SELECT d FROM DatasetType d WHERE d.id=:id"),
    @NamedQuery(name = "DatasetType.findByName",
            query = "SELECT d FROM DatasetType d WHERE d.name=:name"),
    @NamedQuery(name = "DatasetType.deleteById",
            query = "DELETE FROM DatasetType d WHERE d.id=:id"),})
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = "name"),}
)

public class DatasetType implements Serializable {

    public static final String DATASET_TYPE_DATASET = "dataset";
    public static final String DATASET_TYPE_SOFTWARE = "software";
    public static final String DATASET_TYPE_WORKFLOW = "workflow";
    public static final String DATASET_TYPE_REVIEW = "review";
    public static final String DEFAULT_DATASET_TYPE = DATASET_TYPE_DATASET;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Machine readable name to use via API.
     */
    // Any constraints? @Pattern regexp?
    @Column(nullable = false)
    private String name;

    /**
     * Human readable name to show in the UI.
     */
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String displayName;


    /**
      * Human readable names to show in the UI, with translations.
     */
    // @OneToMany(cascade = CascadeType.ALL, mappedBy = "datasetType", orphanRemoval = true)
    // private List<DatasetTypeTranslation> displayNames = new ArrayList<>();

    /**
      * Convenience method to get display name in default locale.
     */
    // @Transient
    // public String getDisplayName() {
    //     return displayNames.stream()
    //             .filter(d -> d.getLocale().equals("en"))
    //             .findFirst()
    //             .map(DatasetTypeTranslation::getDisplayName)
    //             .orElse(name);
    // }

    /**
      * Convenience method to set display name in default locale.
     */
    // public void setDisplayName(String displayName) {
    //     DatasetTypeTranslation defaultName = displayNames.stream()
    //             .filter(d -> d.getLocale().equals("en"))
    //             .findFirst()
    //             .orElseGet(() -> {
    //                 DatasetTypeTranslation d = new DatasetTypeTranslation();
    //                 d.setDatasetType(this);
    //                 d.setLocale("en");
    //                 displayNames.add(d);
    //                 return d;
    //             });
    //     defaultName.setDisplayName(displayName);
    // }    


    /**
     * The metadata blocks this dataset type is linked to.
     */
    @ManyToMany(cascade = {CascadeType.MERGE})
    private List<MetadataBlock> metadataBlocks = new ArrayList<>();
    
    /**
     * The Licenses this dataset type is linked to.
     */
    @ManyToMany(cascade = {CascadeType.MERGE})
    private List<License> licenses = new ArrayList<>();

    public DatasetType() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(Locale locale) {
        return BundleUtil.getStringFromPropertyFile(name + ".displayName", "datasetTypes");
    }

    public List<MetadataBlock> getMetadataBlocks() {
        return metadataBlocks;
    }

    public void setMetadataBlocks(List<MetadataBlock> metadataBlocks) {
        this.metadataBlocks = metadataBlocks;
    }
    
    public List<License> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<License> licenses) {
        this.licenses = licenses;
    }

    public JsonObjectBuilder toJson() {
        return toJson(null);
    }

    public JsonObjectBuilder toJson(Locale locale) {
        JsonArrayBuilder linkedMetadataBlocks = Json.createArrayBuilder();
        for (MetadataBlock metadataBlock : this.getMetadataBlocks()) {
            linkedMetadataBlocks.add(metadataBlock.getName());
        }
        JsonArrayBuilder availableLicenses = Json.createArrayBuilder();
        for (License license : this.getLicenses()) {
            availableLicenses.add(license.getName());
        }
        return Json.createObjectBuilder()
                .add("id", getId())
                .add("name", getName())
                .add("displayName", locale == null ? getDisplayName() : getDisplayName(locale))
                .add("linkedMetadataBlocks", linkedMetadataBlocks)
                .add("availableLicenses", availableLicenses);
    }

}
