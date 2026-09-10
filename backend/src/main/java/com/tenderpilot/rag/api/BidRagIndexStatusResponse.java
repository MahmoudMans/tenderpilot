package com.tenderpilot.rag.api;

public record BidRagIndexStatusResponse(

        int documentCount,

        int indexedDocumentCount,

        int chunkCount,

        boolean ready,

        String embeddingModel

) {
}
