package com.tenderpilot.datasource.application;

import com.tenderpilot.datasource.api.BoampDataSourceStatusResponse;

import com.tenderpilot.datasource.boamp.BoampSettings;

import com.tenderpilot.datasource.domain.IngestionRunEntity;

import com.tenderpilot.datasource.infrastructure.IngestionRunRepository;

import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.TenderSourceRecordEntity;

import com.tenderpilot.procurement.infrastructure.TenderSourceRecordRepository;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoampDataSourceService {

    private final BoampSettings
            settings;


    private final TenderSourceRecordRepository
            sourceRecordRepository;


    private final IngestionRunRepository
            ingestionRunRepository;


    public BoampDataSourceService(

            BoampSettings settings,

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
    public BoampDataSourceStatusResponse status() {

        long count =

                sourceRecordRepository
                        .countBySource(
                                ProcurementSource.BOAMP
                        );


        Instant lastSeen =

                sourceRecordRepository
                        .findTop1BySourceOrderByLastSeenAtDesc(
                                ProcurementSource.BOAMP
                        )
                        .map(
                                TenderSourceRecordEntity
                                        ::getLastSeenAt
                        )
                        .orElse(
                                null
                        );


        BoampDataSourceStatusResponse.LastRunResponse
                lastRun =

                ingestionRunRepository
                        .findTop1BySourceOrderByStartedAtDesc(
                                ProcurementSource.BOAMP
                        )
                        .map(
                                this::mapRun
                        )
                        .orElse(
                                null
                        );


        return new BoampDataSourceStatusResponse(

                "BOAMP",

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


    private BoampDataSourceStatusResponse.LastRunResponse mapRun(
            IngestionRunEntity run
    ) {

        return new BoampDataSourceStatusResponse
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
