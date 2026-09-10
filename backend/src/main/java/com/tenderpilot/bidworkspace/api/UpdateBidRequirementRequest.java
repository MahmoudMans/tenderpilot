package com.tenderpilot.bidworkspace.api;

import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;
import com.tenderpilot.bidworkspace.domain.BidRequirementStatus;

import java.time.LocalDate;

import java.util.UUID;

public record UpdateBidRequirementRequest(

        String title,

        String description,

        BidRequirementCategory category,

        BidRequirementStatus status,

        Boolean mandatory,

        UUID sourceDocumentId,

        String sourceReference,

        LocalDate dueDate,

        String assignedTo,

        String evidenceNotes

) {
}
