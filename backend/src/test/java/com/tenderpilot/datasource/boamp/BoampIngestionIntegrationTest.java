package com.tenderpilot.datasource.boamp;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.datasource.api.BoampSyncResponse;

import com.tenderpilot.datasource.application.BoampIngestionService;

import com.tenderpilot.datasource.domain.IngestionRunStatus;

import com.tenderpilot.datasource.infrastructure.IngestionRunRepository;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import com.tenderpilot.procurement.application.OpportunityQueryService;

import com.tenderpilot.procurement.domain.ProcurementSource;

import com.tenderpilot.procurement.infrastructure.TenderSourceRecordRepository;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

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
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_boamp_ingestion;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        BoampIngestionIntegrationTest
                .FakeBoampGatewayConfiguration.class
)
@Transactional
class BoampIngestionIntegrationTest {

    @Autowired
    private BoampIngestionService
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
    void syncImportsBoampNoticeIntoCanonicalCatalogue() {

        BoampSyncResponse response =

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

                                "BOAMP integration fixture",

                                null,

                                ProcurementSource.BOAMP

                        );


        assertThat(
                opportunities
        )
                .hasSize(
                        1
                );


        assertThat(
                opportunities
                        .getFirst()
                        .primaryRegionName()
        )
                .isEqualTo(
                        "Île-de-France"
                );


        assertThat(
                sourceRecordRepository
                        .countBySource(
                                ProcurementSource.BOAMP
                        )
        )
                .isEqualTo(
                        1
                );


        assertThat(
                ingestionRunRepository
                        .findTop1BySourceOrderByStartedAtDesc(
                                ProcurementSource.BOAMP
                        )
        )
                .isPresent();

    }


    @TestConfiguration
    static class FakeBoampGatewayConfiguration {

        @Bean
        @Primary
        BoampSearchGateway boampSearchGateway() {

            return request ->

                    new BoampSearchPage(

                            1,

                            List.of(

                                    new BoampNotice(

                                            "26-INTEGRATION",

                                            "26_INTEGRATION",

                                            "BOAMP integration fixture renovation",

                                            LocalDate.of(
                                                    2026,
                                                    9,
                                                    9
                                            ),

                                            Instant.parse(
                                                    "2026-10-20T14:00:00Z"
                                            ),

                                            "Ville de Paris",

                                            "Appel d'offres ouvert",

                                            "Travaux",

                                            "Avis de marché",

                                            "eForms",

                                            "https://www.boamp.fr/avis/detail/26-INTEGRATION",

                                            "75",

                                            List.of(
                                                    "45453000"
                                            ),

                                            new BigDecimal(
                                                    "1200000"
                                            ),

                                            "EUR"

                                    )

                            )

                    );

        }

    }

}
