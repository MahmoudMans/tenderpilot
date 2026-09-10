package com.tenderpilot.localai.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalAiSettings {

    private final boolean enabled;

    private final String provider;

    private final String baseUrl;

    private final String model;

    private final int maxExtractedChars;

    private final int maxInputChars;

    private final String promptVersion;


    public LocalAiSettings(

            @Value(
                    "${tenderpilot.local-ai.enabled:true}"
            )
            boolean enabled,

            @Value(
                    "${tenderpilot.local-ai.provider:OLLAMA}"
            )
            String provider,

            @Value(
                    "${tenderpilot.local-ai.base-url:http://localhost:11434}"
            )
            String baseUrl,

            @Value(
                    "${tenderpilot.local-ai.model:qwen3:4b}"
            )
            String model,

            @Value(
                    "${tenderpilot.local-ai.max-extracted-chars:120000}"
            )
            int maxExtractedChars,

            @Value(
                    "${tenderpilot.local-ai.max-input-chars:60000}"
            )
            int maxInputChars,

            @Value(
                    "${tenderpilot.local-ai.prompt-version:BID_DOCUMENT_ANALYSIS_V1}"
            )
            String promptVersion

    ) {

        this.enabled =
                enabled;

        this.provider =
                required(
                        provider,
                        "OLLAMA"
                );

        this.baseUrl =
                trimTrailingSlash(
                        required(
                                baseUrl,
                                "http://localhost:11434"
                        )
                );

        this.model =
                required(
                        model,
                        "qwen3:4b"
                );

        this.maxExtractedChars =
                Math.max(
                        10000,
                        Math.min(
                                500000,
                                maxExtractedChars
                        )
                );

        this.maxInputChars =
                Math.max(
                        5000,
                        Math.min(
                                this.maxExtractedChars,
                                maxInputChars
                        )
                );

        this.promptVersion =
                required(
                        promptVersion,
                        "BID_DOCUMENT_ANALYSIS_V1"
                );
    }


    private String required(
            String value,
            String fallback
    ) {

        if (
                value == null
                ||
                value.isBlank()
        ) {

            return fallback;
        }


        return value.trim();
    }


    private String trimTrailingSlash(
            String value
    ) {

        String result =
                value;


        while (
                result.endsWith("/")
        ) {

            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }


        return result;
    }


    public boolean enabled() {
        return enabled;
    }


    public String provider() {
        return provider;
    }


    public String baseUrl() {
        return baseUrl;
    }


    public String model() {
        return model;
    }


    public int maxExtractedChars() {
        return maxExtractedChars;
    }


    public int maxInputChars() {
        return maxInputChars;
    }


    public String promptVersion() {
        return promptVersion;
    }

}
