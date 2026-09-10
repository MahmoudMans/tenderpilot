package com.tenderpilot.localai.domain;

import com.tenderpilot.bidworkspace.domain.BidDocumentEntity;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_document_ai_analyses"
)
public class BidDocumentAnalysisEntity {

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


    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    private BidDocumentAnalysisStatus status;


    @Column(
            name = "provider",
            nullable = false,
            length = 80
    )
    private String provider;


    @Column(
            name = "model_name",
            nullable = false,
            length = 200
    )
    private String model;


    @Column(
            name = "prompt_version",
            nullable = false,
            length = 100
    )
    private String promptVersion;


    @Column(
            name = "input_text_sha256",
            length = 64
    )
    private String inputTextSha256;


    @Column(
            name = "summary",
            columnDefinition = "TEXT"
    )
    private String summary;


    @Column(
            name = "confidence_score"
    )
    private Integer confidenceScore;


    @Column(
            name = "error_message",
            columnDefinition = "TEXT"
    )
    private String errorMessage;


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "organization_bid_document_ai_findings",
            joinColumns = @JoinColumn(
                    name = "analysis_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<BidAiFindingValue> findings =
            new ArrayList<>();


    @Column(
            name = "analyzed_at",
            nullable = false
    )
    private Instant analyzedAt;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;


    protected BidDocumentAnalysisEntity() {
    }


    private BidDocumentAnalysisEntity(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentEntity document,
            String provider,
            String model,
            String promptVersion
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.document =
                document;

        this.provider =
                provider;

        this.model =
                model;

        this.promptVersion =
                promptVersion;

        this.status =
                BidDocumentAnalysisStatus.FAILED;
    }


    public static BidDocumentAnalysisEntity create(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentEntity document,
            String provider,
            String model,
            String promptVersion
    ) {

        return new BidDocumentAnalysisEntity(
                organizationId,
                tender,
                document,
                provider,
                model,
                promptVersion
        );
    }


    public void complete(
            String provider,
            String model,
            String promptVersion,
            String inputTextSha256,
            String summary,
            int confidence,
            List<BidAiFindingValue> findings
    ) {

        this.status =
                BidDocumentAnalysisStatus.COMPLETED;

        this.provider =
                provider;

        this.model =
                model;

        this.promptVersion =
                promptVersion;

        this.inputTextSha256 =
                inputTextSha256;

        this.summary =
                summary;

        this.confidenceScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                confidence
                        )
                );

        this.errorMessage =
                null;

        this.findings.clear();

        this.findings.addAll(
                findings
        );

        this.analyzedAt =
                Instant.now();
    }


    public void fail(
            String provider,
            String model,
            String promptVersion,
            String inputTextSha256,
            String errorMessage
    ) {

        this.status =
                BidDocumentAnalysisStatus.FAILED;

        this.provider =
                provider;

        this.model =
                model;

        this.promptVersion =
                promptVersion;

        this.inputTextSha256 =
                inputTextSha256;

        this.summary =
                null;

        this.confidenceScore =
                null;

        this.errorMessage =
                errorMessage;

        this.findings.clear();

        this.analyzedAt =
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
                createdAt == null
        ) {

            createdAt =
                    now;
        }


        if (
                analyzedAt == null
        ) {

            analyzedAt =
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


    public BidDocumentAnalysisStatus getStatus() {
        return status;
    }


    public String getProvider() {
        return provider;
    }


    public String getModel() {
        return model;
    }


    public String getPromptVersion() {
        return promptVersion;
    }


    public String getInputTextSha256() {
        return inputTextSha256;
    }


    public String getSummary() {
        return summary;
    }


    public Integer getConfidenceScore() {
        return confidenceScore;
    }


    public String getErrorMessage() {
        return errorMessage;
    }


    public List<BidAiFindingValue> getFindings() {

        return List.copyOf(
                findings
        );
    }


    public Instant getAnalyzedAt() {
        return analyzedAt;
    }

}
