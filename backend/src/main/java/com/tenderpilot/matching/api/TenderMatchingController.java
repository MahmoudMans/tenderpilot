package com.tenderpilot.matching.api;

import com.tenderpilot.matching.application.TenderMatchingService;
import com.tenderpilot.matching.domain.TenderMatchGrade;
import com.tenderpilot.matching.security.CurrentOrganizationResolver;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/matches"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class TenderMatchingController {

    private final TenderMatchingService
            matchingService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public TenderMatchingController(
            TenderMatchingService matchingService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.matchingService =
                matchingService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping
    public List<TenderMatchSummaryResponse> list(
            Authentication authentication,

            @RequestParam(
                    required = false
            )
            Integer minScore,

            @RequestParam(
                    required = false
            )
            TenderMatchGrade grade,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean includeBlocked
    ) {

        UUID organizationId =
                organizationResolver
                        .resolve(
                                authentication
                        );


        return matchingService
                .list(
                        organizationId,
                        minScore,
                        grade,
                        includeBlocked
                );
    }


    @GetMapping(
            "/overview"
    )
    public TenderMatchOverviewResponse overview(
            Authentication authentication
    ) {

        return matchingService
                .overview(
                        organizationResolver
                                .resolve(
                                        authentication
                                )
                );
    }


    @PostMapping(
            "/recalculate"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST')"
    )
    public TenderMatchRecalculationResponse recalculate(
            Authentication authentication
    ) {

        return matchingService
                .recalculate(
                        organizationResolver
                                .resolve(
                                        authentication
                                )
                );
    }

}
