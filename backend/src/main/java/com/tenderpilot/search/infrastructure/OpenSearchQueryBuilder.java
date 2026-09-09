package com.tenderpilot.search.infrastructure;

import com.tenderpilot.search.domain.ProcurementSearchQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class OpenSearchQueryBuilder {

    private final OpenSearchSettings
            settings;


    public OpenSearchQueryBuilder(
            OpenSearchSettings settings
    ) {

        this.settings =
                settings;

    }


    public Map<String, Object> build(
            ProcurementSearchQuery query
    ) {

        int size =
                normalizedSize(
                        query.size()
                );


        List<Object> must =
                new ArrayList<>();


        List<Object> filters =
                new ArrayList<>();


        String text =
                clean(
                        query.text()
                );


        if (
                text == null
        ) {

            must.add(
                    Map.of(
                            "match_all",
                            Map.of()
                    )
            );

        } else {

            Map<String, Object> multiMatch =
                    new LinkedHashMap<>();

            multiMatch.put(
                    "query",
                    text
            );

            multiMatch.put(
                    "fields",
                    List.of(
                            "title^5",
                            "buyerName^4",
                            "cpvCodes^4",
                            "cpvLabels^3",
                            "description^2",
                            "supplierNames^2",
                            "lotTitles"
                    )
            );

            multiMatch.put(
                    "fuzziness",
                    "AUTO"
            );


            must.add(
                    Map.of(
                            "multi_match",
                            multiMatch
                    )
            );

        }


        if (
                query.status() != null
        ) {

            filters.add(
                    term(
                            "status",
                            query.status()
                                    .name()
                    )
            );

        }


        if (
                query.source() != null
        ) {

            filters.add(
                    term(
                            "sources",
                            query.source()
                                    .name()
                    )
            );

        }


        if (
                query.contractNature()
                        !=
                        null
        ) {

            filters.add(
                    term(
                            "contractNature",
                            query
                                    .contractNature()
                                    .name()
                    )
            );

        }


        String countryCode =
                clean(
                        query.countryCode()
                );


        if (
                countryCode != null
        ) {

            filters.add(
                    term(
                            "countryCode",
                            countryCode
                                    .toUpperCase(
                                            Locale.ROOT
                                    )
                    )
            );

        }


        String region =
                clean(
                        query.region()
                );


        if (
                region != null
        ) {

            filters.add(
                    term(
                            "regionName",
                            region
                    )
            );

        }


        if (
                query.minimumQualityScore()
                        !=
                        null
        ) {

            int quality =
                    Math.max(
                            0,
                            Math.min(
                                    100,
                                    query.minimumQualityScore()
                            )
                    );


            filters.add(
                    Map.of(
                            "range",
                            Map.of(
                                    "qualityScore",
                                    Map.of(
                                            "gte",
                                            quality
                                    )
                            )
                    )
            );

        }


        if (
                query.qualityGrade()
                        !=
                        null
        ) {

            filters.add(
                    term(
                            "qualityGrade",
                            query
                                    .qualityGrade()
                                    .name()
                    )
            );

        }


        Map<String, Object> bool =
                new LinkedHashMap<>();

        bool.put(
                "must",
                must
        );

        if (
                !filters.isEmpty()
        ) {

            bool.put(
                    "filter",
                    filters
            );

        }


        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "size",
                size
        );

        body.put(
                "track_total_hits",
                true
        );

        body.put(
                "_source",
                false
        );

        body.put(
                "query",
                Map.of(
                        "bool",
                        bool
                )
        );


        List<Object> sort =
                new ArrayList<>();


        if (
                text != null
        ) {

            sort.add(
                    Map.of(
                            "_score",
                            Map.of(
                                    "order",
                                    "desc"
                            )
                    )
            );

        }


        sort.add(
                Map.of(
                        "publicationDate",
                        Map.of(
                                "order",
                                "desc",
                                "unmapped_type",
                                "date"
                        )
                )
        );


        body.put(
                "sort",
                sort
        );


        return body;

    }


    public int normalizedSize(
            Integer requested
    ) {

        if (
                requested == null
        ) {

            return Math.min(
                    settings.defaultSize(),
                    settings.maxSize()
            );

        }


        return Math.max(
                1,
                Math.min(
                        settings.maxSize(),
                        requested
                )
        );

    }


    private Map<String, Object> term(
            String field,
            Object value
    ) {

        return Map.of(
                "term",
                Map.of(
                        field,
                        value
                )
        );

    }


    private String clean(
            String value
    ) {

        if (
                value == null
        ) {
            return null;
        }


        String cleaned =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return cleaned.isBlank()
                ? null
                : cleaned;

    }

}
