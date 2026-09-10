package com.tenderpilot.compliance.domain;

public record ComplianceRequirementSnapshot(

        boolean mandatory,

        ComplianceVerdict verdict,

        ComplianceRiskLevel riskLevel,

        EvidenceStrength evidenceStrength

) {
}
