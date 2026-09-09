package com.tenderpilot.tenant;

import com.tenderpilot.common.domain.ApiAuthenticationException;
import com.tenderpilot.organization.domain.MembershipRole;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CurrentPrincipalService {

    public CurrentPrincipal require() {

        Authentication authentication =

                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (
                !(
                        authentication
                                instanceof
                                JwtAuthenticationToken jwtAuthentication
                )
                ||
                !authentication.isAuthenticated()
        ) {

            throw new ApiAuthenticationException(
                    "Authentication is required"
            );

        }


        String organizationId =
                jwtAuthentication
                        .getToken()
                        .getClaimAsString(
                                "org_id"
                        );


        String role =
                jwtAuthentication
                        .getToken()
                        .getClaimAsString(
                                "role"
                        );


        String email =
                jwtAuthentication
                        .getToken()
                        .getClaimAsString(
                                "email"
                        );


        try {

            return new CurrentPrincipal(

                    UUID.fromString(
                            jwtAuthentication
                                    .getToken()
                                    .getSubject()
                    ),

                    UUID.fromString(
                            organizationId
                    ),

                    MembershipRole
                            .valueOf(
                                    role
                            ),

                    email

            );


        } catch (
                RuntimeException exception
        ) {

            throw new ApiAuthenticationException(
                    "Access token does not contain a valid TenderPilot tenant context"
            );

        }

    }

}
