package com.tenderpilot.localai.domain;

import com.tenderpilot.bidworkspace.domain.BidDocumentEntity;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_document_texts"
)
public class BidDocumentTextEntity {

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


    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "document_id",
            nullable = false,
            unique = true
    )
    private BidDocumentEntity document;


    @Column(
            name = "source_sha256",
            nullable = false,
            length = 64
    )
    private String sourceSha256;


    @Column(
            name = "text_sha256",
            nullable = false,
            length = 64
    )
    private String textSha256;


    @Column(
            name = "detected_media_type",
            length = 200
    )
    private String detectedMediaType;


    @Column(
            name = "extractor_version",
            nullable = false,
            length = 100
    )
    private String extractorVersion;


    @Column(
            name = "character_count",
            nullable = false
    )
    private int characterCount;


    @Column(
            name = "truncated",
            nullable = false
    )
    private boolean truncated;


    @Column(
            name = "extracted_text",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String extractedText;


    @Column(
            name = "extracted_at",
            nullable = false
    )
    private Instant extractedAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;


    protected BidDocumentTextEntity() {
    }


    private BidDocumentTextEntity(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentEntity document
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.document =
                document;
    }


    public static BidDocumentTextEntity create(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentEntity document
    ) {

        return new BidDocumentTextEntity(
                organizationId,
                tender,
                document
        );
    }


    public void apply(
            String sourceSha256,
            String textSha256,
            BidDocumentExtractionResult result
    ) {

        this.sourceSha256 =
                sourceSha256;

        this.textSha256 =
                textSha256;

        this.detectedMediaType =
                result.detectedMediaType();

        this.extractorVersion =
                result.extractorVersion();

        this.characterCount =
                result.characterCount();

        this.truncated =
                result.truncated();

        this.extractedText =
                result.text();

        this.extractedAt =
                Instant.now();
    }


    public boolean isCurrentFor(
            String sha256
    ) {

        return sourceSha256 != null
                &&
                sourceSha256.equals(
                        sha256
                );
    }


    @PrePersist
    void beforeInsert() {

        Instant now =
                Instant.now();


        if (
                id == null
        ) {

            id =
                    UUID.randomUUID();
        }


        if (
                extractedAt == null
        ) {

            extractedAt =
                    now;
        }


        updatedAt =
                now;
    }


    @PreUpdate
    void beforeUpdate() {

        updatedAt =
                Instant.now();
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


    public BidDocumentEntity getDocument() {
        return document;
    }


    public String getSourceSha256() {
        return sourceSha256;
    }


    public String getTextSha256() {
        return textSha256;
    }


    public String getDetectedMediaType() {
        return detectedMediaType;
    }


    public String getExtractorVersion() {
        return extractorVersion;
    }


    public int getCharacterCount() {
        return characterCount;
    }


    public boolean isTruncated() {
        return truncated;
    }


    public String getExtractedText() {
        return extractedText;
    }


    public Instant getExtractedAt() {
        return extractedAt;
    }

}
