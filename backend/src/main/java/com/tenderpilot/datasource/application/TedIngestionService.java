package com.tenderpilot.datasource.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.datasource.api.TedSyncResponse;

import com.tenderpilot.datasource.domain.IngestionRunEntity;

import com.tenderpilot.datasource.infrastructure.IngestionRunRepository;

import com.tenderpilot.datasource.ted.TedNoticeMapper;
import com.tenderpilot.datasource.ted.TedQueryFactory;
import com.tenderpilot.datasource.ted.TedSearchGateway;
import com.tenderpilot.datasource.ted.TedSearchNotice;
import com.tenderpilot.datasource.ted.TedSearchPage;
import com.tenderpilot.datasource.ted.TedSearchRequest;
import com.tenderpilot.datasource.ted.TedSettings;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;

import com.tenderpilot.procurement.domain.ProcurementSource;

import org.springframework.stereotype.Service;

@Service
public class TedIngestionService {

    private final TedSearchGateway
            searchGateway;


    private final TedQueryFactory
            queryFactory;


    private final TedNoticeMapper
            noticeMapper;


    private final CanonicalTenderImportService
            canonicalImportService;


    private final IngestionRunRepository
            ingestionRunRepository;


    private final TedSettings
            settings;


    public TedIngestionService(

            TedSearchGateway searchGateway,

            TedQueryFactory queryFactory,

            TedNoticeMapper noticeMapper,

            CanonicalTenderImportService canonicalImportService,

            IngestionRunRepository ingestionRunRepository,

            TedSettings settings

    ) {

        this.searchGateway =
                searchGateway;

        this.queryFactory =
                queryFactory;

        this.noticeMapper =
                noticeMapper;

        this.canonicalImportService =
                canonicalImportService;

        this.ingestionRunRepository =
                ingestionRunRepository;

        this.settings =
                settings;

    }


    public TedSyncResponse syncRecent(

            int lookbackDays,

            int requestedLimit

    ) {

        if (
                !settings.manualSyncEnabled()
        ) {

            throw new InvalidRequestException(
                    "Manual TED synchronization is disabled"
            );

        }


        int days =

                Math.max(

                        1,

                        Math.min(
                                lookbackDays,
                                31
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


        String query =

                queryFactory
                        .recentPublications(
                                days
                        );


        IngestionRunEntity run =

                ingestionRunRepository
                        .saveAndFlush(

                                IngestionRunEntity
                                        .start(

                                                ProcurementSource.TED,

                                                query,

                                                limit

                                        )

                        );


        try {

            TedSearchPage page =

                    searchGateway
                            .search(

                                    new TedSearchRequest(

                                            query,

                                            1,

                                            limit

                                    )

                            );


            int processed =
                    0;


            int failed =
                    0;


            String lastError =
                    null;


            for (
                    TedSearchNotice notice :
                    page.notices()
            ) {

                try {

                    canonicalImportService
                            .upsert(

                                    noticeMapper
                                            .map(
                                                    notice
                                            )

                            );


                    processed++;


                } catch (
                        RuntimeException exception
                ) {

                    failed++;


                    lastError =
                            safeMessage(
                                    exception
                            );

                }

            }


            run.complete(

                    page.notices()
                            .size(),

                    processed,

                    failed,

                    lastError

            );


            run =
                    ingestionRunRepository
                            .saveAndFlush(
                                    run
                            );


            return TedSyncResponse
                    .from(
                            run
                    );


        } catch (
                RuntimeException exception
        ) {

            run.fail(
                    safeMessage(
                            exception
                    )
            );


            ingestionRunRepository
                    .saveAndFlush(
                            run
                    );


            throw exception;

        }

    }


    public TedSyncResponse syncDefaults() {

        return syncRecent(

                settings.defaultLookbackDays(),

                settings.defaultLimit()

        );

    }


    private String safeMessage(
            Throwable throwable
    ) {

        if (
                throwable == null
        ) {

            return "Unknown ingestion error";

        }


        String message =
                throwable.getMessage();


        if (
                message == null
                ||
                message.isBlank()
        ) {

            return throwable
                    .getClass()
                    .getSimpleName();

        }


        return message;

    }

}
