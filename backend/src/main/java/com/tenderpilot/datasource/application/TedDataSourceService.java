package com.tenderpilot.datasource.application;

import com.tenderpilot.datasource.api.TedDataSourceStatusResponse;

import com.tenderpilot.datasource.domain.IngestionRunEntity;

import com.tenderpilot.datasource.infrastructure.IngestionRunRepository;

import com.tenderpilot.datasource.ted.TedSettings;

import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.TenderSourceRecordEntity;

import com.tenderpilot.procurement.infrastructure.TenderSourceRecordRepository;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TedDataSourceService {

    private final TedSettings
            settings;


    private final TenderSourceRecordRepository
            sourceRecordRepository;


    private final IngestionRunRepository
            ingestionRunRepository;


    public TedDataSourceService(

            TedSettings settings,

            TenderSourceRecordRepository sourceRecordRepository,

            IngestionRunRepository ingestionRunRepository

    ) {

        this.settings =
                settings;

        this.sourceRecordRepository =
                sourceRecordRepository;

        this.ingestionRunRepository =
                ingestionRunRepository;

    }


    @Transactional(
            readOnly = true
    )
    public TedDataSourceStatusResponse status() {

        long count =

                sourceRecordRepository
                        .countBySource(
                                ProcurementSource.TED
                        );


        Instant lastSeen =

                sourceRecordRepository
                        .findTop1BySourceOrderByLastSeenAtDesc(
                                ProcurementSource.TED
                        )
                        .map(
                                TenderSourceRecordEntity
                                        ::getLastSeenAt
                        )
                        .orElse(
                                null
                        );


        TedDataSourceStatusResponse.LastRunResponse
                lastRun =

                ingestionRunRepository
                        .findTop1BySourceOrderByStartedAtDesc(
                                ProcurementSource.TED
                        )
                        .map(
                                this::mapRun
                        )
                        .orElse(
                                null
                        );


        return new TedDataSourceStatusResponse(

                "TED",

                "v3",

                settings.baseUrl(),

                settings.searchPath(),

                false,

                settings.manualSyncEnabled(),

                count,

                lastSeen,

                lastRun

        );

    }


    private TedDataSourceStatusResponse.LastRunResponse mapRun(
            IngestionRunEntity run
    ) {

        return new TedDataSourceStatusResponse
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
