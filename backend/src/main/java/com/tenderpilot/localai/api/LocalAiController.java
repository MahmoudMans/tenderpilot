package com.tenderpilot.localai.api;

import com.tenderpilot.localai.application.BidDocumentAiAnalysisService;
import com.tenderpilot.localai.application.LocalAiStatusService;

import com.tenderpilot.matching.security.CurrentOrganizationResolver;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/local-ai"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class LocalAiController {

    private final LocalAiStatusService
            statusService;


    private final BidDocumentAiAnalysisService
            analysisService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public LocalAiController(
            LocalAiStatusService statusService,
            BidDocumentAiAnalysisService analysisService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.statusService =
                statusService;

        this.analysisService =
                analysisService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping(
            "/status"
    )
    public LocalAiStatusResponse status() {

        return statusService
                .status();
    }


    @GetMapping(
            "/bid-workspaces/{tenderId}/analyses"
    )
    public List<BidDocumentAiAnalysisResponse> analyses(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return analysisService
                .analyses(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }


    @PostMapping(
            "/bid-workspaces/{tenderId}/documents/{documentId}/analyze"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST')"
    )
    public BidDocumentAiAnalysisResponse analyze(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @PathVariable
            UUID documentId
    ) {

        return analysisService
                .analyze(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        documentId
                );
    }


    @PostMapping(
            "/bid-workspaces/{tenderId}/analyze-all"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST')"
    )
    public List<BidDocumentAiAnalysisResponse> analyzeAll(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return analysisService
                .analyzeAll(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }

}
