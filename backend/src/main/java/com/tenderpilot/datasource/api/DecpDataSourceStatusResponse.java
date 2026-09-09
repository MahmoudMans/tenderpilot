package com.tenderpilot.datasource.api;

import com.tenderpilot.datasource.domain.IngestionRunStatus;

import java.time.Instant;
import java.util.UUID;

public record DecpDataSourceStatusResponse(

        String source,

        String apiVersion,

        String baseUrl,

        String recordsPath,

        String datasetId,

        boolean authenticationRequired,

        boolean manualSyncEnabled,

        long canonicalContractCount,

        Instant lastContractSeenAt,

        LastRunResponse lastRun

) {

    public record LastRunResponse(
            UUID id,
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
    }
}
