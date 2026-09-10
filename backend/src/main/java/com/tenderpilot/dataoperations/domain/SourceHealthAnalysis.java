package com.tenderpilot.dataoperations.domain;

import java.time.Instant;

public record SourceHealthAnalysis(

        String source,

        SourceHealthStatus health,

        int runCount7Days,

        int successCount7Days,

        int failureCount7Days,

        Integer successRate7Days,

        int failureStreak,

        Instant lastStartedAt,

        Instant lastFinishedAt,

        Instant lastSuccessAt,

        Instant lastFailureAt,

        long importedCount7Days,

        long rejectedCount7Days

) {
}
