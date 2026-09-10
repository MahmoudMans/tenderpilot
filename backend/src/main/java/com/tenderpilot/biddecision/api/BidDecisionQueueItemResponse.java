package com.tenderpilot.biddecision.api;

import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import java.time.Instant;
import java.util.UUID;

public record BidDecisionQueueItemResponse(

        UUID matchId,

        UUID tenderId,

        int matchScore,

        TenderMatchGrade matchGrade,

        boolean blocked,

        OpportunitySummaryResponse opportunity,

        DecisionSummary decision

) {

    public record DecisionSummary(

            UUID id,

            BidDecisionOutcome outcome,

            BidDecisionOutcome recommendedOutcome,

            int decisionScore,

            Instant updatedAt

    ) {
    }

}
