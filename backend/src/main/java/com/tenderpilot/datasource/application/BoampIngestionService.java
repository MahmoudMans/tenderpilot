package com.tenderpilot.datasource.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.datasource.api.BoampSyncResponse;

import com.tenderpilot.datasource.boamp.BoampNotice;
import com.tenderpilot.datasource.boamp.BoampNoticeMapper;
import com.tenderpilot.datasource.boamp.BoampQueryFactory;
import com.tenderpilot.datasource.boamp.BoampSearchGateway;
import com.tenderpilot.datasource.boamp.BoampSearchPage;
import com.tenderpilot.datasource.boamp.BoampSearchRequest;
import com.tenderpilot.datasource.boamp.BoampSettings;

import com.tenderpilot.datasource.domain.IngestionRunEntity;

import com.tenderpilot.datasource.infrastructure.IngestionRunRepository;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;

import com.tenderpilot.procurement.domain.ProcurementSource;

import org.springframework.stereotype.Service;

@Service
public class BoampIngestionService {

    private final BoampSearchGateway
            searchGateway;


    private final BoampQueryFactory
            queryFactory;


    private final BoampNoticeMapper
            noticeMapper;


    private final CanonicalTenderImportService
            canonicalImportService;


    private final IngestionRunRepository
            ingestionRunRepository;


    private final BoampSettings
            settings;


    public BoampIngestionService(

            BoampSearchGateway searchGateway,

            BoampQueryFactory queryFactory,

            BoampNoticeMapper noticeMapper,

            CanonicalTenderImportService canonicalImportService,

            IngestionRunRepository ingestionRunRepository,

            BoampSettings settings

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


    public BoampSyncResponse syncRecent(

            int lookbackDays,

            int requestedLimit

    ) {

        if (
                !settings.manualSyncEnabled()
        ) {

            throw new InvalidRequestException(
                    "Manual BOAMP synchronization is disabled"
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
                        .recentPublications(
                                days
                        );


        IngestionRunEntity run =

                ingestionRunRepository
                        .saveAndFlush(

                                IngestionRunEntity
                                        .start(

                                                ProcurementSource.BOAMP,

                                                where,

                                                limit

                                        )

                        );


        try {

            BoampSearchPage page =

                    searchGateway
                            .search(

                                    new BoampSearchRequest(

                                            where,

                                            0,

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
                    BoampNotice notice :
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


            return BoampSyncResponse
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


    public BoampSyncResponse syncDefaults() {

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

            return "Unknown BOAMP ingestion error";

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
