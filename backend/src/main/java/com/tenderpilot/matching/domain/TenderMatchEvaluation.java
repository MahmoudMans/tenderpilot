package com.tenderpilot.matching.domain;

import java.util.List;

public record TenderMatchEvaluation(

        int totalScore,

        TenderMatchGrade grade,

        boolean blocked,

        int cpvScore,

        int geographyScore,

        int valueScore,

        int buyerScore,

        int capabilityScore,

        int timingScore,

        int qualityScore,

        List<MatchSignalValue> signals

) {
}
