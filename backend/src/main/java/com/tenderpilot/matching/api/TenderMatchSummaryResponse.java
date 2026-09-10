package com.tenderpilot.matching.api;

import com.tenderpilot.matching.domain.MatchSignalCode;
import com.tenderpilot.matching.domain.MatchSignalImpact;
import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

public record TenderMatchSummaryResponse(

        UUID id,

        int score,

        TenderMatchGrade grade,

        boolean blocked,

        int cpvScore,

        int geographyScore,

        int valueScore,

        int buyerScore,

        int capabilityScore,

        int timingScore,

        int qualityScore,

        Instant evaluatedAt,

        OpportunitySummaryResponse opportunity,

        List<SignalResponse> signals

) {

    public record SignalResponse(
            MatchSignalCode code,
            MatchSignalImpact impact,
            String dimension,
            int earnedScore,
            int maximumScore,
            String message
    ) {
    }

}
