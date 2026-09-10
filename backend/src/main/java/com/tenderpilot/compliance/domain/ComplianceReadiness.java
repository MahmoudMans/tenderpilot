package com.tenderpilot.compliance.domain;

public record ComplianceReadiness(

        int readinessScore,

        int mandatoryBlockerCount,

        int mandatoryIncompleteCount,

        int criticalRiskCount,

        boolean readyForSubmission

) {
}
