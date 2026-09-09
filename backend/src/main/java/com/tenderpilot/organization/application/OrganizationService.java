package com.tenderpilot.organization.application;

import com.tenderpilot.common.domain.ApiAuthenticationException;
import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.organization.api.OrganizationMemberResponse;
import com.tenderpilot.organization.api.OrganizationResponse;

import com.tenderpilot.organization.domain.OrganizationEntity;
import com.tenderpilot.organization.domain.OrganizationMembershipEntity;

import com.tenderpilot.organization.infrastructure.OrganizationMembershipRepository;
import com.tenderpilot.organization.infrastructure.OrganizationRepository;

import com.tenderpilot.tenant.CurrentPrincipal;
import com.tenderpilot.tenant.CurrentPrincipalService;
import com.tenderpilot.tenant.TenantAccessGuard;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final OrganizationRepository
            organizationRepository;


    private final OrganizationMembershipRepository
            membershipRepository;


    private final CurrentPrincipalService
            currentPrincipalService;


    private final TenantAccessGuard
            tenantAccessGuard;


    public OrganizationService(

            OrganizationRepository organizationRepository,

            OrganizationMembershipRepository membershipRepository,

            CurrentPrincipalService currentPrincipalService,

            TenantAccessGuard tenantAccessGuard

    ) {

        this.organizationRepository =
                organizationRepository;

        this.membershipRepository =
                membershipRepository;

        this.currentPrincipalService =
                currentPrincipalService;

        this.tenantAccessGuard =
                tenantAccessGuard;

    }


    @Transactional(
            readOnly = true
    )
    public OrganizationResponse current() {

        CurrentPrincipal principal =

                currentPrincipalService
                        .require();


        assertMembership(
                principal
        );


        return map(

                organizationRepository
                        .findById(
                                principal.organizationId()
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Organization was not found"
                                        )
                        )

        );

    }


    @Transactional(
            readOnly = true
    )
    public OrganizationResponse getForCurrentTenant(
            UUID organizationId
    ) {

        tenantAccessGuard
                .requireCurrentOrganization(
                        organizationId
                );


        return current();

    }


    @Transactional(
            readOnly = true
    )
    public List<OrganizationMemberResponse> currentMembers() {

        CurrentPrincipal principal =

                currentPrincipalService
                        .require();


        assertMembership(
                principal
        );


        return membershipRepository

                .findAllByOrganization_IdOrderByCreatedAtAsc(
                        principal.organizationId()
                )

                .stream()

                .map(
                        this::mapMember
                )

                .toList();

    }


    private void assertMembership(
            CurrentPrincipal principal
    ) {

        membershipRepository
                .findByUser_IdAndOrganization_Id(

                        principal.userId(),

                        principal.organizationId()

                )
                .orElseThrow(
                        () ->
                                new ApiAuthenticationException(
                                        "Organization membership is no longer active"
                                )
                );

    }


    private OrganizationResponse map(
            OrganizationEntity organization
    ) {

        return new OrganizationResponse(

                organization.getId(),

                organization.getName(),

                organization.getSlug(),

                organization.getCreatedAt()

        );

    }


    private OrganizationMemberResponse mapMember(
            OrganizationMembershipEntity membership
    ) {

        return new OrganizationMemberResponse(

                membership
                        .getUser()
                        .getId(),

                membership
                        .getUser()
                        .getFullName(),

                membership
                        .getUser()
                        .getEmail(),

                membership.getRole(),

                membership.getCreatedAt()

        );

    }

}
