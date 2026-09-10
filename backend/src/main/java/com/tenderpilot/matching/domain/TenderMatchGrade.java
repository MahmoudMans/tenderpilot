package com.tenderpilot.matching.domain;

public enum TenderMatchGrade {

    BLOCKED,

    EXCELLENT,

    STRONG,

    GOOD,

    WEAK,

    POOR;


    public static TenderMatchGrade fromScore(
            int score
    ) {

        if (score >= 85) {
            return EXCELLENT;
        }

        if (score >= 70) {
            return STRONG;
        }

        if (score >= 55) {
            return GOOD;
        }

        if (score >= 40) {
            return WEAK;
        }

        return POOR;
    }

}
