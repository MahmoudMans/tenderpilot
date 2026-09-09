package com.tenderpilot.organization.api;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(

        UUID id,

        String name,

        String slug,

        Instant createdAt

) {
}
