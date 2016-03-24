package edu.harvard.iq.dataverse.anonlink;

import edu.harvard.iq.dataverse.Dataset;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * This is the entity class for the "Anonymous Link To Unpublished Dataset"
 * feature.
 */
@Entity
public class AnonLink implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//      @Transient
    @Column(nullable = false)
    private String token;

    /**
     * @todo When a dataset is deleted, automatically delete the anon link but
     * not vice versa.
     *
     * @todo Make is so there can only ever be one link per dataset. Multiple
     * tokens are not allowed.
     */
//    @OneToOne(cascade = {CascadeType.REMOVE})
//    @OneToOne(mappedBy = "anonLink", cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    @OneToOne()
    @JoinColumn(nullable = false, unique=true)
    private Dataset dataset;

    /**
     * Don't use this constructor. It only exists to get JPA to stop complaining
     * about the following.
     *
     * Exception Description: The instance creation method
     * [edu.harvard.iq.dataverse.anonlink.AnonLink.<Default Constructor>], with
     * no parameters, does not exist, or is not accessible.
     */
    @Deprecated
    public AnonLink() {
    }

    public AnonLink(Dataset dataset) {
        this.dataset = dataset;
        this.token = UUID.randomUUID().toString();
    }

    public AnonLink(Dataset dataset, String token) {
        this.dataset = dataset;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Dataset getDataset() {
        return dataset;
    }

}
