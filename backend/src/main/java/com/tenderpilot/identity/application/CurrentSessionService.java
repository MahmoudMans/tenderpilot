package com.tenderpilot.identity.application;

import com.tenderpilot.common.domain.ApiAuthenticationException;

import com.tenderpilot.identity.api.AuthUserResponse;

import com.tenderpilot.identity.domain.UserEntity;
import com.tenderpilot.identity.infrastructure.UserRepository;

import com.tenderpilot.organization.domain.OrganizationEntity;
import com.tenderpilot.organization.domain.OrganizationMembershipEntity;

import com.tenderpilot.organization.infrastructure.OrganizationMembershipRepository;
import com.tenderpilot.organization.infrastructure.OrganizationRepository;

import com.tenderpilot.tenant.CurrentPrincipal;
import com.tenderpilot.tenant.CurrentPrincipalService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentSessionService {

    private final CurrentPrincipalService
            currentPrincipalService;


    private final UserRepository
            userRepository;


    private final OrganizationRepository
            organizationRepository;


    private final OrganizationMembershipRepository
            membershipRepository;


    private final AuthResponseFactory
            authResponseFactory;


    public CurrentSessionService(

            CurrentPrincipalService currentPrincipalService,

            UserRepository userRepository,

            OrganizationRepository organizationRepository,

            OrganizationMembershipRepository membershipRepository,

            AuthResponseFactory authResponseFactory

    ) {

        this.currentPrincipalService =
                currentPrincipalService;

        this.userRepository =
                userRepository;

        this.organizationRepository =
                organizationRepository;

        this.membershipRepository =
                membershipRepository;

        this.authResponseFactory =
                authResponseFactory;

    }


    @Transactional(
            readOnly = true
    )
    public AuthUserResponse currentUser() {

        CurrentPrincipal principal =

                currentPrincipalService
                        .require();


        UserEntity user =

                userRepository
                        .findById(
                                principal.userId()
                        )
                        .orElseThrow(
                                () ->
                                        new ApiAuthenticationException(
                                                "Authenticated user no longer exists"
                                        )
                        );


        OrganizationEntity organization =

                organizationRepository
                        .findById(
                                principal.organizationId()
                        )
                        .orElseThrow(
                                () ->
                                        new ApiAuthenticationException(
                                                "Authenticated organization no longer exists"
                                        )
                        );


        OrganizationMembershipEntity membership =

                membershipRepository
                        .findByUser_IdAndOrganization_Id(

                                user.getId(),

                                organization.getId()

                        )
                        .orElseThrow(
                                () ->
                                        new ApiAuthenticationException(
                                                "Authenticated organization membership no longer exists"
                                        )
                        );


        return authResponseFactory
                .user(

                        user,

                        organization,

                        membership.getRole()

                );

    }

}
