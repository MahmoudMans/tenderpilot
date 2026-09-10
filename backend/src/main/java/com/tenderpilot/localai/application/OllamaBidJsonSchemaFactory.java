package com.tenderpilot.localai.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class OllamaBidJsonSchemaFactory {

    public Map<String, Object> schema() {

        Map<String, Object> findingProperties =
                new LinkedHashMap<>();


        findingProperties.put(
                "type",
                Map.of(
                        "type",
                        "string",
                        "enum",
                        List.of(
                                "REQUIREMENT",
                                "RISK",
                                "DEADLINE",
                                "DELIVERABLE",
                                "QUALIFICATION",
                                "COMMERCIAL",
                                "OTHER"
                        )
                )
        );


        findingProperties.put(
                "title",
                Map.of(
                        "type",
                        "string"
                )
        );


        findingProperties.put(
                "detail",
                Map.of(
                        "type",
                        "string"
                )
        );


        findingProperties.put(
                "evidence",
                Map.of(
                        "type",
                        "string"
                )
        );


        findingProperties.put(
                "mandatory",
                Map.of(
                        "type",
                        "boolean"
                )
        );


        findingProperties.put(
                "requirementCategory",
                Map.of(
                        "type",
                        "string",
                        "enum",
                        List.of(
                                "ADMINISTRATIVE",
                                "TECHNICAL",
                                "COMMERCIAL",
                                "FINANCIAL",
                                "LEGAL",
                                "SUBMISSION",
                                "OTHER"
                        )
                )
        );


        Map<String, Object> finding =
                new LinkedHashMap<>();

        finding.put(
                "type",
                "object"
        );

        finding.put(
                "additionalProperties",
                false
        );

        finding.put(
                "properties",
                findingProperties
        );

        finding.put(
                "required",
                List.of(
                        "type",
                        "title",
                        "detail",
                        "evidence",
                        "mandatory",
                        "requirementCategory"
                )
        );


        Map<String, Object> properties =
                new LinkedHashMap<>();


        properties.put(
                "summary",
                Map.of(
                        "type",
                        "string"
                )
        );


        properties.put(
                "confidence",
                Map.of(
                        "type",
                        "integer",
                        "minimum",
                        0,
                        "maximum",
                        100
                )
        );


        properties.put(
                "findings",
                Map.of(
                        "type",
                        "array",
                        "items",
                        finding
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
                        "summary",
                        "confidence",
                        "findings"
                )
        );


        return schema;
    }

}
