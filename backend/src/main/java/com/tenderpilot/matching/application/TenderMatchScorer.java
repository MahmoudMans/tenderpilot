package com.tenderpilot.matching.application;

import com.tenderpilot.matching.domain.CompanyMatchProfile;
import com.tenderpilot.matching.domain.MatchSignalCode;
import com.tenderpilot.matching.domain.MatchSignalImpact;
import com.tenderpilot.matching.domain.MatchSignalValue;
import com.tenderpilot.matching.domain.TenderMatchCandidate;
import com.tenderpilot.matching.domain.TenderMatchEvaluation;
import com.tenderpilot.matching.domain.TenderMatchGrade;

import java.math.BigDecimal;

import java.text.Normalizer;

import java.time.Duration;
import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class TenderMatchScorer {

    private static final int CPV_MAX = 25;

    private static final int GEOGRAPHY_MAX = 15;

    private static final int VALUE_MAX = 15;

    private static final int BUYER_MAX = 10;

    private static final int CAPABILITY_MAX = 15;

    private static final int TIMING_MAX = 10;

    private static final int QUALITY_MAX = 10;


    public TenderMatchEvaluation evaluate(
            CompanyMatchProfile profile,
            TenderMatchCandidate tender,
            Instant now
    ) {

        List<MatchSignalValue> signals =
                new ArrayList<>();


        String tenderText =
                normalized(
                        String.join(
                                " ",
                                safe(tender.title()),
                                safe(tender.description()),
                                String.join(
                                        " ",
                                        tender.cpvLabels()
                                )
                        )
                );


        String blockedKeyword =
                firstExcludedKeyword(
                        profile,
                        tenderText
                );


        if (
                blockedKeyword != null
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.EXCLUDED_KEYWORD,
                            MatchSignalImpact.BLOCKER,
                            "exclusions",
                            0,
                            100,
                            "Opportunity contains excluded keyword: "
                                    +
                                    blockedKeyword
                    )
            );


            return new TenderMatchEvaluation(
                    0,
                    TenderMatchGrade.BLOCKED,
                    true,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    List.copyOf(
                            signals
                    )
            );
        }


        int cpv =
                cpvScore(
                        profile,
                        tender,
                        signals
                );


        int geography =
                geographyScore(
                        profile,
                        tender,
                        signals
                );


        int value =
                valueScore(
                        profile,
                        tender,
                        signals
                );


        int buyer =
                buyerScore(
                        profile,
                        tender,
                        signals
                );


        int capability =
                capabilityScore(
                        profile,
                        tenderText,
                        signals
                );


        int timing =
                timingScore(
                        profile,
                        tender,
                        now,
                        signals
                );


        int quality =
                qualityScore(
                        tender,
                        signals
                );


        int total =
                cpv
                        +
                        geography
                        +
                        value
                        +
                        buyer
                        +
                        capability
                        +
                        timing
                        +
                        quality;


        total =
                Math.max(
                        0,
                        Math.min(
                                100,
                                total
                        )
                );


        return new TenderMatchEvaluation(
                total,
                TenderMatchGrade.fromScore(
                        total
                ),
                false,
                cpv,
                geography,
                value,
                buyer,
                capability,
                timing,
                quality,
                List.copyOf(
                        signals
                )
        );
    }


    private int cpvScore(
            CompanyMatchProfile profile,
            TenderMatchCandidate tender,
            List<MatchSignalValue> signals
    ) {

        if (
                profile.targetCpvs()
                        .isEmpty()
                ||
                tender.cpvCodes()
                        .isEmpty()
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.CPV_NO_MATCH,
                            MatchSignalImpact.NEGATIVE,
                            "cpv",
                            0,
                            CPV_MAX,
                            "No usable CPV overlap could be established."
                    )
            );

            return 0;
        }


        int bestPrefix =
                0;


        for (
                String target :
                profile.targetCpvs()
        ) {

            for (
                    String actual :
                    tender.cpvCodes()
            ) {

                int prefix =
                        commonPrefix(
                                digits(target),
                                digits(actual)
                        );


                bestPrefix =
                        Math.max(
                                bestPrefix,
                                prefix
                        );
            }
        }


        int score;


        if (
                bestPrefix >= 8
        ) {

            score = 25;

        } else if (
                bestPrefix >= 5
        ) {

            score = 20;

        } else if (
                bestPrefix >= 4
        ) {

            score = 17;

        } else if (
                bestPrefix >= 3
        ) {

            score = 14;

        } else if (
                bestPrefix >= 2
        ) {

            score = 10;

        } else {

            score = 0;
        }


        if (
                score == 25
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.CPV_EXACT_MATCH,
                            MatchSignalImpact.POSITIVE,
                            "cpv",
                            score,
                            CPV_MAX,
                            "Opportunity contains an exact target CPV classification."
                    )
            );

        } else if (
                score > 0
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.CPV_HIERARCHY_MATCH,
                            MatchSignalImpact.POSITIVE,
                            "cpv",
                            score,
                            CPV_MAX,
                            "Opportunity matches the company's target CPV hierarchy."
                    )
            );

        } else {

            signals.add(
                    signal(
                            MatchSignalCode.CPV_NO_MATCH,
                            MatchSignalImpact.NEGATIVE,
                            "cpv",
                            0,
                            CPV_MAX,
                            "Opportunity CPVs do not match company target CPVs."
                    )
            );
        }


        return score;
    }


    private int geographyScore(
            CompanyMatchProfile profile,
            TenderMatchCandidate tender,
            List<MatchSignalValue> signals
    ) {

        if (
                profile.operatingRegions()
                        .isEmpty()
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.GEOGRAPHY_COUNTRY_MATCH,
                            MatchSignalImpact.NEUTRAL,
                            "geography",
                            8,
                            GEOGRAPHY_MAX,
                            "No explicit company operating-region preference is configured."
                    )
            );

            return 8;
        }


        for (
                CompanyMatchProfile.OperatingRegion region :
                profile.operatingRegions()
        ) {

            if (
                    equalsNormalized(
                            region.regionCode(),
                            tender.regionCode()
                    )
                    ||
                    equalsNormalized(
                            region.regionName(),
                            tender.regionName()
                    )
            ) {

                signals.add(
                        signal(
                                MatchSignalCode.GEOGRAPHY_EXACT_MATCH,
                                MatchSignalImpact.POSITIVE,
                                "geography",
                                15,
                                GEOGRAPHY_MAX,
                                "Opportunity is in a configured operating region."
                        )
                );

                return 15;
            }
        }


        for (
                CompanyMatchProfile.OperatingRegion region :
                profile.operatingRegions()
        ) {

            if (
                    equalsNormalized(
                            region.countryCode(),
                            tender.countryCode()
                    )
            ) {

                signals.add(
                        signal(
                                MatchSignalCode.GEOGRAPHY_COUNTRY_MATCH,
                                MatchSignalImpact.NEUTRAL,
                                "geography",
                                8,
                                GEOGRAPHY_MAX,
                                "Opportunity is in a supported country but outside the preferred region."
                        )
                );

                return 8;
            }
        }


        signals.add(
                signal(
                        MatchSignalCode.GEOGRAPHY_NO_MATCH,
                        MatchSignalImpact.NEGATIVE,
                        "geography",
                        0,
                        GEOGRAPHY_MAX,
                        "Opportunity geography does not match configured operating regions."
                )
        );


        return 0;
    }


    private int valueScore(
            CompanyMatchProfile profile,
            TenderMatchCandidate tender,
            List<MatchSignalValue> signals
    ) {

        BigDecimal preferredMin =
                profile.preferredMinimumValue();


        BigDecimal preferredMax =
                profile.preferredMaximumValue();


        if (
                preferredMin == null
                &&
                preferredMax == null
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.VALUE_UNKNOWN,
                            MatchSignalImpact.NEUTRAL,
                            "value",
                            10,
                            VALUE_MAX,
                            "No preferred contract-value range is configured."
                    )
            );

            return 10;
        }


        BigDecimal tenderMin =
                tender.estimatedMinimumValue();


        BigDecimal tenderMax =
                tender.estimatedMaximumValue();


        if (
                tenderMin == null
                &&
                tenderMax == null
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.VALUE_UNKNOWN,
                            MatchSignalImpact.NEUTRAL,
                            "value",
                            5,
                            VALUE_MAX,
                            "Opportunity does not disclose a structured estimated value."
                    )
            );

            return 5;
        }


        if (
                profile.preferredCurrency()
                        !=
                        null
                &&
                tender.currency()
                        !=
                        null
                &&
                !profile
                        .preferredCurrency()
                        .equalsIgnoreCase(
                                tender.currency()
                        )
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.VALUE_OUT_OF_RANGE,
                            MatchSignalImpact.NEGATIVE,
                            "value",
                            0,
                            VALUE_MAX,
                            "Opportunity currency differs from the preferred company currency."
                    )
            );

            return 0;
        }


        BigDecimal actualMin =
                tenderMin != null
                        ? tenderMin
                        : tenderMax;


        BigDecimal actualMax =
                tenderMax != null
                        ? tenderMax
                        : tenderMin;


        BigDecimal acceptedMin =
                preferredMin != null
                        ? preferredMin
                        : BigDecimal.ZERO;


        BigDecimal acceptedMax =
                preferredMax;


        boolean aboveMinimum =
                actualMax.compareTo(
                        acceptedMin
                )
                        >=
                        0;


        boolean belowMaximum =
                acceptedMax == null
                ||
                actualMin.compareTo(
                        acceptedMax
                )
                        <=
                        0;


        if (
                aboveMinimum
                &&
                belowMaximum
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.VALUE_IN_RANGE,
                            MatchSignalImpact.POSITIVE,
                            "value",
                            15,
                            VALUE_MAX,
                            "Opportunity value overlaps the preferred contract range."
                    )
            );

            return 15;
        }


        signals.add(
                signal(
                        MatchSignalCode.VALUE_OUT_OF_RANGE,
                        MatchSignalImpact.NEGATIVE,
                        "value",
                        0,
                        VALUE_MAX,
                        "Opportunity value is outside the preferred contract range."
                )
        );


        return 0;
    }


    private int buyerScore(
            CompanyMatchProfile profile,
            TenderMatchCandidate tender,
            List<MatchSignalValue> signals
    ) {

        if (
                profile.preferredBuyerTypes()
                        .isEmpty()
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.BUYER_NEUTRAL,
                            MatchSignalImpact.NEUTRAL,
                            "buyer",
                            5,
                            BUYER_MAX,
                            "No preferred buyer categories are configured."
                    )
            );

            return 5;
        }


        String actual =
                normalized(
                        tender.buyerCategory()
                );


        boolean matched =
                profile
                        .preferredBuyerTypes()
                        .stream()
                        .map(
                                this::normalized
                        )
                        .anyMatch(
                                preferred ->
                                        preferred != null
                                        &&
                                        actual != null
                                        &&
                                        (
                                                preferred.equals(
                                                        actual
                                                )
                                                ||
                                                preferred.contains(
                                                        actual
                                                )
                                                ||
                                                actual.contains(
                                                        preferred
                                                )
                                        )
                        );


        if (
                matched
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.BUYER_PREFERRED,
                            MatchSignalImpact.POSITIVE,
                            "buyer",
                            10,
                            BUYER_MAX,
                            "Buyer category matches a preferred buyer type."
                    )
            );

            return 10;
        }


        signals.add(
                signal(
                        MatchSignalCode.BUYER_NOT_PREFERRED,
                        MatchSignalImpact.NEGATIVE,
                        "buyer",
                        0,
                        BUYER_MAX,
                        "Buyer category is outside configured preferences."
                )
        );


        return 0;
    }


    private int capabilityScore(
            CompanyMatchProfile profile,
            String tenderText,
            List<MatchSignalValue> signals
    ) {

        if (
                profile.capabilities()
                        .isEmpty()
                ||
                tenderText == null
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.CAPABILITY_NO_MATCH,
                            MatchSignalImpact.NEGATIVE,
                            "capabilities",
                            0,
                            CAPABILITY_MAX,
                            "No capability evidence could be matched."
                    )
            );

            return 0;
        }


        int matched =
                0;


        List<String> matchedNames =
                new ArrayList<>();


        for (
                String rawCapability :
                profile.capabilities()
        ) {

            String capability =
                    normalized(
                            rawCapability
                    );


            if (
                    capability == null
            ) {
                continue;
            }


            boolean fullPhrase =
                    tenderText.contains(
                            capability
                    );


            if (
                    fullPhrase
                    ||
                    tokenCoverage(
                            capability,
                            tenderText
                    )
                            >=
                            0.5
            ) {

                matched++;

                matchedNames.add(
                        rawCapability
                );
            }
        }


        int score =
                (int)
                        Math.round(
                                CAPABILITY_MAX
                                        *
                                        (
                                                matched
                                                        /
                                                        (double)
                                                                profile
                                                                        .capabilities()
                                                                        .size()
                                        )
                        );


        score =
                Math.max(
                        0,
                        Math.min(
                                CAPABILITY_MAX,
                                score
                        )
                );


        if (
                score > 0
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.CAPABILITY_MATCH,
                            MatchSignalImpact.POSITIVE,
                            "capabilities",
                            score,
                            CAPABILITY_MAX,
                            "Matched company capabilities: "
                                    +
                                    String.join(
                                            ", ",
                                            matchedNames
                                    )
                    )
            );

        } else {

            signals.add(
                    signal(
                            MatchSignalCode.CAPABILITY_NO_MATCH,
                            MatchSignalImpact.NEGATIVE,
                            "capabilities",
                            0,
                            CAPABILITY_MAX,
                            "Opportunity text does not match configured company capabilities."
                    )
            );
        }


        return score;
    }


    private int timingScore(
            CompanyMatchProfile profile,
            TenderMatchCandidate tender,
            Instant now,
            List<MatchSignalValue> signals
    ) {

        if (
                tender.submissionDeadline()
                        ==
                        null
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.NOTICE_SHORT,
                            MatchSignalImpact.NEGATIVE,
                            "timing",
                            0,
                            TIMING_MAX,
                            "No structured submission deadline is available."
                    )
            );

            return 0;
        }


        long days =
                Duration
                        .between(
                                now,
                                tender.submissionDeadline()
                        )
                        .toDays();


        if (
                days < 0
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.NOTICE_PASSED,
                            MatchSignalImpact.NEGATIVE,
                            "timing",
                            0,
                            TIMING_MAX,
                            "Submission deadline has already passed."
                    )
            );

            return 0;
        }


        Integer required =
                profile.minimumNoticeDays();


        if (
                required == null
                ||
                days >= required
        ) {

            signals.add(
                    signal(
                            MatchSignalCode.NOTICE_SUFFICIENT,
                            MatchSignalImpact.POSITIVE,
                            "timing",
                            10,
                            TIMING_MAX,
                            "Available response time satisfies the configured notice requirement."
                    )
            );

            return 10;
        }


        signals.add(
                signal(
                        MatchSignalCode.NOTICE_SHORT,
                        MatchSignalImpact.NEGATIVE,
                        "timing",
                        5,
                        TIMING_MAX,
                        "Opportunity is still open, but the remaining response time is shorter than preferred."
                )
        );


        return 5;
    }


    private int qualityScore(
            TenderMatchCandidate tender,
            List<MatchSignalValue> signals
    ) {

        int raw =
                tender.dataQualityScore()
                        ==
                        null
                        ? 50
                        : Math.max(
                                0,
                                Math.min(
                                        100,
                                        tender.dataQualityScore()
                                )
                        );


        int score =
                (int)
                        Math.round(
                                raw / 10.0
                        );


        signals.add(
                signal(
                        MatchSignalCode.QUALITY_SCORE,
                        raw >= 75
                                ? MatchSignalImpact.POSITIVE
                                : MatchSignalImpact.NEUTRAL,
                        "quality",
                        score,
                        QUALITY_MAX,
                        "Procurement data quality score is "
                                +
                                raw
                                +
                                "/100."
                )
        );


        return score;
    }


    private String firstExcludedKeyword(
            CompanyMatchProfile profile,
            String tenderText
    ) {

        if (
                tenderText == null
        ) {
            return null;
        }


        for (
                String raw :
                profile.excludedKeywords()
        ) {

            String keyword =
                    normalized(
                            raw
                    );


            if (
                    keyword != null
                    &&
                    tenderText.contains(
                            keyword
                    )
            ) {

                return raw;
            }
        }


        return null;
    }


    private double tokenCoverage(
            String capability,
            String tenderText
    ) {

        String[] tokens =
                capability.split(
                        "\\s+"
                );


        int meaningful =
                0;


        int matched =
                0;


        for (
                String token :
                tokens
        ) {

            if (
                    token.length()
                            <
                            3
            ) {
                continue;
            }


            meaningful++;


            if (
                    tenderText.contains(
                            token
                    )
            ) {

                matched++;
            }
        }


        return meaningful == 0
                ? 0
                : matched
                        /
                        (double) meaningful;
    }


    private int commonPrefix(
            String left,
            String right
    ) {

        if (
                left == null
                ||
                right == null
        ) {
            return 0;
        }


        int length =
                Math.min(
                        left.length(),
                        right.length()
                );


        int index =
                0;


        while (
                index < length
                &&
                left.charAt(index)
                        ==
                        right.charAt(index)
        ) {

            index++;
        }


        return index;
    }


    private String digits(
            String value
    ) {

        if (
                value == null
        ) {
            return null;
        }


        String result =
                value.replaceAll(
                        "[^0-9]",
                        ""
                );


        return result.isBlank()
                ? null
                : result;
    }


    private boolean equalsNormalized(
            String left,
            String right
    ) {

        String normalizedLeft =
                normalized(
                        left
                );


        String normalizedRight =
                normalized(
                        right
                );


        return normalizedLeft != null
                &&
                normalizedLeft.equals(
                        normalizedRight
                );
    }


    private String normalized(
            String value
    ) {

        if (
                value == null
        ) {
            return null;
        }


        String cleaned =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        if (
                cleaned.isBlank()
        ) {
            return null;
        }


        return Normalizer
                .normalize(
                        cleaned,
                        Normalizer.Form.NFD
                )
                .replaceAll(
                        "\\p{M}+",
                        ""
                )
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9 ]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }


    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    private MatchSignalValue signal(
            MatchSignalCode code,
            MatchSignalImpact impact,
            String dimension,
            int earned,
            int maximum,
            String message
    ) {

        return new MatchSignalValue(
                code,
                impact,
                dimension,
                earned,
                maximum,
                message
        );
    }

}
