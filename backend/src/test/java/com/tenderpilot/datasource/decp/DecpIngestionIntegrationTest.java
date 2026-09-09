package com.tenderpilot.datasource.decp;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.datasource.api.DecpSyncResponse;
import com.tenderpilot.datasource.application.DecpIngestionService;
import com.tenderpilot.datasource.domain.IngestionRunStatus;

import com.tenderpilot.procurement.api.OpportunityDetailResponse;
import com.tenderpilot.procurement.api.OpportunitySummaryResponse;
import com.tenderpilot.procurement.application.OpportunityQueryService;
import com.tenderpilot.procurement.award.infrastructure.TenderAwardRepository;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.infrastructure.TenderSourceRecordRepository;

import java.math.BigDecimal;
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
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_decp_ingestion;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        }
)
@ActiveProfiles("test")
@Import(
        DecpIngestionIntegrationTest
                .FakeDecpGatewayConfiguration.class
)
@Transactional
class DecpIngestionIntegrationTest {

    @Autowired
    private DecpIngestionService
            ingestionService;

    @Autowired
    private OpportunityQueryService
            opportunityQueryService;

    @Autowired
    private TenderAwardRepository
            awardRepository;

    @Autowired
    private TenderSourceRecordRepository
            sourceRecordRepository;

    @Test
    void importsAwardWithoutUsingAmountAsEstimate() {

        DecpSyncResponse result =
                ingestionService
                        .syncRecent(
                                7,
                                25
                        );

        assertThat(result.status())
                .isEqualTo(
                        IngestionRunStatus.COMPLETED
                );

        assertThat(result.processedCount())
                .isEqualTo(1);

        assertThat(result.failedCount())
                .isZero();

        List<OpportunitySummaryResponse> opportunities =
                opportunityQueryService
                        .list(
                                "DECP integration award",
                                null,
                                ProcurementSource.DECP
                        );

        assertThat(opportunities)
                .hasSize(1);

        OpportunitySummaryResponse summary =
                opportunities.getFirst();

        assertThat(
                summary.estimatedMinValue()
        )
                .isNull();

        assertThat(
                summary.estimatedMaxValue()
        )
                .isNull();

        assertThat(
                summary.awardedAmount()
        )
                .isEqualByComparingTo(
                        "1850000"
                );

        OpportunityDetailResponse detail =
                opportunityQueryService
                        .detail(summary.id());

        assertThat(detail.award())
                .isNotNull();

        assertThat(
                detail.award()
                        .awardedAmount()
        )
                .isEqualByComparingTo(
                        "1850000"
                );

        assertThat(
                detail.award()
                        .suppliers()
        )
                .hasSize(1);

        assertThat(
                detail.award()
                        .suppliers()
                        .getFirst()
                        .legalName()
        )
                .isEqualTo(
                        "DECP Supplier SAS"
                );

        assertThat(awardRepository.count())
                .isEqualTo(1);

        assertThat(
                sourceRecordRepository
                        .countBySource(
                                ProcurementSource.DECP
                        )
        )
                .isEqualTo(1);
    }

    @TestConfiguration
    static class FakeDecpGatewayConfiguration {

        @Bean
        @Primary
        DecpSearchGateway decpSearchGateway() {

            return request ->
                    new DecpSearchPage(
                            1,
                            List.of(
                                    new DecpAwardRecord(
                                            "DECP-INTEGRATION-001",
                                            "Marché",
                                            "DECP integration award renovation",
                                            "45453000-7",
                                            "Appel d'offres ouvert",
                                            "21750001600019",
                                            "Ville de Paris",
                                            18,
                                            LocalDate.of(2026, 9, 1),
                                            LocalDate.of(2026, 9, 8),
                                            new BigDecimal("1850000"),
                                            "Révisable",
                                            4,
                                            null,
                                            "75101",
                                            "Code commune",
                                            "Paris",
                                            "Insertion",
                                            "Performance énergétique",
                                            null,
                                            null,
                                            "Non",
                                            "integration-fixture",
                                            List.of(
                                                    new DecpSupplierData(
                                                            "12345678900011",
                                                            "SIRET",
                                                            "DECP Supplier SAS"
                                                    )
                                            )
                                    )
                            )
                    );
        }
    }
}
