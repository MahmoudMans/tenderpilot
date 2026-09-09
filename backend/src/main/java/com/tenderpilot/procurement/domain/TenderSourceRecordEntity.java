package com.tenderpilot.procurement.domain;

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
        name = "procurement_tender_sources",
        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_procurement_source_external_id",
                        columnNames = {
                                "source",
                                "external_id"
                        }
                )

        }
)
public class TenderSourceRecordEntity {

    @Id
    private UUID id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "tender_id",
            nullable = false
    )
    private TenderEntity tender;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "source",
            nullable = false,
            length = 40
    )
    private ProcurementSource source;


    @Column(
            name = "external_id",
            nullable = false,
            length = 220
    )
    private String externalId;


    @Column(
            name = "external_version",
            length = 120
    )
    private String externalVersion;


    @Column(
            name = "notice_type",
            length = 120
    )
    private String noticeType;


    @Column(
            name = "source_url",
            length = 1000
    )
    private String sourceUrl;


    @Column(
            name = "payload_checksum",
            length = 128
    )
    private String payloadChecksum;


    @Column(
            name = "source_published_at"
    )
    private Instant sourcePublishedAt;


    @Column(
            name = "first_seen_at",
            nullable = false
    )
    private Instant firstSeenAt;


    @Column(
            name = "last_seen_at",
            nullable = false
    )
    private Instant lastSeenAt;


    protected TenderSourceRecordEntity() {
    }


    private TenderSourceRecordEntity(

            TenderEntity tender,

            ProcurementSource source,

            String externalId

    ) {

        this.id =
                UUID.randomUUID();

        this.tender =
                tender;

        this.source =
                source;

        this.externalId =
                externalId;

    }


    public static TenderSourceRecordEntity create(

            TenderEntity tender,

            ProcurementSource source,

            String externalId

    ) {

        return new TenderSourceRecordEntity(

                tender,

                source,

                externalId

        );

    }


    public void touch(

            String externalVersion,

            String noticeType,

            String sourceUrl,

            String payloadChecksum,

            Instant sourcePublishedAt

    ) {

        this.externalVersion =
                externalVersion;

        this.noticeType =
                noticeType;

        this.sourceUrl =
                sourceUrl;

        this.payloadChecksum =
                payloadChecksum;

        this.sourcePublishedAt =
                sourcePublishedAt;

        this.lastSeenAt =
                Instant.now();

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
                firstSeenAt == null
        ) {

            firstSeenAt =
                    now;

        }


        if (
                lastSeenAt == null
        ) {

            lastSeenAt =
                    now;

        }

    }


    public UUID getId() {

        return id;

    }


    public TenderEntity getTender() {

        return tender;

    }


    public ProcurementSource getSource() {

        return source;

    }


    public String getExternalId() {

        return externalId;

    }


    public String getExternalVersion() {

        return externalVersion;

    }


    public String getNoticeType() {

        return noticeType;

    }


    public String getSourceUrl() {

        return sourceUrl;

    }


    public String getPayloadChecksum() {

        return payloadChecksum;

    }


    public Instant getSourcePublishedAt() {

        return sourcePublishedAt;

    }


    public Instant getFirstSeenAt() {

        return firstSeenAt;

    }


    public Instant getLastSeenAt() {

        return lastSeenAt;

    }

}
