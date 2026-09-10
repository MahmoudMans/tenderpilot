package com.tenderpilot.localai.application;

import com.tenderpilot.localai.domain.OllamaBidAnalysisResult;

public interface OllamaGateway {

    OllamaStatus status();


    OllamaBidAnalysisResult analyze(
            String filename,
            String extractedText
    );


    record OllamaStatus(

            boolean enabled,

            boolean reachable,

            boolean modelAvailable,

            String provider,

            String model,

            String baseUrl

    ) {
    }

}
