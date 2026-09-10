package com.tenderpilot.bidworkspace.api;

import com.tenderpilot.bidworkspace.domain.BidRequirementCategory;

import java.time.LocalDate;

import java.util.UUID;

public record CreateBidRequirementRequest(

        String title,

        String description,

        BidRequirementCategory category,

        Boolean mandatory,

        UUID sourceDocumentId,

        String sourceReference,

        LocalDate dueDate,

        String assignedTo,

        String evidenceNotes

) {
}
