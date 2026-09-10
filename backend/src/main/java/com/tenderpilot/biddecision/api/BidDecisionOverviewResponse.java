package com.tenderpilot.biddecision.api;

import java.time.Instant;

public record BidDecisionOverviewResponse(

        long candidateCount,

        long totalDecisions,

        long pendingCount,

        long goCount,

        long holdCount,

        long noGoCount,

        double averageDecisionScore,

        Instant lastUpdatedAt

) {
}
