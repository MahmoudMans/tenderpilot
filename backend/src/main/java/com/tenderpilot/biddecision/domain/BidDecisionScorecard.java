package com.tenderpilot.biddecision.domain;

public record BidDecisionScorecard(

        int strategicFit,

        int capabilityConfidence,

        int commercialAttractiveness,

        int deliveryFeasibility,

        int resourceCapacity,

        int riskConfidence

) {
}
