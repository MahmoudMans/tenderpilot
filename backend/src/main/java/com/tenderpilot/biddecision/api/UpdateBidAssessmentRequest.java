package com.tenderpilot.biddecision.api;

public record UpdateBidAssessmentRequest(

        Integer strategicFit,

        Integer capabilityConfidence,

        Integer commercialAttractiveness,

        Integer deliveryFeasibility,

        Integer resourceCapacity,

        Integer riskConfidence,

        String rationale,

        String keyRisks,

        String assumptions

) {
}
