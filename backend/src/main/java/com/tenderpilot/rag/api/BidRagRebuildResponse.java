package com.tenderpilot.rag.api;

public record BidRagRebuildResponse(

        int totalDocuments,

        int indexedDocuments,

        int skippedDocuments,

        int chunks,

        String embeddingModel

) {
}
