package com.tenderpilot.bidplanning.api;

import com.tenderpilot.bidplanning.application.BidPlanningService;

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
        "/api/v1/bid-planning"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class BidPlanningController {

    private final BidPlanningService
            planningService;


    private final CurrentOrganizationResolver
            organizationResolver;


    public BidPlanningController(
            BidPlanningService planningService,
            CurrentOrganizationResolver organizationResolver
    ) {

        this.planningService =
                planningService;

        this.organizationResolver =
                organizationResolver;
    }


    @GetMapping(
            "/{tenderId}"
    )
    public BidPlanningWorkspaceResponse workspace(
            Authentication authentication,
            @PathVariable
            UUID tenderId
    ) {

        return planningService
                .workspace(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId
                );
    }


    @PostMapping(
            "/{tenderId}/tasks"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST','CONTRIBUTOR')"
    )
    public BidPlanningWorkspaceResponse createTask(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            CreateBidTaskRequest request
    ) {

        return planningService
                .createTask(
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
            "/{tenderId}/tasks/{taskId}"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST','CONTRIBUTOR')"
    )
    public BidPlanningWorkspaceResponse updateTask(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @PathVariable
            UUID taskId,
            @RequestBody
            UpdateBidTaskRequest request
    ) {

        return planningService
                .updateTask(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        taskId,
                        request,
                        authentication.getName()
                );
    }


    @DeleteMapping(
            "/{tenderId}/tasks/{taskId}"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public BidPlanningWorkspaceResponse deleteTask(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @PathVariable
            UUID taskId
    ) {

        return planningService
                .deleteTask(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        taskId,
                        authentication.getName()
                );
    }


    @PutMapping(
            "/{tenderId}/submission-control"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER','ANALYST')"
    )
    public BidPlanningWorkspaceResponse updateSubmissionControl(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            UpdateSubmissionControlRequest request
    ) {

        return planningService
                .updateSubmissionControl(
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
            "/{tenderId}/submission-control/authorization"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public BidPlanningWorkspaceResponse authorizeSubmission(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            SubmissionAuthorizationRequest request
    ) {

        return planningService
                .authorizeSubmission(
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
            "/{tenderId}/submission-control/mark-submitted"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public BidPlanningWorkspaceResponse markSubmitted(
            Authentication authentication,
            @PathVariable
            UUID tenderId,
            @RequestBody
            MarkBidSubmittedRequest request
    ) {

        return planningService
                .markSubmitted(
                        organizationResolver
                                .resolve(
                                        authentication
                                ),
                        tenderId,
                        request,
                        authentication.getName()
                );
    }

}
