package com.tenderpilot.rag.api;

import com.tenderpilot.matching.security.CurrentOrganizationResolver;

import com.tenderpilot.rag.application.BidRagService;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/rag"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class BidRagController {

    private final BidRagService
            ragService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public BidRagController(
            BidRagService ragService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.ragService =
                ragService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping(
            "/status"
    )
    public RagStatusResponse status() {

        return ragService
                .status();
    }


    @GetMapping(
            "/bid-workspaces/{tenderId}/index"
    )
    public BidRagIndexStatusResponse indexStatus(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return ragService
                .indexStatus(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }


    @PostMapping(
            "/bid-workspaces/{tenderId}/rebuild"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST')"
    )
    public BidRagRebuildResponse rebuild(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return ragService
                .rebuild(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }


    @PostMapping(
            "/bid-workspaces/{tenderId}/ask"
    )
    public BidRagAnswerResponse ask(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            AskBidDocumentsRequest request
    ) {

        return ragService
                .ask(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        request,
                        authentication.getName()
                );
    }


    @GetMapping(
            "/bid-workspaces/{tenderId}/history"
    )
    public List<BidRagAnswerResponse> history(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return ragService
                .history(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }

}
