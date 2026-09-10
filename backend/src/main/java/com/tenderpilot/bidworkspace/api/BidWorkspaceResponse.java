package com.tenderpilot.bidworkspace.api;

import com.tenderpilot.biddecision.domain.BidDecisionOutcome;

import com.tenderpilot.bidworkspace.domain.BidDocumentCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementStatus;

import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public record BidWorkspaceResponse(

        UUID tenderId,

        int matchScore,

        TenderMatchGrade matchGrade,

        BidDecisionOutcome decisionOutcome,

        boolean writable,

        String writeLockReason,

        OpportunitySummaryResponse opportunity,

        Statistics statistics,

        List<DocumentResponse> documents,

        List<RequirementResponse> requirements

) {

    public record Statistics(

            int documentCount,

            int requirementCount,

            int completedRequirementCount,

            int gapRequirementCount,

            int mandatoryGapCount,

            int completionPercentage

    ) {
    }


    public record DocumentResponse(

            UUID id,

            BidDocumentCategory category,

            String originalFilename,

            String contentType,

            long sizeBytes,

            String sha256,

            String note,

            String uploadedBy,

            Instant uploadedAt

    ) {
    }


    public record RequirementResponse(

            UUID id,

            UUID sourceDocumentId,

            String sourceDocumentName,

            String title,

            String description,

            BidRequirementCategory category,

            BidRequirementStatus status,

            boolean mandatory,

            String sourceReference,

            LocalDate dueDate,

            String assignedTo,

            String evidenceNotes,

            String createdBy,

            String updatedBy,

            Instant createdAt,

            Instant updatedAt

    ) {
    }

}
