package com.tenderpilot.rag.api;

public record RagStatusResponse(

        boolean enabled,

        boolean reachable,

        boolean generationModelAvailable,

        boolean embeddingModelAvailable,

        String generationModel,

        String embeddingModel,

        String baseUrl,

        String vectorStore,

        String dataBoundary

) {
}
