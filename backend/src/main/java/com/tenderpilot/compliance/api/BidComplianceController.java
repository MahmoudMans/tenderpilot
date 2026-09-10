package com.tenderpilot.compliance.api;

import com.tenderpilot.compliance.application.BidComplianceService;

import com.tenderpilot.matching.security.CurrentOrganizationResolver;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/compliance"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class BidComplianceController {

    private final BidComplianceService
            complianceService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public BidComplianceController(
            BidComplianceService complianceService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.complianceService =
                complianceService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping(
            "/bid-workspaces/{tenderId}"
    )
    public BidComplianceMatrixResponse matrix(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return complianceService
                .matrix(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }


    @PutMapping(
            "/bid-workspaces/{tenderId}/requirements/{requirementId}"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST')"
    )
    public BidComplianceMatrixResponse review(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @PathVariable
            UUID requirementId,
            @RequestBody
            UpdateComplianceReviewRequest request
    ) {

        return complianceService
                .review(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        requirementId,
                        request,
                        authentication.getName()
                );
    }

}
