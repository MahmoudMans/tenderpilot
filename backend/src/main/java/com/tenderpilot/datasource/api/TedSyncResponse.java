package com.tenderpilot.datasource.api;

import com.tenderpilot.datasource.domain.IngestionRunEntity;
import com.tenderpilot.datasource.domain.IngestionRunStatus;

import java.time.Instant;

import java.util.UUID;

public record TedSyncResponse(

        UUID runId,

        IngestionRunStatus status,

        String query,

        int requestedLimit,

        int discoveredCount,

        int processedCount,

        int failedCount,

        Instant startedAt,

        Instant completedAt,

        String lastError

) {

    public static TedSyncResponse from(
            IngestionRunEntity run
    ) {

        return new TedSyncResponse(

                run.getId(),

                run.getStatus(),

                run.getQueryText(),

                run.getRequestedLimit(),

                run.getDiscoveredCount(),

                run.getProcessedCount(),

                run.getFailedCount(),

                run.getStartedAt(),

                run.getCompletedAt(),

                run.getLastError()

        );

    }

}
