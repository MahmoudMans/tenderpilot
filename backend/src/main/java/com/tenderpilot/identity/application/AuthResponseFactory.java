package com.tenderpilot.identity.application;

import com.tenderpilot.identity.api.AuthSessionResponse;
import com.tenderpilot.identity.api.AuthUserResponse;
import com.tenderpilot.identity.api.OrganizationSummaryResponse;
import com.tenderpilot.identity.domain.UserEntity;
import com.tenderpilot.organization.domain.MembershipRole;
import com.tenderpilot.organization.domain.OrganizationEntity;

import org.springframework.stereotype.Component;

@Component
public class AuthResponseFactory {

    public AuthUserResponse user(

            UserEntity user,

            OrganizationEntity organization,

            MembershipRole role

    ) {

        return new AuthUserResponse(

                user.getId(),

                user.getFullName(),

                user.getEmail(),

                new OrganizationSummaryResponse(

                        organization.getId(),

                        organization.getName(),

                        organization.getSlug()

                ),

                role

        );

    }


    public AuthSessionResponse session(

            IssuedAccessToken accessToken,

            UserEntity user,

            OrganizationEntity organization,

            MembershipRole role

    ) {

        return new AuthSessionResponse(

                accessToken.value(),

                accessToken.expiresInSeconds(),

                user(
                        user,
                        organization,
                        role
                )

        );

    }

}
