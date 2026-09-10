package com.tenderpilot.biddecision.domain;

public record BidAssessmentEvaluation(

        int score,

        BidDecisionOutcome recommendation,

        String recommendationReason

) {
}
