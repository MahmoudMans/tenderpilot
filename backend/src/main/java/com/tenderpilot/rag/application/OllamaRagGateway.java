package com.tenderpilot.rag.application;

import com.tenderpilot.rag.domain.OllamaRagAnswerResult;
import com.tenderpilot.rag.domain.RagContextItem;

import java.util.List;

public interface OllamaRagGateway {

    RagModelStatus status();


    List<List<Double>> embed(
            List<String> inputs
    );


    OllamaRagAnswerResult answer(
            String question,
            List<RagContextItem> contexts
    );


    record RagModelStatus(

            boolean enabled,

            boolean reachable,

            boolean generationModelAvailable,

            boolean embeddingModelAvailable,

            String generationModel,

            String embeddingModel,

            String baseUrl

    ) {
    }

}
