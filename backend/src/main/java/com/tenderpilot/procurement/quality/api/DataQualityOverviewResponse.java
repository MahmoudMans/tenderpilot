package com.tenderpilot.procurement.quality.api;

import java.time.Instant;

public record DataQualityOverviewResponse(

        long totalTenders,

        long assessedTenders,

        long unassessedTenders,

        double averageScore,

        long excellentCount,

        long goodCount,

        long fairCount,

        long poorCount,

        long criticalCount,

        long criticalIssueCount,

        long warningIssueCount,

        long infoIssueCount,

        Instant lastAssessedAt

) {
}
