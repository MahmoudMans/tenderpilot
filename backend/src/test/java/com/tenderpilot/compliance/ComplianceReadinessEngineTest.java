package com.tenderpilot.compliance;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.compliance.application.ComplianceReadinessEngine;

import com.tenderpilot.compliance.domain.CompliancePriority;
import com.tenderpilot.compliance.domain.ComplianceRequirementSnapshot;
import com.tenderpilot.compliance.domain.ComplianceRiskLevel;
import com.tenderpilot.compliance.domain.ComplianceVerdict;
import com.tenderpilot.compliance.domain.EvidenceStrength;

import java.util.List;

import org.junit.jupiter.api.Test;

class ComplianceReadinessEngineTest {

    private final ComplianceReadinessEngine engine =
            new ComplianceReadinessEngine();


    @Test
    void fullyCompliantStrongEvidenceIsSubmissionReady() {

        var result =
                engine.calculate(
                        List.of(
                                snapshot(
                                        true,
                                        ComplianceVerdict.COMPLIANT,
                                        ComplianceRiskLevel.LOW,
                                        EvidenceStrength.STRONG
                                ),

                                snapshot(
                                        true,
                                        ComplianceVerdict.COMPLIANT,
                                        ComplianceRiskLevel.LOW,
                                        EvidenceStrength.STRONG
                                )
                        )
                );


        assertThat(
                result.readinessScore()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.readyForSubmission()
        )
                .isTrue();
    }


    @Test
    void mandatoryNonComplianceCreatesBlocker() {

        var result =
                engine.calculate(
                        List.of(
                                snapshot(
                                        true,
                                        ComplianceVerdict.NON_COMPLIANT,
                                        ComplianceRiskLevel.HIGH,
                                        EvidenceStrength.NONE
                                )
                        )
                );


        assertThat(
                result.mandatoryBlockerCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.readyForSubmission()
        )
                .isFalse();


        assertThat(
                engine.priority(
                        snapshot(
                                true,
                                ComplianceVerdict.NON_COMPLIANT,
                                ComplianceRiskLevel.HIGH,
                                EvidenceStrength.NONE
                        )
                )
        )
                .isEqualTo(
                        CompliancePriority.BLOCKER
                );
    }


    @Test
    void mandatoryUnassessedRequirementPreventsReadiness() {

        var result =
                engine.calculate(
                        List.of(
                                snapshot(
                                        true,
                                        ComplianceVerdict.UNASSESSED,
                                        ComplianceRiskLevel.UNASSESSED,
                                        EvidenceStrength.NONE
                                )
                        )
                );


        assertThat(
                result.mandatoryIncompleteCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.readyForSubmission()
        )
                .isFalse();
    }


    @Test
    void criticalRiskPreventsSubmissionReadiness() {

        var result =
                engine.calculate(
                        List.of(
                                snapshot(
                                        true,
                                        ComplianceVerdict.COMPLIANT,
                                        ComplianceRiskLevel.CRITICAL,
                                        EvidenceStrength.STRONG
                                )
                        )
                );


        assertThat(
                result.readinessScore()
        )
                .isEqualTo(
                        80
                );


        assertThat(
                result.criticalRiskCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.readyForSubmission()
        )
                .isFalse();
    }


    @Test
    void evidenceStrengthChangesRowScore() {

        int strong =
                engine.rowScore(
                        ComplianceVerdict.COMPLIANT,
                        ComplianceRiskLevel.LOW,
                        EvidenceStrength.STRONG
                );


        int weak =
                engine.rowScore(
                        ComplianceVerdict.COMPLIANT,
                        ComplianceRiskLevel.LOW,
                        EvidenceStrength.WEAK
                );


        assertThat(
                strong
        )
                .isGreaterThan(
                        weak
                );


        assertThat(
                strong
        )
                .isEqualTo(
                        100
                );


        assertThat(
                weak
        )
                .isEqualTo(
                        85
                );
    }


    @Test
    void notApplicableRequirementDoesNotReduceReadiness() {

        var result =
                engine.calculate(
                        List.of(
                                snapshot(
                                        true,
                                        ComplianceVerdict.NOT_APPLICABLE,
                                        ComplianceRiskLevel.UNASSESSED,
                                        EvidenceStrength.NONE
                                )
                        )
                );


        assertThat(
                result.readinessScore()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.readyForSubmission()
        )
                .isTrue();
    }


    private ComplianceRequirementSnapshot snapshot(
            boolean mandatory,
            ComplianceVerdict verdict,
            ComplianceRiskLevel risk,
            EvidenceStrength evidence
    ) {

        return new ComplianceRequirementSnapshot(
                mandatory,
                verdict,
                risk,
                evidence
        );
    }

}
