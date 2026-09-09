package com.tenderpilot.search.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenSearchSettings {

    private final boolean enabled;

    private final boolean autoIndex;

    private final String baseUrl;

    private final String indexName;

    private final int defaultSize;

    private final int maxSize;


    public OpenSearchSettings(

            @Value(
                    "${tenderpilot.search.enabled:true}"
            )
            boolean enabled,

            @Value(
                    "${tenderpilot.search.auto-index:true}"
            )
            boolean autoIndex,

            @Value(
                    "${tenderpilot.search.base-url:http://localhost:9200}"
            )
            String baseUrl,

            @Value(
                    "${tenderpilot.search.index-name:tenderpilot-opportunities-v1}"
            )
            String indexName,

            @Value(
                    "${tenderpilot.search.default-size:25}"
            )
            int defaultSize,

            @Value(
                    "${tenderpilot.search.max-size:100}"
            )
            int maxSize

    ) {

        this.enabled =
                enabled;

        this.autoIndex =
                autoIndex;

        this.baseUrl =
                removeTrailingSlash(
                        baseUrl
                );

        this.indexName =
                normalizeIndexName(
                        indexName
                );

        this.defaultSize =
                Math.max(
                        1,
                        defaultSize
                );

        this.maxSize =
                Math.max(
                        1,
                        Math.min(
                                100,
                                maxSize
                        )
                );

    }


    private String removeTrailingSlash(
            String value
    ) {

        String result =
                value.trim();


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


    private String normalizeIndexName(
            String value
    ) {

        String result =
                value
                        .trim()
                        .toLowerCase();


        if (
                !result.matches(
                        "[a-z0-9._-]+"
                )
                ||
                result.startsWith("_")
                ||
                result.startsWith("-")
        ) {

            throw new IllegalArgumentException(
                    "Invalid OpenSearch procurement index name"
            );

        }


        return result;

    }


    public boolean enabled() {
        return enabled;
    }


    public boolean autoIndex() {
        return autoIndex;
    }


    public String baseUrl() {
        return baseUrl;
    }


    public String indexName() {
        return indexName;
    }


    public int defaultSize() {
        return defaultSize;
    }


    public int maxSize() {
        return maxSize;
    }

}
