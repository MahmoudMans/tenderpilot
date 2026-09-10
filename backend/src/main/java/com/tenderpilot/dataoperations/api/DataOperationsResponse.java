package com.tenderpilot.dataoperations.api;

import com.tenderpilot.dataoperations.domain.IngestionRunOutcome;
import com.tenderpilot.dataoperations.domain.SourceHealthStatus;

import java.time.Instant;

import java.util.List;

public record DataOperationsResponse(

        Instant generatedAt,

        Summary summary,

        List<Source> sources,

        List<RecentRun> recentRuns

) {

    public record Summary(

            int sourceCount,

            int healthySourceCount,

            int degradedSourceCount,

            int unhealthySourceCount,

            int staleSourceCount,

            int runningSourceCount,

            int unknownSourceCount,

            int runs24Hours,

            int failures24Hours,

            int runs7Days,

            Integer successRate7Days

    ) {
    }


    public record Source(

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


    public record RecentRun(

            String source,

            IngestionRunOutcome outcome,

            String rawStatus,

            Instant startedAt,

            Instant finishedAt,

            Long durationSeconds,

            Long importedCount,

            Long rejectedCount

    ) {
    }

}
