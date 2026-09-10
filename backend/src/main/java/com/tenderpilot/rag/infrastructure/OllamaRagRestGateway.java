package com.tenderpilot.rag.infrastructure;

import com.tenderpilot.localai.infrastructure.LocalAiSettings;

import com.tenderpilot.rag.application.OllamaRagGateway;
import com.tenderpilot.rag.application.OllamaRagJsonSchemaFactory;
import com.tenderpilot.rag.application.OllamaRagPromptFactory;

import com.tenderpilot.rag.domain.OllamaRagAnswerResult;
import com.tenderpilot.rag.domain.RagContextItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.databind.ObjectMapper;

@Component
public class OllamaRagRestGateway
        implements OllamaRagGateway {

    private final LocalAiSettings
            localAiSettings;


    private final RagSettings
            ragSettings;


    private final OllamaRagPromptFactory
            promptFactory;


    private final OllamaRagJsonSchemaFactory
            schemaFactory;


    private final ObjectMapper
            objectMapper;


    private final RestClient
            restClient;


    public OllamaRagRestGateway(
            LocalAiSettings localAiSettings,
            RagSettings ragSettings,
            OllamaRagPromptFactory promptFactory,
            OllamaRagJsonSchemaFactory schemaFactory,
            ObjectMapper objectMapper
    ) {

        this.localAiSettings =
                localAiSettings;

        this.ragSettings =
                ragSettings;

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
                                localAiSettings.baseUrl()
                        )
                        .build();
    }


    @Override
    @SuppressWarnings("rawtypes")
    public RagModelStatus status() {

        if (
                !localAiSettings.enabled()
                ||
                !ragSettings.enabled()
        ) {

            return new RagModelStatus(
                    false,
                    false,
                    false,
                    false,
                    localAiSettings.model(),
                    ragSettings.embeddingModel(),
                    localAiSettings.baseUrl()
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


            Object models =
                    response.get(
                            "models"
                    );


            return new RagModelStatus(
                    true,

                    true,

                    modelAvailable(
                            models,
                            localAiSettings.model()
                    ),

                    modelAvailable(
                            models,
                            ragSettings.embeddingModel()
                    ),

                    localAiSettings.model(),

                    ragSettings.embeddingModel(),

                    localAiSettings.baseUrl()
            );


        } catch (
                RestClientException exception
        ) {

            return unreachable();
        }
    }


    @Override
    @SuppressWarnings("rawtypes")
    public List<List<Double>> embed(
            List<String> inputs
    ) {

        if (
                inputs == null
                ||
                inputs.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Embedding input cannot be empty"
            );
        }


        Map<String, Object> request =
                new LinkedHashMap<>();


        request.put(
                "model",
                ragSettings.embeddingModel()
        );


        request.put(
                "input",
                inputs
        );


        request.put(
                "truncate",
                true
        );


        request.put(
                "keep_alive",
                "5m"
        );


        Map response =
                restClient
                        .post()
                        .uri(
                                "/api/embed"
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
                    "Ollama returned no embedding response"
            );
        }


        Object raw =
                response.get(
                        "embeddings"
                );


        if (
                !(raw instanceof List<?> vectors)
        ) {

            throw new IllegalStateException(
                    "Ollama embedding response does not contain embeddings"
            );
        }


        List<List<Double>> result =
                new ArrayList<>(
                        vectors.size()
                );


        for (
                Object vectorRaw :
                vectors
        ) {

            if (
                    !(vectorRaw instanceof List<?> values)
            ) {

                throw new IllegalStateException(
                        "Invalid Ollama embedding vector"
                );
            }


            List<Double> vector =
                    new ArrayList<>(
                            values.size()
                    );


            for (
                    Object value :
                    values
            ) {

                if (
                        !(value instanceof Number number)
                ) {

                    throw new IllegalStateException(
                            "Invalid Ollama embedding value"
                    );
                }


                vector.add(
                        number.doubleValue()
                );
            }


            if (
                    vector.isEmpty()
            ) {

                throw new IllegalStateException(
                        "Ollama returned an empty embedding"
                );
            }


            result.add(
                    List.copyOf(
                            vector
                    )
            );
        }


        if (
                result.size()
                        !=
                        inputs.size()
        ) {

            throw new IllegalStateException(
                    "Ollama embedding count does not match input count"
            );
        }


        return List.copyOf(
                result
        );
    }


    @Override
    @SuppressWarnings("rawtypes")
    public OllamaRagAnswerResult answer(
            String question,
            List<RagContextItem> contexts
    ) {

        Map<String, Object> request =
                new LinkedHashMap<>();


        request.put(
                "model",
                localAiSettings.model()
        );


        request.put(
                "prompt",
                promptFactory.prompt(
                        question,
                        contexts
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
                        0.0
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
                    "Ollama returned no RAG answer"
            );
        }


        Object raw =
                response.get(
                        "response"
                );


        if (
                raw == null
                ||
                raw.toString()
                        .isBlank()
        ) {

            throw new IllegalStateException(
                    "Ollama returned an empty RAG answer"
            );
        }


        try {

            return objectMapper
                    .readValue(
                            raw.toString(),
                            OllamaRagAnswerResult.class
                    );


        } catch (
                Exception exception
        ) {

            throw new IllegalStateException(
                    "Ollama returned invalid RAG JSON",
                    exception
            );
        }
    }


    @SuppressWarnings("rawtypes")
    private boolean modelAvailable(
            Object rawModels,
            String expectedModel
    ) {

        if (
                !(rawModels instanceof List<?> models)
        ) {

            return false;
        }


        String expected =
                normalizeModel(
                        expectedModel
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


            for (
                    String key :
                    List.of(
                            "name",
                            "model"
                    )
            ) {

                Object value =
                        model.get(
                                key
                        );


                if (
                        value != null
                        &&
                        expected.equals(
                                normalizeModel(
                                        value.toString()
                                )
                        )
                ) {

                    return true;
                }
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


    private RagModelStatus unreachable() {

        return new RagModelStatus(
                true,
                false,
                false,
                false,
                localAiSettings.model(),
                ragSettings.embeddingModel(),
                localAiSettings.baseUrl()
        );
    }

}
