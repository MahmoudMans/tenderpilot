package com.tenderpilot.datasource.ted;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.datasource.api.TedSyncResponse;

import com.tenderpilot.datasource.application.TedIngestionService;

import com.tenderpilot.datasource.domain.IngestionRunStatus;

import com.tenderpilot.datasource.infrastructure.IngestionRunRepository;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import com.tenderpilot.procurement.application.OpportunityQueryService;

import com.tenderpilot.procurement.domain.ProcurementSource;

import com.tenderpilot.procurement.infrastructure.TenderSourceRecordRepository;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_ted_ingestion;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        TedIngestionIntegrationTest
                .FakeTedGatewayConfiguration.class
)
@Transactional
class TedIngestionIntegrationTest {

    @Autowired
    private TedIngestionService
            ingestionService;


    @Autowired
    private OpportunityQueryService
            opportunityQueryService;


    @Autowired
    private TenderSourceRecordRepository
            sourceRecordRepository;


    @Autowired
    private IngestionRunRepository
            ingestionRunRepository;


    @Test
    void syncImportsTedNoticeIntoSharedCanonicalCatalogueAndPersistsRun() {

        TedSyncResponse response =

                ingestionService
                        .syncRecent(
                                7,
                                25
                        );


        assertThat(
                response.status()
        )
                .isEqualTo(
                        IngestionRunStatus.COMPLETED
                );


        assertThat(
                response.processedCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                response.failedCount()
        )
                .isZero();


        List<OpportunitySummaryResponse> opportunities =

                opportunityQueryService
                        .list(

                                "TED ingestion integration",

                                null,

                                ProcurementSource.TED

                        );


        assertThat(
                opportunities
        )
                .hasSize(
                        1
                );


        assertThat(
                sourceRecordRepository
                        .countBySource(
                                ProcurementSource.TED
                        )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                ingestionRunRepository
                        .findTop1BySourceOrderByStartedAtDesc(
                                ProcurementSource.TED
                        )
        )
                .isPresent();

    }


    @TestConfiguration
    static class FakeTedGatewayConfiguration {

        @Bean
        @Primary
        TedSearchGateway tedSearchGateway() {

            return request ->

                    new TedSearchPage(

                            1,

                            List.of(

                                    new TedSearchNotice(

                                            "999999-2026",

                                            "01",

                                            "cn-standard",

                                            "TED ingestion integration opportunity",

                                            "Canonical ingestion integration fixture.",

                                            LocalDate.of(
                                                    2026,
                                                    9,
                                                    9
                                            ),

                                            new TedBuyerData(

                                                    "Integration Municipality",

                                                    "FR-INTEGRATION-001",

                                                    "FRA",

                                                    "Paris",

                                                    "https://example.test",

                                                    "rauth"

                                            ),

                                            "open",

                                            "works",

                                            List.of(
                                                    LocalDate.of(
                                                            2026,
                                                            10,
                                                            20
                                                    )
                                            ),

                                            LocalTime.of(
                                                    16,
                                                    0
                                            ),

                                            new BigDecimal(
                                                    "1200000"
                                            ),

                                            "EUR",

                                            List.of(
                                                    "45453000"
                                            ),

                                            List.of(

                                                    new TedLocationData(

                                                            "FRA",

                                                            "FR101",

                                                            "Paris"

                                                    )

                                            ),

                                            false,

                                            true,

                                            "https://ted.europa.eu/en/notice/-/detail/999999-2026"

                                    )

                            )

                    );

        }

    }

}
