package com.tenderpilot.search.api;

import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import com.tenderpilot.procurement.quality.domain.DataQualityGrade;

import com.tenderpilot.search.application.ProcurementSearchService;
import com.tenderpilot.search.application.SearchProjectionService;
import com.tenderpilot.search.application.SearchReindexResult;
import com.tenderpilot.search.application.SearchStatusService;

import com.tenderpilot.search.domain.ProcurementSearchQuery;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/search"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class ProcurementSearchController {

    private final ProcurementSearchService
            searchService;


    private final SearchProjectionService
            projectionService;


    private final SearchStatusService
            statusService;


    public ProcurementSearchController(

            ProcurementSearchService searchService,

            SearchProjectionService projectionService,

            SearchStatusService statusService

    ) {

        this.searchService =
                searchService;

        this.projectionService =
                projectionService;

        this.statusService =
                statusService;

    }


    @GetMapping(
            "/opportunities"
    )
    public ProcurementSearchResponse search(

            @RequestParam(
                    required = false
            )
            String q,


            @RequestParam(
                    required = false
            )
            ProcurementStatus status,


            @RequestParam(
                    required = false
            )
            ProcurementSource source,


            @RequestParam(
                    required = false
            )
            ContractNature contractNature,


            @RequestParam(
                    required = false
            )
            String countryCode,


            @RequestParam(
                    required = false
            )
            String region,


            @RequestParam(
                    required = false
            )
            Integer minQualityScore,


            @RequestParam(
                    required = false
            )
            DataQualityGrade qualityGrade,


            @RequestParam(
                    required = false
            )
            Integer size

    ) {

        return searchService
                .search(
                        new ProcurementSearchQuery(
                                q,
                                status,
                                source,
                                contractNature,
                                countryCode,
                                region,
                                minQualityScore,
                                qualityGrade,
                                size
                        )
                );

    }


    @GetMapping(
            "/status"
    )
    public SearchProjectionStatusResponse status() {

        return statusService
                .status();

    }


    @PostMapping(
            "/reindex"
    )
    @PreAuthorize(
            "hasRole('OWNER')"
    )
    public SearchReindexResponse reindex() {

        SearchReindexResult result =
                projectionService
                        .rebuild();


        return new SearchReindexResponse(
                result.discoveredCount(),
                result.indexedCount(),
                result.failedCount(),
                result.lastError()
        );

    }

}
