package com.tenderpilot.compliance.api;

import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementStatus;

import com.tenderpilot.compliance.domain.CompliancePriority;
import com.tenderpilot.compliance.domain.ComplianceRiskLevel;
import com.tenderpilot.compliance.domain.ComplianceVerdict;
import com.tenderpilot.compliance.domain.EvidenceStrength;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public record BidComplianceMatrixResponse(

        UUID tenderId,

        BidDecisionOutcome decisionOutcome,

        boolean writable,

        String writeLockReason,

        Statistics statistics,

        List<Row> rows

) {

    public record Statistics(

            int totalRequirements,

            int reviewedRequirements,

            int mandatoryRequirements,

            int compliantRequirements,

            int partialRequirements,

            int nonCompliantRequirements,

            int unassessedRequirements,

            int mandatoryBlockerCount,

            int mandatoryIncompleteCount,

            int criticalRiskCount,

            int readinessScore,

            boolean readyForSubmission

    ) {
    }


    public record Row(

            UUID requirementId,

            String title,

            String description,

            BidRequirementCategory category,

            BidRequirementStatus requirementStatus,

            boolean mandatory,

            UUID sourceDocumentId,

            String sourceDocumentName,

            String sourceReference,

            LocalDate dueDate,

            String assignedTo,

            String evidenceNotes,

            ComplianceVerdict verdict,

            ComplianceRiskLevel riskLevel,

            EvidenceStrength evidenceStrength,

            int readinessContribution,

            CompliancePriority priority,

            String nextAction,

            String reviewRationale,

            String mitigationAction,

            String reviewedBy,

            Instant reviewedAt

    ) {
    }

}
