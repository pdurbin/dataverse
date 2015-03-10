package edu.harvard.iq.dataverse.search;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * This table introduces denormalization in how permissions releated to search
 * and indexing are stored to make indexing of permissions into Solr more
 * performant by allowing us to simply copy each row into a "permissions" Solr
 * document. Permissions recorded here are always explicit, rather than the mix
 * of explicit and implicit permissions (i.e. due to inheritance rules) stored
 * in the permissions system. See also
 * https://github.com/IQSS/dataverse/issues/50 )
 */
@Entity
public class IndexPermissionsCache implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique Solr ID of the Solr "permission" document itself.
     */
    @Column(nullable = false, unique = true)
    private String solrId;

    /**
     * The corresponding primary/content Solr document to which the "permission"
     * document applies. Every Solr "permission" document applies to one and
     * only one Solr "content" document.
     */
    @Column(nullable = false, unique = true)
    private String definitionPoint;

    /**
     * A comma-separated list of users and groups who should be able to
     * "discover" (search and browse to) the definitionPoint in question. This
     * field should be updated any time a permission change affects whether or
     * not a Solr document should be seen or not seen by a user or group.
     *
     * Scenarios in which it is expected that this value will be updated
     * include:
     *
     * - a role being assigned or revoked
     *
     * - a user or group being added or removed from a role
     *
     * - a new DvObject is added to the system
     *
     * - etc.
     *
     * When this value is updated, a timestamp should be recorded at
     * permissionChangeTime.
     */
    @Column(columnDefinition = "TEXT NOT NULL")
    private String discoverableByList;

    /**
     * The time at which discoverableByList was last updated. This value is
     * compared against indexTime to know if the Solr "permission" document was
     * successfully updated in Solr.
     */
    @Column(nullable = false)
    private Timestamp permissionChangeTime;

    /**
     * The time at which the Solr "permission" document was last updated.
     * Assuming Solr working properly, this timestamp should always be later in
     * time that permissionChangeTime because it takes a little time to index
     * documents into Solr. We'll know a Solr "permission" document stale and
     * should be re-indexed indexTime is not at least a few millisecond after
     * permissionChangeTime.
     */
    @Column(nullable = true)
    private Timestamp indexTime;

    /**
     * The type of the corresponding primary/content document, if it's
     * published, draft, or deaccessioned. Each Solr id must be unique so we
     * index different primary/content documents for the same DvObject under
     * different IDs based on the type. In the UI, users will see two different
     * "cards" if a dataset has a published version and a later draft version.
     *
     * This field is mostly informational. You can also figure out the type from
     * the name used for definitionPoint.
     *
     * @todo JPA created a charvar rather than an enum so PostgreSQL allows any
     * string (e.g. "foo) to be stored, which is not what we want.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentDocType contentDocType;

    /**
     * Mostly informational, this field is for storing the DvObject id in
     * question so you don't have to parse it out of the solrId or
     * definitionPoint.
     */
    @Column(nullable = false)
    private long defPointDvObjectId;

    /**
     * @deprecated Do not use. Required by JPA.
     */
    @Deprecated
    public IndexPermissionsCache() {
    }

    public IndexPermissionsCache(String solrId, String definitionPoint, String discoverableByList, Timestamp permissionChangeTime, ContentDocType contentDocType, long defPointDvObjectId) {
        this.solrId = solrId;
        this.definitionPoint = definitionPoint;
        this.discoverableByList = discoverableByList;
        this.permissionChangeTime = permissionChangeTime;
        this.contentDocType = contentDocType;
        this.defPointDvObjectId = defPointDvObjectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSolrId() {
        return solrId;
    }

    public String getDefinitionPoint() {
        return definitionPoint;
    }

    public String getDiscoverableByList() {
        return discoverableByList;
    }

    public Timestamp getPermissionChangeTime() {
        return permissionChangeTime;
    }

    public Timestamp getIndexTime() {
        return indexTime;
    }

    public ContentDocType getContentDocType() {
        return contentDocType;
    }

    public long getDefPointDvObjectId() {
        return defPointDvObjectId;
    }

    public enum ContentDocType {

        PUBLISHED, DRAFT, DEACCESSIONED
    }

}
