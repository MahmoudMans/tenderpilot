package com.tenderpilot.search.application;

import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import com.tenderpilot.procurement.application.OpportunityQueryService;

import com.tenderpilot.search.api.ProcurementSearchResponse;

import com.tenderpilot.search.domain.ProcurementSearchQuery;

import com.tenderpilot.search.infrastructure.OpenSearchQueryBuilder;
import com.tenderpilot.search.infrastructure.OpenSearchSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class ProcurementSearchService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ProcurementSearchService.class
            );


    private final OpenSearchGateway
            gateway;


    private final OpenSearchSettings
            settings;


    private final OpenSearchQueryBuilder
            queryBuilder;


    private final OpportunityQueryService
            opportunityQueryService;


    public ProcurementSearchService(

            OpenSearchGateway gateway,

            OpenSearchSettings settings,

            OpenSearchQueryBuilder queryBuilder,

            OpportunityQueryService opportunityQueryService

    ) {

        this.gateway =
                gateway;

        this.settings =
                settings;

        this.queryBuilder =
                queryBuilder;

        this.opportunityQueryService =
                opportunityQueryService;

    }


    public ProcurementSearchResponse search(
            ProcurementSearchQuery query
    ) {

        if (
                settings.enabled()
        ) {

            try {

                OpenSearchGateway.SearchPage page =
                        gateway.search(
                                query
                        );


                List<ProcurementSearchResponse.Hit> hits =
                        new ArrayList<>();


                for (
                        OpenSearchGateway.SearchMatch match :
                        page.matches()
                ) {

                    try {

                        hits.add(
                                new ProcurementSearchResponse
                                        .Hit(
                                                match.score(),
                                                opportunityQueryService
                                                        .summaryById(
                                                                match.tenderId()
                                                        )
                                        )
                        );

                    } catch (
                            ResourceNotFoundException staleProjection
                    ) {

                        log.warn(
                                "Ignoring stale OpenSearch document {}",
                                match.tenderId()
                        );

                    }

                }


                return new ProcurementSearchResponse(
                        "OPENSEARCH",
                        false,
                        page.total(),
                        List.copyOf(
                                hits
                        )
                );


            } catch (
                    RuntimeException exception
            ) {

                log.warn(
                        "OpenSearch query unavailable; using PostgreSQL fallback",
                        exception
                );

            }

        }


        return fallback(
                query
        );

    }


    private ProcurementSearchResponse fallback(
            ProcurementSearchQuery query
    ) {

        List<OpportunitySummaryResponse> candidates =
                opportunityQueryService
                        .list(
                                query.text(),
                                query.status(),
                                query.source(),
                                query.minimumQualityScore(),
                                query.qualityGrade()
                        );


        String country =
                clean(
                        query.countryCode()
                );


        String region =
                clean(
                        query.region()
                );


        int size =
                queryBuilder
                        .normalizedSize(
                                query.size()
                        );


        List<ProcurementSearchResponse.Hit> results =
                candidates
                        .stream()

                        .filter(
                                opportunity ->
                                        query.contractNature()
                                                ==
                                                null
                                        ||
                                        opportunity.contractNature()
                                                ==
                                                query.contractNature()
                        )

                        .filter(
                                opportunity ->
                                        country == null
                                        ||
                                        opportunity.countryCode()
                                                .equalsIgnoreCase(
                                                        country
                                                )
                        )

                        .filter(
                                opportunity ->
                                        region == null
                                        ||
                                        (
                                                opportunity.primaryRegionName()
                                                        !=
                                                        null
                                                &&
                                                opportunity
                                                        .primaryRegionName()
                                                        .toLowerCase(
                                                                Locale.ROOT
                                                        )
                                                        .contains(
                                                                region
                                                                        .toLowerCase(
                                                                                Locale.ROOT
                                                                        )
                                                        )
                                        )
                        )

                        .limit(
                                size
                        )

                        .map(
                                opportunity ->
                                        new ProcurementSearchResponse
                                                .Hit(
                                                        null,
                                                        opportunity
                                                )
                        )

                        .toList();


        return new ProcurementSearchResponse(
                "POSTGRESQL_FALLBACK",
                true,
                results.size(),
                results
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
                value.trim();


        return cleaned.isBlank()
                ? null
                : cleaned;

    }

}
