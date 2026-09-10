package com.tenderpilot.bidworkspace.domain;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_documents",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bid_document_org_tender_sha",
                columnNames = {
                        "organization_id",
                        "tender_id",
                        "sha256"
                }
        )
)
public class BidDocumentEntity {

    @Id
    private UUID id;


    @Column(
            name = "organization_id",
            nullable = false
    )
    private UUID organizationId;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "tender_id",
            nullable = false
    )
    private TenderEntity tender;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 50
    )
    private BidDocumentCategory category;


    @Column(
            name = "original_filename",
            nullable = false,
            length = 500
    )
    private String originalFilename;


    @Column(
            name = "stored_filename",
            nullable = false,
            length = 300
    )
    private String storedFilename;


    @Column(
            name = "content_type",
            length = 200
    )
    private String contentType;


    @Column(
            name = "size_bytes",
            nullable = false
    )
    private long sizeBytes;


    @Column(
            name = "sha256",
            nullable = false,
            length = 64
    )
    private String sha256;


    @Column(
            name = "storage_key",
            nullable = false,
            unique = true,
            length = 1000
    )
    private String storageKey;


    @Column(
            name = "note",
            length = 2000
    )
    private String note;


    @Column(
            name = "uploaded_by",
            nullable = false,
            length = 320
    )
    private String uploadedBy;


    @Column(
            name = "uploaded_at",
            nullable = false
    )
    private Instant uploadedAt;


    protected BidDocumentEntity() {
    }


    private BidDocumentEntity(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentCategory category,
            String originalFilename,
            String storedFilename,
            String contentType,
            long sizeBytes,
            String sha256,
            String storageKey,
            String note,
            String uploadedBy
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.category =
                category;

        this.originalFilename =
                originalFilename;

        this.storedFilename =
                storedFilename;

        this.contentType =
                contentType;

        this.sizeBytes =
                sizeBytes;

        this.sha256 =
                sha256;

        this.storageKey =
                storageKey;

        this.note =
                note;

        this.uploadedBy =
                uploadedBy;

        this.uploadedAt =
                Instant.now();
    }


    public static BidDocumentEntity create(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentCategory category,
            String originalFilename,
            String storedFilename,
            String contentType,
            long sizeBytes,
            String sha256,
            String storageKey,
            String note,
            String uploadedBy
    ) {

        return new BidDocumentEntity(
                organizationId,
                tender,
                category,
                originalFilename,
                storedFilename,
                contentType,
                sizeBytes,
                sha256,
                storageKey,
                note,
                uploadedBy
        );
    }


    @PrePersist
    void beforeInsert() {

        if (
                id == null
        ) {

            id =
                    UUID.randomUUID();
        }


        if (
                uploadedAt == null
        ) {

            uploadedAt =
                    Instant.now();
        }
    }


    public UUID getId() {
        return id;
    }


    public UUID getOrganizationId() {
        return organizationId;
    }


    public TenderEntity getTender() {
        return tender;
    }


    public BidDocumentCategory getCategory() {
        return category;
    }


    public String getOriginalFilename() {
        return originalFilename;
    }


    public String getStoredFilename() {
        return storedFilename;
    }


    public String getContentType() {
        return contentType;
    }


    public long getSizeBytes() {
        return sizeBytes;
    }


    public String getSha256() {
        return sha256;
    }


    public String getStorageKey() {
        return storageKey;
    }


    public String getNote() {
        return note;
    }


    public String getUploadedBy() {
        return uploadedBy;
    }


    public Instant getUploadedAt() {
        return uploadedAt;
    }

}
