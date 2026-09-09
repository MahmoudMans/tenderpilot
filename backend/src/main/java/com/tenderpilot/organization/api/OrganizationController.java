package com.tenderpilot.organization.api;

import com.tenderpilot.organization.application.OrganizationService;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/organizations"
)
public class OrganizationController {

    private final OrganizationService
            organizationService;


    public OrganizationController(
            OrganizationService organizationService
    ) {

        this.organizationService =
                organizationService;

    }


    @GetMapping(
            "/current"
    )
    public OrganizationResponse current() {

        return organizationService
                .current();

    }


    @GetMapping(
            "/{organizationId}"
    )
    public OrganizationResponse byId(

            @PathVariable
            UUID organizationId

    ) {

        return organizationService
                .getForCurrentTenant(
                        organizationId
                );

    }


    @GetMapping(
            "/current/members"
    )
    @PreAuthorize(
            "hasAnyRole('OWNER','BID_MANAGER')"
    )
    public List<OrganizationMemberResponse> members() {

        return organizationService
                .currentMembers();

    }

}
