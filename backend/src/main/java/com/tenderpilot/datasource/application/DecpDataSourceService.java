package com.tenderpilot.datasource.application;

import com.tenderpilot.datasource.api.DecpDataSourceStatusResponse;
import com.tenderpilot.datasource.decp.DecpSettings;
import com.tenderpilot.datasource.domain.IngestionRunEntity;
import com.tenderpilot.datasource.infrastructure.IngestionRunRepository;

import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.TenderSourceRecordEntity;
import com.tenderpilot.procurement.infrastructure.TenderSourceRecordRepository;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DecpDataSourceService {

    private final DecpSettings settings;

    private final TenderSourceRecordRepository
            sourceRecordRepository;

    private final IngestionRunRepository
            ingestionRunRepository;

    public DecpDataSourceService(
            DecpSettings settings,
            TenderSourceRecordRepository sourceRecordRepository,
            IngestionRunRepository ingestionRunRepository
    ) {

        this.settings = settings;

        this.sourceRecordRepository =
                sourceRecordRepository;

        this.ingestionRunRepository =
                ingestionRunRepository;
    }

    @Transactional(readOnly = true)
    public DecpDataSourceStatusResponse status() {

        long count =
                sourceRecordRepository
                        .countBySource(
                                ProcurementSource.DECP
                        );

        Instant lastSeen =
                sourceRecordRepository
                        .findTop1BySourceOrderByLastSeenAtDesc(
                                ProcurementSource.DECP
                        )
                        .map(
                                TenderSourceRecordEntity
                                        ::getLastSeenAt
                        )
                        .orElse(null);

        DecpDataSourceStatusResponse.LastRunResponse
                lastRun =
                ingestionRunRepository
                        .findTop1BySourceOrderByStartedAtDesc(
                                ProcurementSource.DECP
                        )
                        .map(this::runResponse)
                        .orElse(null);

        return new DecpDataSourceStatusResponse(
                "DECP",
                "Explore API 2.1",
                settings.baseUrl(),
                settings.recordsPath(),
                settings.datasetId(),
                false,
                settings.manualSyncEnabled(),
                count,
                lastSeen,
                lastRun
        );
    }

    private DecpDataSourceStatusResponse.LastRunResponse
    runResponse(
            IngestionRunEntity run
    ) {

        return new DecpDataSourceStatusResponse
                .LastRunResponse(
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
