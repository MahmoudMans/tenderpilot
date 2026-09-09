package com.tenderpilot.search.infrastructure;

import com.tenderpilot.search.application.OpenSearchGateway;

import com.tenderpilot.search.domain.ProcurementSearchDocument;
import com.tenderpilot.search.domain.ProcurementSearchQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class OpenSearchProcurementGateway
        implements OpenSearchGateway {

    private final OpenSearchSettings
            settings;


    private final OpenSearchIndexDefinition
            indexDefinition;


    private final OpenSearchQueryBuilder
            queryBuilder;


    private final RestClient
            restClient;


    public OpenSearchProcurementGateway(

            OpenSearchSettings settings,

            OpenSearchIndexDefinition indexDefinition,

            OpenSearchQueryBuilder queryBuilder

    ) {

        this.settings =
                settings;

        this.indexDefinition =
                indexDefinition;

        this.queryBuilder =
                queryBuilder;

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
    public boolean isAvailable() {

        if (
                !settings.enabled()
        ) {
            return false;
        }


        try {

            Map response =
                    restClient
                            .get()
                            .uri("/")
                            .retrieve()
                            .body(
                                    Map.class
                            );


            return response != null;

        } catch (
                RestClientException exception
        ) {

            return false;

        }

    }


    @Override
    @SuppressWarnings("rawtypes")
    public boolean indexExists() {

        if (
                !settings.enabled()
        ) {
            return false;
        }


        try {

            Map response =
                    restClient
                            .get()
                            .uri(
                                    "/{index}",
                                    settings.indexName()
                            )
                            .retrieve()
                            .body(
                                    Map.class
                            );


            return response != null;

        } catch (
                RestClientResponseException exception
        ) {

            if (
                    exception
                            .getStatusCode()
                            .value()
                            ==
                            404
            ) {
                return false;
            }


            throw exception;

        }

    }


    @Override
    public void ensureIndex() {

        if (
                !settings.enabled()
        ) {
            return;
        }


        if (
                indexExists()
        ) {
            return;
        }


        restClient
                .put()
                .uri(
                        "/{index}",
                        settings.indexName()
                )
                .body(
                        indexDefinition
                                .createIndexBody()
                )
                .retrieve()
                .toBodilessEntity();

    }


    @Override
    public void recreateIndex() {

        if (
                !settings.enabled()
        ) {
            return;
        }


        try {

            restClient
                    .delete()
                    .uri(
                            "/{index}",
                            settings.indexName()
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (
                RestClientResponseException exception
        ) {

            if (
                    exception
                            .getStatusCode()
                            .value()
                            !=
                            404
            ) {
                throw exception;
            }

        }


        restClient
                .put()
                .uri(
                        "/{index}",
                        settings.indexName()
                )
                .body(
                        indexDefinition
                                .createIndexBody()
                )
                .retrieve()
                .toBodilessEntity();

    }


    @Override
    public void index(
            ProcurementSearchDocument document
    ) {

        restClient
                .put()
                .uri(
                        "/{index}/_doc/{id}",
                        settings.indexName(),
                        document
                                .id()
                                .toString()
                )
                .body(
                        document.toSource()
                )
                .retrieve()
                .toBodilessEntity();

    }


    @Override
    @SuppressWarnings("rawtypes")
    public long count() {

        if (
                !indexExists()
        ) {
            return 0;
        }


        Map response =
                restClient
                        .get()
                        .uri(
                                "/{index}/_count",
                                settings.indexName()
                        )
                        .retrieve()
                        .body(
                                Map.class
                        );


        if (
                response == null
        ) {
            return 0;
        }


        Object count =
                response.get(
                        "count"
                );


        return count instanceof Number number
                ? number.longValue()
                : 0;

    }


    @Override
    @SuppressWarnings("rawtypes")
    public SearchPage search(
            ProcurementSearchQuery query
    ) {

        ensureIndex();


        Map response =
                restClient
                        .post()
                        .uri(
                                "/{index}/_search",
                                settings.indexName()
                        )
                        .body(
                                queryBuilder
                                        .build(
                                                query
                                        )
                        )
                        .retrieve()
                        .body(
                                Map.class
                        );


        if (
                response == null
        ) {

            throw new IllegalStateException(
                    "OpenSearch returned an empty search response"
            );

        }


        Object hitsValue =
                response.get(
                        "hits"
                );


        if (
                !(hitsValue instanceof Map hits)
        ) {

            return new SearchPage(
                    0,
                    List.of()
            );

        }


        long total =
                parseTotal(
                        hits.get(
                                "total"
                        )
                );


        Object rawMatches =
                hits.get(
                        "hits"
                );


        if (
                !(rawMatches instanceof List<?> list)
        ) {

            return new SearchPage(
                    total,
                    List.of()
            );

        }


        List<SearchMatch> matches =
                new ArrayList<>();


        for (
                Object raw :
                list
        ) {

            if (
                    !(raw instanceof Map hit)
            ) {
                continue;
            }


            Object idValue =
                    hit.get(
                            "_id"
                    );


            if (
                    idValue == null
            ) {
                continue;
            }


            UUID id;


            try {

                id =
                        UUID.fromString(
                                idValue.toString()
                        );

            } catch (
                    IllegalArgumentException exception
            ) {

                continue;

            }


            Object scoreValue =
                    hit.get(
                            "_score"
                    );


            double score =
                    scoreValue instanceof Number number
                            ? number.doubleValue()
                            : 0.0;


            matches.add(
                    new SearchMatch(
                            id,
                            score
                    )
            );

        }


        return new SearchPage(
                total,
                List.copyOf(
                        matches
                )
        );

    }


    @SuppressWarnings("rawtypes")
    private long parseTotal(
            Object value
    ) {

        if (
                value instanceof Number number
        ) {

            return number.longValue();

        }


        if (
                value instanceof Map map
        ) {

            Object nested =
                    map.get(
                            "value"
                    );


            if (
                    nested instanceof Number number
            ) {

                return number.longValue();

            }

        }


        return 0;

    }

}
