package com.tenderpilot.organization.api;

import com.tenderpilot.organization.domain.MembershipRole;

import java.time.Instant;
import java.util.UUID;

public record OrganizationMemberResponse(

        UUID userId,

        String fullName,

        String email,

        MembershipRole role,

        Instant joinedAt

) {
}
