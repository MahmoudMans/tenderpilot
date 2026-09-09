package com.tenderpilot.search.infrastructure;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class OpenSearchIndexDefinition {

    public Map<String, Object> createIndexBody() {

        Map<String, Object> properties =
                new LinkedHashMap<>();


        properties.put(
                "id",
                field(
                        "keyword"
                )
        );

        properties.put(
                "canonicalKey",
                field(
                        "keyword"
                )
        );

        properties.put(
                "title",
                field(
                        "text"
                )
        );

        properties.put(
                "description",
                field(
                        "text"
                )
        );

        properties.put(
                "buyerName",
                field(
                        "text"
                )
        );

        properties.put(
                "buyerCategory",
                field(
                        "keyword"
                )
        );

        properties.put(
                "status",
                field(
                        "keyword"
                )
        );

        properties.put(
                "procedureType",
                field(
                        "keyword"
                )
        );

        properties.put(
                "contractNature",
                field(
                        "keyword"
                )
        );

        properties.put(
                "publicationDate",
                field(
                        "date"
                )
        );

        properties.put(
                "submissionDeadline",
                field(
                        "date"
                )
        );

        properties.put(
                "estimatedMinValue",
                field(
                        "double"
                )
        );

        properties.put(
                "estimatedMaxValue",
                field(
                        "double"
                )
        );

        properties.put(
                "currency",
                field(
                        "keyword"
                )
        );

        properties.put(
                "countryCode",
                field(
                        "keyword"
                )
        );

        properties.put(
                "regionCode",
                field(
                        "keyword"
                )
        );

        properties.put(
                "regionName",
                field(
                        "keyword"
                )
        );

        properties.put(
                "frameworkAgreement",
                field(
                        "boolean"
                )
        );

        properties.put(
                "jointProcurement",
                field(
                        "boolean"
                )
        );

        properties.put(
                "electronicSubmission",
                field(
                        "boolean"
                )
        );

        properties.put(
                "cpvCodes",
                field(
                        "keyword"
                )
        );

        properties.put(
                "cpvLabels",
                field(
                        "text"
                )
        );

        properties.put(
                "sources",
                field(
                        "keyword"
                )
        );

        properties.put(
                "lotTitles",
                field(
                        "text"
                )
        );

        properties.put(
                "awardedAmount",
                field(
                        "double"
                )
        );

        properties.put(
                "awardedCurrency",
                field(
                        "keyword"
                )
        );

        properties.put(
                "supplierNames",
                field(
                        "text"
                )
        );

        properties.put(
                "supplierIdentifiers",
                field(
                        "keyword"
                )
        );

        properties.put(
                "qualityScore",
                field(
                        "integer"
                )
        );

        properties.put(
                "qualityGrade",
                field(
                        "keyword"
                )
        );

        properties.put(
                "qualityIssueCodes",
                field(
                        "keyword"
                )
        );


        Map<String, Object> settings =
                new LinkedHashMap<>();

        settings.put(
                "number_of_shards",
                1
        );

        settings.put(
                "number_of_replicas",
                0
        );


        Map<String, Object> mappings =
                new LinkedHashMap<>();

        mappings.put(
                "dynamic",
                "strict"
        );

        mappings.put(
                "properties",
                properties
        );


        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "settings",
                settings
        );

        body.put(
                "mappings",
                mappings
        );


        return body;

    }


    private Map<String, Object> field(
            String type
    ) {

        return Map.of(
                "type",
                type
        );

    }

}
