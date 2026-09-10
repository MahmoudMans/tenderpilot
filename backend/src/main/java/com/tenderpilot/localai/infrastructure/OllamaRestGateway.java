package com.tenderpilot.localai.infrastructure;

import com.tenderpilot.localai.application.OllamaBidJsonSchemaFactory;
import com.tenderpilot.localai.application.OllamaBidPromptFactory;
import com.tenderpilot.localai.application.OllamaGateway;

import com.tenderpilot.localai.domain.OllamaBidAnalysisResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.databind.ObjectMapper;

@Component
public class OllamaRestGateway
        implements OllamaGateway {

    private final LocalAiSettings settings;

    private final OllamaBidPromptFactory promptFactory;

    private final OllamaBidJsonSchemaFactory schemaFactory;

    private final ObjectMapper objectMapper;

    private final RestClient restClient;


    public OllamaRestGateway(
            LocalAiSettings settings,
            OllamaBidPromptFactory promptFactory,
            OllamaBidJsonSchemaFactory schemaFactory,
            ObjectMapper objectMapper
    ) {

        this.settings =
                settings;

        this.promptFactory =
                promptFactory;

        this.schemaFactory =
                schemaFactory;

        this.objectMapper =
                objectMapper;

        this.restClient =
                RestClient
                        .builder()
                        .baseUrl(
                                settings.baseUrl()
                        )
                        .build();
    }


    @Override
    @SuppressWarnings("rawtypes")
    public OllamaStatus status() {

        if (
                !settings.enabled()
        ) {

            return new OllamaStatus(
                    false,
                    false,
                    false,
                    settings.provider(),
                    settings.model(),
                    settings.baseUrl()
            );
        }


        try {

            Map response =
                    restClient
                            .get()
                            .uri(
                                    "/api/tags"
                            )
                            .retrieve()
                            .body(
                                    Map.class
                            );


            if (
                    response == null
            ) {

                return unreachable();
            }


            boolean available =
                    modelAvailable(
                            response.get(
                                    "models"
                            )
                    );


            return new OllamaStatus(
                    true,
                    true,
                    available,
                    settings.provider(),
                    settings.model(),
                    settings.baseUrl()
            );


        } catch (
                RestClientException exception
        ) {

            return unreachable();
        }
    }


    @Override
    @SuppressWarnings("rawtypes")
    public OllamaBidAnalysisResult analyze(
            String filename,
            String extractedText
    ) {

        if (
                !settings.enabled()
        ) {

            throw new IllegalStateException(
                    "Local AI is disabled"
            );
        }


        Map<String, Object> request =
                new LinkedHashMap<>();


        request.put(
                "model",
                settings.model()
        );


        request.put(
                "prompt",
                promptFactory.prompt(
                        filename,
                        extractedText
                )
        );


        request.put(
                "format",
                schemaFactory.schema()
        );


        request.put(
                "stream",
                false
        );


        request.put(
                "think",
                false
        );


        request.put(
                "keep_alive",
                "5m"
        );


        request.put(
                "options",
                Map.of(
                        "temperature",
                        0.1
                )
        );


        Map response =
                restClient
                        .post()
                        .uri(
                                "/api/generate"
                        )
                        .body(
                                request
                        )
                        .retrieve()
                        .body(
                                Map.class
                        );


        if (
                response == null
        ) {

            throw new IllegalStateException(
                    "Ollama returned an empty response"
            );
        }


        Object generated =
                response.get(
                        "response"
                );


        if (
                generated == null
                ||
                generated.toString()
                        .isBlank()
        ) {

            throw new IllegalStateException(
                    "Ollama returned no structured analysis"
            );
        }


        try {

            return objectMapper
                    .readValue(
                            generated.toString(),
                            OllamaBidAnalysisResult.class
                    );


        } catch (
                Exception exception
        ) {

            throw new IllegalStateException(
                    "Ollama returned invalid structured analysis JSON",
                    exception
            );
        }
    }


    @SuppressWarnings("rawtypes")
    private boolean modelAvailable(
            Object rawModels
    ) {

        if (
                !(rawModels instanceof List<?> models)
        ) {

            return false;
        }


        String expected =
                normalizeModel(
                        settings.model()
                );


        for (
                Object raw :
                models
        ) {

            if (
                    !(raw instanceof Map model)
            ) {
                continue;
            }


            Object name =
                    model.get(
                            "name"
                    );


            Object canonical =
                    model.get(
                            "model"
                    );


            if (
                    expected.equals(
                            normalizeModel(
                                    name
                                            ==
                                            null
                                            ? null
                                            : name.toString()
                            )
                    )
                    ||
                    expected.equals(
                            normalizeModel(
                                    canonical
                                            ==
                                            null
                                            ? null
                                            : canonical.toString()
                            )
                    )
            ) {

                return true;
            }
        }


        return false;
    }


    private String normalizeModel(
            String value
    ) {

        if (
                value == null
        ) {

            return "";
        }


        String result =
                value
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        if (
                result.endsWith(
                        ":latest"
                )
        ) {

            result =
                    result.substring(
                            0,
                            result.length() - 7
                    );
        }


        return result;
    }


    private OllamaStatus unreachable() {

        return new OllamaStatus(
                true,
                false,
                false,
                settings.provider(),
                settings.model(),
                settings.baseUrl()
        );
    }

}
