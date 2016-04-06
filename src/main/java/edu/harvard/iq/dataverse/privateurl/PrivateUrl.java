package edu.harvard.iq.dataverse.privateurl;

import edu.harvard.iq.dataverse.Dataset;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * @todo Should this be called PrivateUrlData instead? It might reduce confusion
 * in other parts of the code.
 */
@Entity
public class PrivateUrl implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    /**
     * @todo When a dataset is deleted, automatically delete the Private URL but
     * not vice versa.
     *
     */
//    @OneToOne(mappedBy = "privateUrl", cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    @OneToOne()
    @JoinColumn(nullable = false, unique = true)
    private Dataset dataset;

    /**
     * Don't use this constructor. It only exists to get JPA to stop complaining
     * about the following.
     *
     * Exception Description: The instance creation method
     * [edu.harvard.iq.dataverse.privateurl.PrivateUrl.<Default Constructor>],
     * with no parameters, does not exist, or is not accessible.
     */
    @Deprecated
    public PrivateUrl() {
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

    public PrivateUrl(Dataset dataset, String token) {
        this.token = token;
        this.dataset = dataset;
    }

}
