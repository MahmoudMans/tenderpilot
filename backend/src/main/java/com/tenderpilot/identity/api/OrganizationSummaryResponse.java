package com.tenderpilot.identity.api;

import java.util.UUID;

public record OrganizationSummaryResponse(

        UUID id,

        String name,

        String slug

) {
}
