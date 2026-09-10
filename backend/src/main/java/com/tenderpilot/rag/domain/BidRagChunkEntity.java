package com.tenderpilot.rag.domain;

import com.tenderpilot.bidworkspace.domain.BidDocumentEntity;

import com.tenderpilot.procurement.domain.TenderEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "organization_bid_rag_chunks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bid_rag_document_chunk",
                columnNames = {
                        "document_id",
                        "chunk_index"
                }
        )
)
public class BidRagChunkEntity {

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


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "document_id",
            nullable = false
    )
    private BidDocumentEntity document;


    @Column(
            name = "chunk_index",
            nullable = false
    )
    private int chunkIndex;


    @Column(
            name = "start_offset",
            nullable = false
    )
    private int startOffset;


    @Column(
            name = "end_offset",
            nullable = false
    )
    private int endOffset;


    @Column(
            name = "chunk_sha256",
            nullable = false,
            length = 64
    )
    private String chunkSha256;


    @Column(
            name = "chunk_text",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String chunkText;


    @Column(
            name = "embedding_model",
            nullable = false,
            length = 200
    )
    private String embeddingModel;


    @Column(
            name = "embedding_dimensions",
            nullable = false
    )
    private int embeddingDimensions;


    @Column(
            name = "embedding_data",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String embeddingData;


    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;


    protected BidRagChunkEntity() {
    }


    private BidRagChunkEntity(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentEntity document,
            BidRagChunk chunk,
            String chunkSha256,
            String embeddingModel,
            int embeddingDimensions,
            String embeddingData
    ) {

        this.id =
                UUID.randomUUID();

        this.organizationId =
                organizationId;

        this.tender =
                tender;

        this.document =
                document;

        this.chunkIndex =
                chunk.index();

        this.startOffset =
                chunk.startOffset();

        this.endOffset =
                chunk.endOffset();

        this.chunkSha256 =
                chunkSha256;

        this.chunkText =
                chunk.text();

        this.embeddingModel =
                embeddingModel;

        this.embeddingDimensions =
                embeddingDimensions;

        this.embeddingData =
                embeddingData;
    }


    public static BidRagChunkEntity create(
            UUID organizationId,
            TenderEntity tender,
            BidDocumentEntity document,
            BidRagChunk chunk,
            String chunkSha256,
            String embeddingModel,
            int embeddingDimensions,
            String embeddingData
    ) {

        return new BidRagChunkEntity(
                organizationId,
                tender,
                document,
                chunk,
                chunkSha256,
                embeddingModel,
                embeddingDimensions,
                embeddingData
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


    public UUID getOrganizationId() {
        return organizationId;
    }


    public TenderEntity getTender() {
        return tender;
    }


    public BidDocumentEntity getDocument() {
        return document;
    }


    public int getChunkIndex() {
        return chunkIndex;
    }


    public int getStartOffset() {
        return startOffset;
    }


    public int getEndOffset() {
        return endOffset;
    }


    public String getChunkSha256() {
        return chunkSha256;
    }


    public String getChunkText() {
        return chunkText;
    }


    public String getEmbeddingModel() {
        return embeddingModel;
    }


    public int getEmbeddingDimensions() {
        return embeddingDimensions;
    }


    public String getEmbeddingData() {
        return embeddingData;
    }

}
