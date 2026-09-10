package com.tenderpilot.rag.infrastructure;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class RagSettings {

    private final boolean enabled;

    private final String embeddingModel;

    private final int chunkSizeChars;

    private final int chunkOverlapChars;

    private final int defaultTopK;

    private final int maxTopK;

    private final double minimumSimilarity;

    private final int maximumQuestionChars;

    private final String promptVersion;


    public RagSettings(

            @Value(
                    "${tenderpilot.rag.enabled:true}"
            )
            boolean enabled,

            @Value(
                    "${tenderpilot.rag.embedding-model:nomic-embed-text-v2-moe}"
            )
            String embeddingModel,

            @Value(
                    "${tenderpilot.rag.chunk-size-chars:1600}"
            )
            int chunkSizeChars,

            @Value(
                    "${tenderpilot.rag.chunk-overlap-chars:240}"
            )
            int chunkOverlapChars,

            @Value(
                    "${tenderpilot.rag.default-top-k:5}"
            )
            int defaultTopK,

            @Value(
                    "${tenderpilot.rag.max-top-k:8}"
            )
            int maxTopK,

            @Value(
                    "${tenderpilot.rag.minimum-similarity:0.15}"
            )
            double minimumSimilarity,

            @Value(
                    "${tenderpilot.rag.maximum-question-chars:1200}"
            )
            int maximumQuestionChars,

            @Value(
                    "${tenderpilot.rag.prompt-version:PRIVATE_BID_RAG_V1}"
            )
            String promptVersion

    ) {

        this.enabled =
                enabled;

        this.embeddingModel =
                embeddingModel == null
                        ||
                        embeddingModel.isBlank()
                        ? "nomic-embed-text-v2-moe"
                        : embeddingModel.trim();

        this.chunkSizeChars =
                Math.max(
                        400,
                        Math.min(
                                5000,
                                chunkSizeChars
                        )
                );

        this.chunkOverlapChars =
                Math.max(
                        0,
                        Math.min(
                                this.chunkSizeChars / 3,
                                chunkOverlapChars
                        )
                );

        this.defaultTopK =
                Math.max(
                        1,
                        Math.min(
                                8,
                                defaultTopK
                        )
                );

        this.maxTopK =
                Math.max(
                        this.defaultTopK,
                        Math.min(
                                12,
                                maxTopK
                        )
                );

        this.minimumSimilarity =
                Math.max(
                        -1.0,
                        Math.min(
                                1.0,
                                minimumSimilarity
                        )
                );

        this.maximumQuestionChars =
                Math.max(
                        100,
                        Math.min(
                                5000,
                                maximumQuestionChars
                        )
                );

        this.promptVersion =
                promptVersion == null
                        ||
                        promptVersion.isBlank()
                        ? "PRIVATE_BID_RAG_V1"
                        : promptVersion.trim();
    }


    public boolean enabled() {
        return enabled;
    }


    public String embeddingModel() {
        return embeddingModel;
    }


    public int chunkSizeChars() {
        return chunkSizeChars;
    }


    public int chunkOverlapChars() {
        return chunkOverlapChars;
    }


    public int defaultTopK() {
        return defaultTopK;
    }


    public int maxTopK() {
        return maxTopK;
    }


    public double minimumSimilarity() {
        return minimumSimilarity;
    }


    public int maximumQuestionChars() {
        return maximumQuestionChars;
    }


    public String promptVersion() {
        return promptVersion;
    }

}
