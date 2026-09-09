package com.tenderpilot.identity.api;

import com.tenderpilot.organization.domain.MembershipRole;

import java.util.UUID;

public record AuthUserResponse(

        UUID id,

        String fullName,

        String email,

        OrganizationSummaryResponse organization,

        MembershipRole role

) {
}
