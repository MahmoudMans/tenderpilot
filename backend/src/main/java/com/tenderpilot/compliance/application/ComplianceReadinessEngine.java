package com.tenderpilot.compliance.application;

import com.tenderpilot.compliance.domain.CompliancePriority;
import com.tenderpilot.compliance.domain.ComplianceReadiness;
import com.tenderpilot.compliance.domain.ComplianceRequirementSnapshot;
import com.tenderpilot.compliance.domain.ComplianceRiskLevel;
import com.tenderpilot.compliance.domain.ComplianceVerdict;
import com.tenderpilot.compliance.domain.EvidenceStrength;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ComplianceReadinessEngine {

    public ComplianceReadiness calculate(
            List<ComplianceRequirementSnapshot> requirements
    ) {

        if (
                requirements == null
                ||
                requirements.isEmpty()
        ) {

            return new ComplianceReadiness(
                    0,
                    0,
                    0,
                    0,
                    false
            );
        }


        double weightedPoints =
                0;

        double totalWeight =
                0;


        int mandatoryBlockers =
                0;

        int mandatoryIncomplete =
                0;

        int criticalRisks =
                0;


        for (
                ComplianceRequirementSnapshot requirement :
                requirements
        ) {

            double weight =
                    requirement.mandatory()
                            ? 2.0
                            : 1.0;


            weightedPoints +=
                    rowScore(
                            requirement.verdict(),
                            requirement.riskLevel(),
                            requirement.evidenceStrength()
                    )
                            *
                            weight;


            totalWeight +=
                    weight;


            if (
                    requirement.mandatory()
                    &&
                    requirement.verdict()
                            ==
                            ComplianceVerdict.NON_COMPLIANT
            ) {

                mandatoryBlockers++;
            }


            if (
                    requirement.mandatory()
                    &&
                    requirement.verdict()
                            !=
                            ComplianceVerdict.COMPLIANT
                    &&
                    requirement.verdict()
                            !=
                            ComplianceVerdict.NOT_APPLICABLE
            ) {

                mandatoryIncomplete++;
            }


            if (
                    requirement.riskLevel()
                            ==
                            ComplianceRiskLevel.CRITICAL
                    &&
                    requirement.verdict()
                            !=
                            ComplianceVerdict.NOT_APPLICABLE
            ) {

                criticalRisks++;
            }
        }


        int score =
                totalWeight == 0
                        ? 0
                        : (int)
                                Math.round(
                                        weightedPoints
                                                /
                                                totalWeight
                                );


        score =
                Math.max(
                        0,
                        Math.min(
                                100,
                                score
                        )
                );


        boolean ready =
                score >= 80
                &&
                mandatoryBlockers == 0
                &&
                mandatoryIncomplete == 0
                &&
                criticalRisks == 0;


        return new ComplianceReadiness(
                score,
                mandatoryBlockers,
                mandatoryIncomplete,
                criticalRisks,
                ready
        );
    }


    public int rowScore(
            ComplianceVerdict verdict,
            ComplianceRiskLevel riskLevel,
            EvidenceStrength evidenceStrength
    ) {

        int base =
                switch (
                        verdict
                ) {

                    case COMPLIANT ->
                            100;

                    case PARTIAL ->
                            60;

                    case NON_COMPLIANT ->
                            0;

                    case UNASSESSED ->
                            20;

                    case NOT_APPLICABLE ->
                            100;
                };


        if (
                verdict
                        ==
                        ComplianceVerdict.NOT_APPLICABLE
        ) {

            return 100;
        }


        int evidencePenalty =
                switch (
                        evidenceStrength
                ) {

                    case STRONG ->
                            0;

                    case MODERATE ->
                            5;

                    case WEAK ->
                            15;

                    case NONE ->
                            (
                                    verdict
                                            ==
                                            ComplianceVerdict.COMPLIANT
                                    ||
                                    verdict
                                            ==
                                            ComplianceVerdict.PARTIAL
                            )
                                    ? 25
                                    : 0;
                };


        int riskPenalty =
                switch (
                        riskLevel
                ) {

                    case UNASSESSED, LOW ->
                            0;

                    case MEDIUM ->
                            5;

                    case HIGH ->
                            12;

                    case CRITICAL ->
                            20;
                };


        return Math.max(
                0,
                Math.min(
                        100,
                        base
                                -
                                evidencePenalty
                                -
                                riskPenalty
                )
        );
    }


    public CompliancePriority priority(
            ComplianceRequirementSnapshot requirement
    ) {

        if (
                requirement.mandatory()
                &&
                requirement.verdict()
                        ==
                        ComplianceVerdict.NON_COMPLIANT
        ) {

            return CompliancePriority.BLOCKER;
        }


        if (
                requirement.riskLevel()
                        ==
                        ComplianceRiskLevel.CRITICAL
        ) {

            return CompliancePriority.CRITICAL;
        }


        if (
                requirement.mandatory()
                &&
                (
                        requirement.verdict()
                                ==
                                ComplianceVerdict.UNASSESSED
                        ||
                        requirement.verdict()
                                ==
                                ComplianceVerdict.PARTIAL
                )
        ) {

            return CompliancePriority.HIGH;
        }


        if (
                requirement.riskLevel()
                        ==
                        ComplianceRiskLevel.HIGH
        ) {

            return CompliancePriority.HIGH;
        }


        if (
                requirement.verdict()
                        ==
                        ComplianceVerdict.COMPLIANT
                &&
                requirement.evidenceStrength()
                        ==
                        EvidenceStrength.STRONG
        ) {

            return CompliancePriority.LOW;
        }


        return CompliancePriority.MEDIUM;
    }


    public String nextAction(
            ComplianceRequirementSnapshot requirement
    ) {

        if (
                requirement.verdict()
                        ==
                        ComplianceVerdict.NOT_APPLICABLE
        ) {

            return "No further compliance action is required.";
        }


        if (
                requirement.verdict()
                        ==
                        ComplianceVerdict.UNASSESSED
        ) {

            return "Review the requirement and record a compliance verdict.";
        }


        if (
                requirement.verdict()
                        ==
                        ComplianceVerdict.NON_COMPLIANT
        ) {

            return requirement.mandatory()
                    ? "Resolve this mandatory compliance gap before submission."
                    : "Resolve or formally accept this compliance gap.";
        }


        if (
                requirement.verdict()
                        ==
                        ComplianceVerdict.PARTIAL
        ) {

            return "Complete the outstanding response and supporting evidence.";
        }


        if (
                requirement.riskLevel()
                        ==
                        ComplianceRiskLevel.CRITICAL
                ||
                requirement.riskLevel()
                        ==
                        ComplianceRiskLevel.HIGH
        ) {

            return "Mitigate the identified compliance risk and re-review.";
        }


        if (
                requirement.evidenceStrength()
                        ==
                        EvidenceStrength.NONE
                ||
                requirement.evidenceStrength()
                        ==
                        EvidenceStrength.WEAK
        ) {

            return "Strengthen the supporting evidence before final submission.";
        }


        return "Requirement is compliance-ready; preserve the supporting evidence.";
    }

}
