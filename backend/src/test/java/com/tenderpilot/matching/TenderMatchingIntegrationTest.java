package com.tenderpilot.matching;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.matching.api.TenderMatchRecalculationResponse;
import com.tenderpilot.matching.api.TenderMatchSummaryResponse;

import com.tenderpilot.matching.application.CompanyMatchProfileProvider;
import com.tenderpilot.matching.application.TenderMatchingService;

import com.tenderpilot.matching.domain.CompanyMatchProfile;
import com.tenderpilot.matching.domain.TenderMatchGrade;

import com.tenderpilot.matching.infrastructure.TenderMatchRepository;

import com.tenderpilot.procurement.application.CanonicalTenderImportService;
import com.tenderpilot.procurement.application.CanonicalTenderUpsertRequest;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_matching;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false"
        }
)
@ActiveProfiles(
        "test"
)
@Import(
        TenderMatchingIntegrationTest
                .FakeProfileConfiguration.class
)
@Transactional
class TenderMatchingIntegrationTest {

    private static final UUID ORGANIZATION =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000010"
            );


    private static final UUID OTHER_ORGANIZATION =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000099"
            );


    @Autowired
    private CanonicalTenderImportService
            tenderImportService;


    @Autowired
    private TenderMatchingService
            matchingService;


    @Autowired
    private TenderMatchRepository
            matchRepository;


    @BeforeEach
    void cleanMatches() {

        matchRepository.deleteAll();
        matchRepository.flush();
    }


    @Test
    void recalculationPersistsRankedTenantScopedMatches() {

        UUID ideal =
                tenderImportService
                        .upsert(
                                request(
                                        "MATCH-IDEAL",
                                        "Energy renovation of public school",
                                        "45453000",
                                        "IDF",
                                        "Île-de-France",
                                        BuyerCategory.LOCAL_AUTHORITY,
                                        new BigDecimal(
                                                "1500000"
                                        )
                                )
                        );


        tenderImportService
                .upsert(
                        request(
                                "MATCH-WEAK",
                                "Enterprise software support services",
                                "72000000",
                                "PACA",
                                "Provence-Alpes-Côte d'Azur",
                                BuyerCategory.PUBLIC_AGENCY,
                                new BigDecimal(
                                        "9000000"
                                )
                        )
                );


        TenderMatchRecalculationResponse recalculated =
                matchingService
                        .recalculate(
                                ORGANIZATION
                        );


        assertThat(
                recalculated.evaluatedCount()
        )
                .isEqualTo(
                        2
                );


        List<TenderMatchSummaryResponse> matches =
                matchingService
                        .list(
                                ORGANIZATION,
                                null,
                                null,
                                false
                        );


        assertThat(
                matches
        )
                .hasSize(
                        2
                );


        assertThat(
                matches
                        .getFirst()
                        .opportunity()
                        .id()
        )
                .isEqualTo(
                        ideal
                );


        assertThat(
                matches
                        .getFirst()
                        .score()
        )
                .isGreaterThan(
                        matches
                                .get(1)
                                .score()
                );


        assertThat(
                matchingService
                        .list(
                                OTHER_ORGANIZATION,
                                null,
                                null,
                                true
                        )
        )
                .isEmpty();
    }


    @Test
    void minimumScoreAndGradeFiltersAreSupported() {

        tenderImportService
                .upsert(
                        request(
                                "MATCH-FILTER",
                                "Energy renovation programme",
                                "45453000",
                                "IDF",
                                "Île-de-France",
                                BuyerCategory.LOCAL_AUTHORITY,
                                new BigDecimal(
                                        "1200000"
                                )
                        )
                );


        matchingService
                .recalculate(
                        ORGANIZATION
                );


        List<TenderMatchSummaryResponse> matches =
                matchingService
                        .list(
                                ORGANIZATION,
                                85,
                                TenderMatchGrade.EXCELLENT,
                                false
                        );


        assertThat(
                matches
        )
                .hasSize(
                        1
                );


        assertThat(
                matches
                        .getFirst()
                        .score()
        )
                .isGreaterThanOrEqualTo(
                        85
                );
    }


    private CanonicalTenderUpsertRequest request(
            String externalId,
            String title,
            String cpv,
            String regionCode,
            String regionName,
            BuyerCategory buyerCategory,
            BigDecimal value
    ) {

        return new CanonicalTenderUpsertRequest(
                ProcurementSource.MANUAL_DEMO,

                externalId,

                "1",

                "MATCH_TEST",

                "https://example.test/"
                        +
                        externalId,

                "match-checksum-"
                        +
                        externalId,

                Instant.parse(
                        "2026-09-10T08:00:00Z"
                ),

                new CanonicalTenderUpsertRequest
                        .BuyerInput(
                                "MATCH-BUYER:"
                                        +
                                        externalId,
                                buyerCategory
                                        ==
                                        BuyerCategory.LOCAL_AUTHORITY
                                        ? "Ville de Paris"
                                        : "Public Agency",
                                buyerCategory,
                                "FR",
                                "Paris",
                                null,
                                null
                        ),

                title,

                title
                        +
                        " procurement description",

                ProcurementStatus.PUBLISHED,

                ProcurementProcedure.OPEN,

                cpv.startsWith(
                        "45"
                )
                        ? ContractNature.WORKS
                        : ContractNature.SERVICES,

                LocalDate.of(
                        2026,
                        9,
                        10
                ),

                Instant.parse(
                        "2026-10-25T16:00:00Z"
                ),

                value,

                value,

                "EUR",

                "FR",

                regionCode,

                regionName,

                false,

                false,

                true,

                List.of(
                        new CanonicalTenderUpsertRequest
                                .CpvInput(
                                        cpv,
                                        cpv.equals(
                                                "45453000"
                                        )
                                                ? "Energy renovation and refurbishment"
                                                : "IT services"
                                )
                ),

                List.of(
                        new CanonicalTenderUpsertRequest
                                .LocationInput(
                                        "FR",
                                        regionCode,
                                        regionName,
                                        "Paris"
                                )
                ),

                List.of()
        );
    }


    @TestConfiguration
    static class FakeProfileConfiguration {

        @Bean
        @Primary
        CompanyMatchProfileProvider
        companyMatchProfileProvider() {

            return organizationId ->
                    new CompanyMatchProfile(
                            organizationId,

                            List.of(
                                    "energy renovation"
                            ),

                            List.of(
                                    new CompanyMatchProfile
                                            .OperatingRegion(
                                                    "FR",
                                                    "IDF",
                                                    "Île-de-France"
                                            )
                            ),

                            List.of(
                                    "45453000"
                            ),

                            List.of(
                                    "LOCAL_AUTHORITY"
                            ),

                            List.of(
                                    "asbestos"
                            ),

                            new BigDecimal(
                                    "500000"
                            ),

                            new BigDecimal(
                                    "3000000"
                            ),

                            "EUR",

                            14
                    );
        }
    }

}
