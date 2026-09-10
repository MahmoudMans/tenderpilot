package com.tenderpilot.rag.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class OllamaRagJsonSchemaFactory {

    public Map<String, Object> schema() {

        Map<String, Object> properties =
                new LinkedHashMap<>();


        properties.put(
                "answer",
                Map.of(
                        "type",
                        "string"
                )
        );


        properties.put(
                "supported",
                Map.of(
                        "type",
                        "boolean"
                )
        );


        properties.put(
                "sourceIds",
                Map.of(
                        "type",
                        "array",
                        "items",
                        Map.of(
                                "type",
                                "string"
                        )
                )
        );


        Map<String, Object> schema =
                new LinkedHashMap<>();


        schema.put(
                "type",
                "object"
        );


        schema.put(
                "additionalProperties",
                false
        );


        schema.put(
                "properties",
                properties
        );


        schema.put(
                "required",
                List.of(
                        "answer",
                        "supported",
                        "sourceIds"
                )
        );


        return schema;
    }

}
