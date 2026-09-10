package com.tenderpilot.biddecision.api;

import com.tenderpilot.biddecision.domain.BidDecisionEventType;
import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BidDecisionWorkspaceResponse(

        UUID tenderId,

        int matchScore,

        TenderMatchGrade matchGrade,

        boolean matchBlocked,

        OpportunitySummaryResponse opportunity,

        DecisionResponse decision,

        List<HistoryResponse> history

) {

    public record DecisionResponse(

            UUID id,

            BidDecisionOutcome outcome,

            BidDecisionOutcome recommendedOutcome,

            int decisionScore,

            int matchScoreSnapshot,

            TenderMatchGrade matchGradeSnapshot,

            int strategicFit,

            int capabilityConfidence,

            int commercialAttractiveness,

            int deliveryFeasibility,

            int resourceCapacity,

            int riskConfidence,

            String recommendationReason,

            String rationale,

            String keyRisks,

            String assumptions,

            String decidedBy,

            Instant decidedAt,

            Instant createdAt,

            Instant updatedAt

    ) {
    }


    public record HistoryResponse(

            UUID id,

            BidDecisionEventType eventType,

            BidDecisionOutcome previousOutcome,

            BidDecisionOutcome newOutcome,

            int decisionScore,

            String actor,

            String note,

            Instant createdAt

    ) {
    }

}
