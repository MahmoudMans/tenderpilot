package com.tenderpilot.procurement.quality.application;

import com.tenderpilot.procurement.award.domain.TenderAwardEntity;

import com.tenderpilot.procurement.domain.ProcurementStatus;
import com.tenderpilot.procurement.domain.TenderEntity;

import com.tenderpilot.procurement.quality.domain.DataQualityEvaluation;
import com.tenderpilot.procurement.quality.domain.DataQualityGrade;
import com.tenderpilot.procurement.quality.domain.DataQualityIssueCode;
import com.tenderpilot.procurement.quality.domain.DataQualityIssueValue;
import com.tenderpilot.procurement.quality.domain.DataQualitySeverity;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class ProcurementDataQualityEvaluator {

    public DataQualityEvaluation evaluate(
            TenderEntity tender,
            TenderAwardEntity award
    ) {

        List<DataQualityIssueValue> issues =
                new ArrayList<>();

        int score = 100;


        /*
         * Buyer identity — 15 points.
         */
        if (
                tender.getBuyer() == null
                ||
                isGenericBuyer(
                        tender.getBuyer()
                                .getLegalName()
                )
        ) {

            issues.add(
                    issue(
                            DataQualityIssueCode
                                    .GENERIC_BUYER_IDENTITY,

                            DataQualitySeverity.WARNING,

                            "buyer.legalName",

                            "Buyer identity is generic or insufficiently specific."
                    )
            );

            score -= 15;
        }


        /*
         * CPV classification — 20 points.
         */
        if (
                tender.getCpvs() == null
                ||
                tender.getCpvs()
                        .isEmpty()
        ) {

            issues.add(
                    issue(
                            DataQualityIssueCode
                                    .MISSING_CPV,

                            DataQualitySeverity.WARNING,

                            "cpvs",

                            "No normalized CPV classification is available."
                    )
            );

            score -= 20;
        }


        /*
         * Geography — 15 points.
         */
        if (
                tender.getLocations() == null
                ||
                tender.getLocations()
                        .isEmpty()
        ) {

            issues.add(
                    issue(
                            DataQualityIssueCode
                                    .MISSING_LOCATION,

                            DataQualitySeverity.WARNING,

                            "locations",

                            "No structured place of performance is available."
                    )
            );

            score -= 15;
        }


        /*
         * Open-notice timing — 10 points.
         */
        if (
                requiresSubmissionDeadline(
                        tender.getStatus()
                )
                &&
                tender.getSubmissionDeadline()
                        ==
                        null
        ) {

            issues.add(
                    issue(
                            DataQualityIssueCode
                                    .MISSING_SUBMISSION_DEADLINE,

                            DataQualitySeverity.WARNING,

                            "submissionDeadline",

                            "Published opportunity has no structured submission deadline."
                    )
            );

            score -= 10;
        }


        /*
         * Invalid chronology — critical 25-point deduction.
         */
        if (
                tender.getSubmissionDeadline()
                        !=
                        null
                &&
                tender.getPublicationDate()
                        !=
                        null
                &&
                tender.getSubmissionDeadline()
                        .isBefore(
                                tender
                                        .getPublicationDate()
                                        .atStartOfDay()
                                        .toInstant(
                                                ZoneOffset.UTC
                                        )
                        )
        ) {

            issues.add(
                    issue(
                            DataQualityIssueCode
                                    .DEADLINE_BEFORE_PUBLICATION,

                            DataQualitySeverity.CRITICAL,

                            "submissionDeadline",

                            "Submission deadline occurs before the publication date."
                    )
            );

            score -= 25;
        }


        /*
         * Open-notice commercial value — 5 points.
         *
         * Missing values are common in procurement notices, so this is INFO
         * rather than WARNING.
         */
        if (
                requiresEstimatedValue(
                        tender.getStatus()
                )
                &&
                tender.getEstimatedMinValue()
                        ==
                        null
                &&
                tender.getEstimatedMaxValue()
                        ==
                        null
        ) {

            issues.add(
                    issue(
                            DataQualityIssueCode
                                    .MISSING_ESTIMATED_VALUE,

                            DataQualitySeverity.INFO,

                            "estimatedValue",

                            "Published opportunity has no structured estimated value."
                    )
            );

            score -= 5;
        }


        /*
         * Provenance — 25 critical points if entirely absent.
         * Otherwise 5 informational points if source URLs are absent.
         */
        if (
                tender.getSourceRecords() == null
                ||
                tender.getSourceRecords()
                        .isEmpty()
        ) {

            issues.add(
                    issue(
                            DataQualityIssueCode
                                    .MISSING_SOURCE_LINEAGE,

                            DataQualitySeverity.CRITICAL,

                            "sources",

                            "Canonical procurement record has no source lineage."
                    )
            );

            score -= 25;

        } else {

            boolean sourceUrlAvailable =
                    tender
                            .getSourceRecords()
                            .stream()
                            .anyMatch(
                                    source ->
                                            source.getSourceUrl()
                                                    !=
                                                    null
                                            &&
                                            !source
                                                    .getSourceUrl()
                                                    .isBlank()
                            );


            if (!sourceUrlAvailable) {

                issues.add(
                        issue(
                                DataQualityIssueCode
                                        .MISSING_SOURCE_URL,

                                DataQualitySeverity.INFO,

                                "sources.sourceUrl",

                                "Source lineage exists but no source URL is available."
                        )
                );

                score -= 5;
            }
        }


        /*
         * Award-stage consistency.
         */
        if (
                tender.getStatus()
                        ==
                        ProcurementStatus.AWARDED
        ) {

            if (award == null) {

                issues.add(
                        issue(
                                DataQualityIssueCode
                                        .AWARD_RECORD_MISSING,

                                DataQualitySeverity.CRITICAL,

                                "award",

                                "Tender is marked AWARDED but has no canonical award record."
                        )
                );

                score -= 25;

            } else {

                if (
                        award.getAwardedAmount()
                                ==
                                null
                ) {

                    issues.add(
                            issue(
                                    DataQualityIssueCode
                                            .AWARDED_AMOUNT_MISSING,

                                    DataQualitySeverity.WARNING,

                                    "award.awardedAmount",

                                    "Award record does not contain an awarded amount."
                            )
                    );

                    score -= 10;
                }


                if (
                        award.getSuppliers()
                                .isEmpty()
                ) {

                    issues.add(
                            issue(
                                    DataQualityIssueCode
                                            .AWARDED_SUPPLIER_MISSING,

                                    DataQualitySeverity.WARNING,

                                    "award.suppliers",

                                    "Award record does not contain a structured awarded supplier."
                            )
                    );

                    score -= 10;
                }
            }
        }


        int normalizedScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                score
                        )
                );


        return new DataQualityEvaluation(
                normalizedScore,
                DataQualityGrade.fromScore(
                        normalizedScore
                ),
                List.copyOf(
                        issues
                )
        );
    }


    private boolean requiresSubmissionDeadline(
            ProcurementStatus status
    ) {

        return status
                ==
                ProcurementStatus.PUBLISHED
                ||
                status
                        ==
                        ProcurementStatus.UPDATED;
    }


    private boolean requiresEstimatedValue(
            ProcurementStatus status
    ) {

        return status
                ==
                ProcurementStatus.PUBLISHED
                ||
                status
                        ==
                        ProcurementStatus.UPDATED;
    }


    private boolean isGenericBuyer(
            String value
    ) {

        if (
                value == null
                ||
                value.isBlank()
        ) {
            return true;
        }


        String normalized =
                value
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        return normalized.equals(
                "ted contracting authority"
        )
                ||
                normalized.equals(
                        "acheteur public boamp"
                )
                ||
                normalized.equals(
                        "french public buyer"
                )
                ||
                normalized.equals(
                        "acheteur public français"
                )
                ||
                normalized.startsWith(
                        "public buyer "
                )
                ||
                normalized.startsWith(
                        "acheteur "
                );
    }


    private DataQualityIssueValue issue(
            DataQualityIssueCode code,
            DataQualitySeverity severity,
            String field,
            String message
    ) {

        return new DataQualityIssueValue(
                code,
                severity,
                field,
                message
        );
    }

}
