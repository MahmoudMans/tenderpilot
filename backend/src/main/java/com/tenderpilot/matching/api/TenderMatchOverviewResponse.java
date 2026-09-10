package com.tenderpilot.matching.api;

import java.time.Instant;

public record TenderMatchOverviewResponse(

        long totalMatches,

        long actionableMatches,

        long blockedMatches,

        long excellentMatches,

        long strongMatches,

        long goodMatches,

        double averageScore,

        Instant lastEvaluatedAt

) {
}
