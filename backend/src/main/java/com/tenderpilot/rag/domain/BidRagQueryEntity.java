package com.tenderpilot.rag.domain;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "organization_bid_rag_queries"
)
public class BidRagQueryEntity {

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


    @Column(
            name = "question",
            nullable = false,
            length = 5000
    )
    private String question;


    @Column(
            name = "answer",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String answer;


    @Column(
            name = "supported",
            nullable = false
    )
    private boolean supported;


    @Column(
            name = "generation_model",
            nullable = false,
            length = 200
    )
    private String generationModel;


    @Column(
            name = "embedding_model",
            nullable = false,
            length = 200
    )
    private String embeddingModel;


    @Column(
            name = "prompt_version",
            nullable = false,
            length = 100
    )
    private String promptVersion;


    @Column(
            name = "created_by",
            nullable = false,
            length = 320
    )
    private String createdBy;


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "organization_bid_rag_query_citations",
            joinColumns = @JoinColumn(
                    name = "query_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<BidRagCitationValue> citations =
            new ArrayList<>();


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected BidRagQueryEntity() {
    }


    private BidRagQueryEntity(
            UUID organizationId,
            TenderEntity tender,
            String question,
            String answer,
            boolean supported,
            String generationModel,
            String embeddingModel,
            String promptVersion,
            String createdBy,
            List<BidRagCitationValue> citations
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.question =
                question;

        this.answer =
                answer;

        this.supported =
                supported;

        this.generationModel =
                generationModel;

        this.embeddingModel =
                embeddingModel;

        this.promptVersion =
                promptVersion;

        this.createdBy =
                createdBy;

        this.citations.addAll(
                citations
        );
    }


    public static BidRagQueryEntity create(
            UUID organizationId,
            TenderEntity tender,
            String question,
            String answer,
            boolean supported,
            String generationModel,
            String embeddingModel,
            String promptVersion,
            String createdBy,
            List<BidRagCitationValue> citations
    ) {

        return new BidRagQueryEntity(
                organizationId,
                tender,
                question,
                answer,
                supported,
                generationModel,
                embeddingModel,
                promptVersion,
                createdBy,
                citations
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
                createdAt == null
        ) {

            createdAt =
                    Instant.now();
        }
    }


    public UUID getId() {
        return id;
    }


    public String getQuestion() {
        return question;
    }


    public String getAnswer() {
        return answer;
    }


    public boolean isSupported() {
        return supported;
    }


    public String getGenerationModel() {
        return generationModel;
    }


    public String getEmbeddingModel() {
        return embeddingModel;
    }


    public List<BidRagCitationValue> getCitations() {

        return List.copyOf(
                citations
        );
    }


    public Instant getCreatedAt() {
        return createdAt;
    }

}
