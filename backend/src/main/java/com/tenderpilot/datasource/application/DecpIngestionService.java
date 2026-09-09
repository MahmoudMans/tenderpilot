package com.tenderpilot.datasource.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.datasource.api.DecpSyncResponse;

import com.tenderpilot.datasource.decp.DecpAwardMapper;
import com.tenderpilot.datasource.decp.DecpAwardRecord;
import com.tenderpilot.datasource.decp.DecpCanonicalContractImportService;
import com.tenderpilot.datasource.decp.DecpMappedContract;
import com.tenderpilot.datasource.decp.DecpQueryFactory;
import com.tenderpilot.datasource.decp.DecpSearchGateway;
import com.tenderpilot.datasource.decp.DecpSearchPage;
import com.tenderpilot.datasource.decp.DecpSearchRequest;
import com.tenderpilot.datasource.decp.DecpSettings;

import com.tenderpilot.datasource.domain.IngestionRunEntity;
import com.tenderpilot.datasource.infrastructure.IngestionRunRepository;

import com.tenderpilot.procurement.domain.ProcurementSource;

import org.springframework.stereotype.Service;

@Service
public class DecpIngestionService {

    private final DecpSearchGateway searchGateway;

    private final DecpQueryFactory queryFactory;

    private final DecpAwardMapper mapper;

    private final DecpCanonicalContractImportService
            canonicalContractImportService;

    private final IngestionRunRepository
            ingestionRunRepository;

    private final DecpSettings settings;

    public DecpIngestionService(
            DecpSearchGateway searchGateway,
            DecpQueryFactory queryFactory,
            DecpAwardMapper mapper,
            DecpCanonicalContractImportService
                    canonicalContractImportService,
            IngestionRunRepository ingestionRunRepository,
            DecpSettings settings
    ) {

        this.searchGateway = searchGateway;
        this.queryFactory = queryFactory;
        this.mapper = mapper;

        this.canonicalContractImportService =
                canonicalContractImportService;

        this.ingestionRunRepository =
                ingestionRunRepository;

        this.settings = settings;
    }

    public DecpSyncResponse syncRecent(
            int lookbackDays,
            int requestedLimit
    ) {

        if (!settings.manualSyncEnabled()) {
            throw new InvalidRequestException(
                    "Manual DECP synchronization is disabled"
            );
        }

        int days =
                Math.max(
                        1,
                        Math.min(
                                31,
                                lookbackDays
                        )
                );

        int limit =
                Math.max(
                        1,
                        Math.min(
                                requestedLimit,
                                settings.maxLimit()
                        )
                );

        String where =
                queryFactory
                        .recentPublications(days);

        IngestionRunEntity run =
                ingestionRunRepository
                        .saveAndFlush(
                                IngestionRunEntity
                                        .start(
                                                ProcurementSource.DECP,
                                                where,
                                                limit
                                        )
                        );

        try {

            DecpSearchPage page =
                    searchGateway
                            .search(
                                    new DecpSearchRequest(
                                            where,
                                            0,
                                            limit
                                    )
                            );

            int processed = 0;
            int failed = 0;
            String lastError = null;

            for (
                    DecpAwardRecord sourceRecord :
                    page.records()
            ) {

                try {

                    DecpMappedContract mapped =
                            mapper.map(sourceRecord);

                    canonicalContractImportService
                            .upsert(mapped);

                    processed++;

                } catch (
                        RuntimeException exception
                ) {

                    failed++;

                    lastError =
                            safeMessage(exception);
                }
            }

            run.complete(
                    page.records().size(),
                    processed,
                    failed,
                    lastError
            );

            run =
                    ingestionRunRepository
                            .saveAndFlush(run);

            return DecpSyncResponse.from(run);

        } catch (
                RuntimeException exception
        ) {

            run.fail(
                    safeMessage(exception)
            );

            ingestionRunRepository
                    .saveAndFlush(run);

            throw exception;
        }
    }

    public DecpSyncResponse syncDefaults() {

        return syncRecent(
                settings.defaultLookbackDays(),
                settings.defaultLimit()
        );
    }

    private String safeMessage(
            Throwable throwable
    ) {

        if (throwable == null) {
            return "Unknown DECP ingestion error";
        }

        String message =
                throwable.getMessage();

        return message == null
                ||
                message.isBlank()
                ? throwable
                        .getClass()
                        .getSimpleName()
                : message;
    }
}
