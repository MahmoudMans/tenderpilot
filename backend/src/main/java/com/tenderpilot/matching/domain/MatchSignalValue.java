package com.tenderpilot.matching.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class MatchSignalValue {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "signal_code",
            nullable = false,
            length = 100
    )
    private MatchSignalCode code;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "impact",
            nullable = false,
            length = 30
    )
    private MatchSignalImpact impact;


    @Column(
            name = "dimension_name",
            nullable = false,
            length = 80
    )
    private String dimension;


    @Column(
            name = "earned_score",
            nullable = false
    )
    private int earnedScore;


    @Column(
            name = "maximum_score",
            nullable = false
    )
    private int maximumScore;


    @Column(
            name = "message",
            nullable = false,
            length = 800
    )
    private String message;


    protected MatchSignalValue() {
    }


    public MatchSignalValue(
            MatchSignalCode code,
            MatchSignalImpact impact,
            String dimension,
            int earnedScore,
            int maximumScore,
            String message
    ) {

        this.code = code;
        this.impact = impact;
        this.dimension = dimension;
        this.earnedScore = earnedScore;
        this.maximumScore = maximumScore;
        this.message = message;
    }


    public MatchSignalCode getCode() {
        return code;
    }


    public MatchSignalImpact getImpact() {
        return impact;
    }


    public String getDimension() {
        return dimension;
    }


    public int getEarnedScore() {
        return earnedScore;
    }


    public int getMaximumScore() {
        return maximumScore;
    }


    public String getMessage() {
        return message;
    }

}
