package com.tenderpilot.tenant;

import com.tenderpilot.organization.domain.MembershipRole;

import java.util.UUID;

public record CurrentPrincipal(

        UUID userId,

        UUID organizationId,

        MembershipRole role,

        String email

) {
}
