package com.tenderpilot.bidplanning.api;

import java.time.Instant;

public record UpdateSubmissionControlRequest(

        Boolean portalReady,

        Boolean signaturesReady,

        Boolean technicalResponseReady,

        Boolean commercialResponseReady,

        Boolean administrativePackReady,

        Boolean finalReviewReady,

        Instant plannedSubmissionAt,

        String notes

) {
}
