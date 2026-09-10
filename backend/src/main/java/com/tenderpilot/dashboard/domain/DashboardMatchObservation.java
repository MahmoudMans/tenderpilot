package com.tenderpilot.dashboard.domain;

import java.time.Instant;

import java.util.UUID;

public record DashboardMatchObservation(

        UUID tenderId,

        UUID buyerId,

        String tenderTitle,

        String buyerName,

        Instant submissionDeadline,

        int matchScore,

        String matchGrade,

        boolean blocked

) {
}
