package com.tenderpilot.rag.api;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

public record BidRagAnswerResponse(

        UUID id,

        String question,

        String answer,

        boolean supported,

        String generationModel,

        String embeddingModel,

        Instant createdAt,

        List<CitationResponse> citations

) {

    public record CitationResponse(

            String sourceId,

            UUID documentId,

            String filename,

            int chunkIndex,

            double similarity,

            String excerpt

    ) {
    }

}
