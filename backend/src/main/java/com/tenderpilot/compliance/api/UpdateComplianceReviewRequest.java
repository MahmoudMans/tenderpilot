package com.tenderpilot.compliance.api;

import com.tenderpilot.compliance.domain.ComplianceRiskLevel;
import com.tenderpilot.compliance.domain.ComplianceVerdict;
import com.tenderpilot.compliance.domain.EvidenceStrength;

public record UpdateComplianceReviewRequest(

        ComplianceVerdict verdict,

        ComplianceRiskLevel riskLevel,

        EvidenceStrength evidenceStrength,

        String reviewRationale,

        String mitigationAction

) {
}
