package com.tenderpilot.matching;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.matching.application.TenderMatchScorer;

import com.tenderpilot.matching.domain.CompanyMatchProfile;
import com.tenderpilot.matching.domain.MatchSignalCode;
import com.tenderpilot.matching.domain.TenderMatchCandidate;
import com.tenderpilot.matching.domain.TenderMatchEvaluation;
import com.tenderpilot.matching.domain.TenderMatchGrade;

import java.math.BigDecimal;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TenderMatchScorerTest {

    private final TenderMatchScorer scorer =
            new TenderMatchScorer();


    private final Instant now =
            Instant.parse(
                    "2026-09-10T08:00:00Z"
            );


    @Test
    void perfectFitProducesExcellentHundredScore() {

        TenderMatchEvaluation result =
                scorer.evaluate(
                        profile(),
                        candidate(),
                        now
                );


        assertThat(
                result.totalScore()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.grade()
        )
                .isEqualTo(
                        TenderMatchGrade.EXCELLENT
                );


        assertThat(
                result.blocked()
        )
                .isFalse();
    }


    @Test
    void excludedKeywordBlocksOpportunity() {

        CompanyMatchProfile profile =
                new CompanyMatchProfile(
                        profile().organizationId(),
                        profile().capabilities(),
                        profile().operatingRegions(),
                        profile().targetCpvs(),
                        profile().preferredBuyerTypes(),
                        List.of(
                                "asbestos"
                        ),
                        profile().preferredMinimumValue(),
                        profile().preferredMaximumValue(),
                        profile().preferredCurrency(),
                        profile().minimumNoticeDays()
                );


        TenderMatchCandidate candidate =
                new TenderMatchCandidate(
                        candidate().tenderId(),
                        "Asbestos removal and renovation",
                        candidate().description(),
                        candidate().buyerCategory(),
                        candidate().countryCode(),
                        candidate().regionCode(),
                        candidate().regionName(),
                        candidate().estimatedMinimumValue(),
                        candidate().estimatedMaximumValue(),
                        candidate().currency(),
                        candidate().submissionDeadline(),
                        candidate().cpvCodes(),
                        candidate().cpvLabels(),
                        candidate().dataQualityScore()
                );


        TenderMatchEvaluation result =
                scorer.evaluate(
                        profile,
                        candidate,
                        now
                );


        assertThat(
                result.blocked()
        )
                .isTrue();


        assertThat(
                result.grade()
        )
                .isEqualTo(
                        TenderMatchGrade.BLOCKED
                );


        assertThat(
                result.signals()
        )
                .extracting(
                        signal ->
                                signal.getCode()
                )
                .contains(
                        MatchSignalCode.EXCLUDED_KEYWORD
                );
    }


    @Test
    void cpvHierarchyProducesPartialCredit() {

        CompanyMatchProfile profile =
                new CompanyMatchProfile(
                        profile().organizationId(),
                        profile().capabilities(),
                        profile().operatingRegions(),
                        List.of(
                                "45450000"
                        ),
                        profile().preferredBuyerTypes(),
                        profile().excludedKeywords(),
                        profile().preferredMinimumValue(),
                        profile().preferredMaximumValue(),
                        profile().preferredCurrency(),
                        profile().minimumNoticeDays()
                );


        TenderMatchEvaluation result =
                scorer.evaluate(
                        profile,
                        candidate(),
                        now
                );


        assertThat(
                result.cpvScore()
        )
                .isBetween(
                        1,
                        24
                );
    }


    @Test
    void geographyMismatchLosesGeographicFit() {

        TenderMatchCandidate candidate =
                new TenderMatchCandidate(
                        candidate().tenderId(),
                        candidate().title(),
                        candidate().description(),
                        candidate().buyerCategory(),
                        "FR",
                        "PACA",
                        "Provence-Alpes-Côte d'Azur",
                        candidate().estimatedMinimumValue(),
                        candidate().estimatedMaximumValue(),
                        candidate().currency(),
                        candidate().submissionDeadline(),
                        candidate().cpvCodes(),
                        candidate().cpvLabels(),
                        candidate().dataQualityScore()
                );


        TenderMatchEvaluation result =
                scorer.evaluate(
                        profile(),
                        candidate,
                        now
                );


        assertThat(
                result.geographyScore()
        )
                .isLessThan(
                        15
                );
    }


    @Test
    void contractOutsidePreferredRangeGetsZeroValueScore() {

        TenderMatchCandidate candidate =
                new TenderMatchCandidate(
                        candidate().tenderId(),
                        candidate().title(),
                        candidate().description(),
                        candidate().buyerCategory(),
                        candidate().countryCode(),
                        candidate().regionCode(),
                        candidate().regionName(),
                        new BigDecimal(
                                "9000000"
                        ),
                        new BigDecimal(
                                "10000000"
                        ),
                        "EUR",
                        candidate().submissionDeadline(),
                        candidate().cpvCodes(),
                        candidate().cpvLabels(),
                        candidate().dataQualityScore()
                );


        assertThat(
                scorer
                        .evaluate(
                                profile(),
                                candidate,
                                now
                        )
                        .valueScore()
        )
                .isZero();
    }


    @Test
    void shortNoticeReceivesPartialTimingCredit() {

        TenderMatchCandidate candidate =
                new TenderMatchCandidate(
                        candidate().tenderId(),
                        candidate().title(),
                        candidate().description(),
                        candidate().buyerCategory(),
                        candidate().countryCode(),
                        candidate().regionCode(),
                        candidate().regionName(),
                        candidate().estimatedMinimumValue(),
                        candidate().estimatedMaximumValue(),
                        candidate().currency(),
                        now.plusSeconds(
                                3 * 24 * 3600L
                        ),
                        candidate().cpvCodes(),
                        candidate().cpvLabels(),
                        candidate().dataQualityScore()
                );


        assertThat(
                scorer
                        .evaluate(
                                profile(),
                                candidate,
                                now
                        )
                        .timingScore()
        )
                .isEqualTo(
                        5
                );
    }


    @Test
    void qualityScoreContributesTenPercent() {

        TenderMatchCandidate candidate =
                new TenderMatchCandidate(
                        candidate().tenderId(),
                        candidate().title(),
                        candidate().description(),
                        candidate().buyerCategory(),
                        candidate().countryCode(),
                        candidate().regionCode(),
                        candidate().regionName(),
                        candidate().estimatedMinimumValue(),
                        candidate().estimatedMaximumValue(),
                        candidate().currency(),
                        candidate().submissionDeadline(),
                        candidate().cpvCodes(),
                        candidate().cpvLabels(),
                        50
                );


        assertThat(
                scorer
                        .evaluate(
                                profile(),
                                candidate,
                                now
                        )
                        .qualityScore()
        )
                .isEqualTo(
                        5
                );
    }


    private CompanyMatchProfile profile() {

        return new CompanyMatchProfile(
                UUID.fromString(
                        "10000000-0000-0000-0000-000000000001"
                ),

                List.of(
                        "energy renovation"
                ),

                List.of(
                        new CompanyMatchProfile
                                .OperatingRegion(
                                        "FR",
                                        "IDF",
                                        "Île-de-France"
                                )
                ),

                List.of(
                        "45453000"
                ),

                List.of(
                        "LOCAL_AUTHORITY"
                ),

                List.of(),

                new BigDecimal(
                        "500000"
                ),

                new BigDecimal(
                        "3000000"
                ),

                "EUR",

                14
        );
    }


    private TenderMatchCandidate candidate() {

        return new TenderMatchCandidate(
                UUID.fromString(
                        "20000000-0000-0000-0000-000000000001"
                ),

                "Energy renovation of a public school",

                "Major energy renovation and refurbishment programme.",

                "LOCAL_AUTHORITY",

                "FR",

                "IDF",

                "Île-de-France",

                new BigDecimal(
                        "1000000"
                ),

                new BigDecimal(
                        "2000000"
                ),

                "EUR",

                now.plusSeconds(
                        30 * 24 * 3600L
                ),

                List.of(
                        "45453000"
                ),

                List.of(
                        "Overhaul and refurbishment work"
                ),

                100
        );
    }

}
