package com.tenderpilot.procurement.quality.domain;

public enum DataQualityGrade {

    EXCELLENT,

    GOOD,

    FAIR,

    POOR,

    CRITICAL;


    public static DataQualityGrade fromScore(
            int score
    ) {

        if (score >= 90) {
            return EXCELLENT;
        }

        if (score >= 75) {
            return GOOD;
        }

        if (score >= 60) {
            return FAIR;
        }

        if (score >= 40) {
            return POOR;
        }

        return CRITICAL;
    }

}
