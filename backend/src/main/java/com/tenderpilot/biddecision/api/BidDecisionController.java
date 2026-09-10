package com.tenderpilot.biddecision.api;

import com.tenderpilot.biddecision.application.BidDecisionService;

import com.tenderpilot.matching.security.CurrentOrganizationResolver;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/bid-decisions"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class BidDecisionController {

    private final BidDecisionService
            bidDecisionService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public BidDecisionController(
            BidDecisionService bidDecisionService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.bidDecisionService =
                bidDecisionService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping(
            "/queue"
    )
    public List<BidDecisionQueueItemResponse> queue(
            Authentication authentication,

            @RequestParam(
                    required = false
            )
            Integer minMatchScore,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean includeBlocked
    ) {

        return bidDecisionService
                .queue(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        minMatchScore,
                        includeBlocked
                );
    }


    @GetMapping(
            "/overview"
    )
    public BidDecisionOverviewResponse overview(
            Authentication authentication
    ) {

        return bidDecisionService
                .overview(
                        organizationResolver
                                .resolve(
                                        authentication
                                )
                );
    }


    @GetMapping(
            "/{tenderId}"
    )
    public BidDecisionWorkspaceResponse workspace(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return bidDecisionService
                .workspace(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }


    @PutMapping(
            "/{tenderId}/assessment"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST')"
    )
    public BidDecisionWorkspaceResponse assess(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            UpdateBidAssessmentRequest request
    ) {

        return bidDecisionService
                .assess(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        request,
                        authentication.getName()
                );
    }


    @PostMapping(
            "/{tenderId}/decision"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public BidDecisionWorkspaceResponse decide(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            SetBidDecisionRequest request
    ) {

        return bidDecisionService
                .decide(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        request,
                        authentication.getName()
                );
    }


    @PostMapping(
            "/{tenderId}/reopen"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public BidDecisionWorkspaceResponse reopen(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return bidDecisionService
                .reopen(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        authentication.getName()
                );
    }

}
