package com.tenderpilot.rag.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class BidRagCitationValue {

    @Column(
            name = "chunk_id",
            nullable = false
    )
    private UUID chunkId;


    @Column(
            name = "source_id",
            nullable = false,
            length = 30
    )
    private String sourceId;


    @Column(
            name = "document_id",
            nullable = false
    )
    private UUID documentId;


    @Column(
            name = "filename",
            nullable = false,
            length = 500
    )
    private String filename;


    @Column(
            name = "chunk_index",
            nullable = false
    )
    private int chunkIndex;


    @Column(
            name = "similarity",
            nullable = false
    )
    private double similarity;


    @Column(
            name = "excerpt",
            nullable = false,
            length = 1500
    )
    private String excerpt;


    protected BidRagCitationValue() {
    }


    public BidRagCitationValue(
            UUID chunkId,
            String sourceId,
            UUID documentId,
            String filename,
            int chunkIndex,
            double similarity,
            String excerpt
    ) {

        this.chunkId =
                chunkId;

        this.sourceId =
                sourceId;

        this.documentId =
                documentId;

        this.filename =
                filename;

        this.chunkIndex =
                chunkIndex;

        this.similarity =
                similarity;

        this.excerpt =
                excerpt;
    }


    public UUID getChunkId() {
        return chunkId;
    }


    public String getSourceId() {
        return sourceId;
    }


    public UUID getDocumentId() {
        return documentId;
    }


    public String getFilename() {
        return filename;
    }


    public int getChunkIndex() {
        return chunkIndex;
    }


    public double getSimilarity() {
        return similarity;
    }


    public String getExcerpt() {
        return excerpt;
    }

}
