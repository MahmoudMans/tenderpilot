package com.tenderpilot.datasource.api;

import com.tenderpilot.datasource.domain.IngestionRunStatus;

import java.time.Instant;

import java.util.UUID;

public record TedDataSourceStatusResponse(

        String source,

        String apiVersion,

        String baseUrl,

        String searchPath,

        boolean authenticationRequired,

        boolean manualSyncEnabled,

        long canonicalNoticeCount,

        Instant lastNoticeSeenAt,

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
