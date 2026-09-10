package com.tenderpilot.bidworkspace.api;

import com.tenderpilot.bidworkspace.application.BidWorkspaceService;

import com.tenderpilot.matching.security.CurrentOrganizationResolver;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/bid-workspaces"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class BidWorkspaceController {

    private final BidWorkspaceService
            workspaceService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public BidWorkspaceController(
            BidWorkspaceService workspaceService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.workspaceService =
                workspaceService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping(
            "/{tenderId}"
    )
    public BidWorkspaceResponse workspace(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return workspaceService
                .workspace(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }


    @PostMapping(
            "/{tenderId}/documents"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST','CONTRIBUTOR')"
    )
    public BidWorkspaceResponse uploadDocument(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            CreateBidDocumentRequest request
    ) {

        return workspaceService
                .uploadDocument(
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
            "/{tenderId}/documents/{documentId}/content"
    )
    public BidDocumentContentResponse documentContent(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @PathVariable
            UUID documentId
    ) {

        return workspaceService
                .documentContent(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        documentId
                );
    }


    @DeleteMapping(
            "/{tenderId}/documents/{documentId}"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public BidWorkspaceResponse deleteDocument(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @PathVariable
            UUID documentId
    ) {

        return workspaceService
                .deleteDocument(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        documentId
                );
    }


    @PostMapping(
            "/{tenderId}/requirements"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST','CONTRIBUTOR')"
    )
    public BidWorkspaceResponse createRequirement(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            CreateBidRequirementRequest request
    ) {

        return workspaceService
                .createRequirement(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        request,
                        authentication.getName()
                );
    }


    @PutMapping(
            "/{tenderId}/requirements/{requirementId}"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST','CONTRIBUTOR')"
    )
    public BidWorkspaceResponse updateRequirement(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @PathVariable
            UUID requirementId,
            @RequestBody
            UpdateBidRequirementRequest request
    ) {

        return workspaceService
                .updateRequirement(
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


    @DeleteMapping(
            "/{tenderId}/requirements/{requirementId}"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public BidWorkspaceResponse deleteRequirement(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @PathVariable
            UUID requirementId
    ) {

        return workspaceService
                .deleteRequirement(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        requirementId
                );
    }

}
