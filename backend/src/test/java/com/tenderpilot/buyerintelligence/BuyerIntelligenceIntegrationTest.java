package com.tenderpilot.buyerintelligence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.buyerintelligence.application.BuyerIntelligenceService;

import com.tenderpilot.common.domain.ResourceNotFoundException;

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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_buyer_intelligence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "tenderpilot.search.enabled=false",
                "tenderpilot.search.auto-index=false",
                "tenderpilot.local-ai.enabled=false",
                "tenderpilot.rag.enabled=false"
        }
)
@ActiveProfiles(
        "test"
)
@Transactional
class BuyerIntelligenceIntegrationTest {

    @Autowired
    private CanonicalTenderImportService
            tenderImportService;


    @Autowired
    private BuyerIntelligenceService
            intelligenceService;


    @Test
    void buyerSearchUsesCanonicalPublicProcurementCorpus() {

        seedTender(
                "BUYER-INTELLIGENCE-FR-1",
                "BUYER-PARIS-001",
                "Ville Intelligence Paris",
                "FR",
                "Paris"
        );


        var buyers =
                intelligenceService
                        .search(
                                "Intelligence Paris",
                                "FR",
                                20
                        );


        assertThat(
                buyers
        )
                .hasSize(
                        1
                );


        assertThat(
                buyers
                        .getFirst()
                        .name()
        )
                .isEqualTo(
                        "Ville Intelligence Paris"
                );


        assertThat(
                buyers
                        .getFirst()
                        .noticeCount()
        )
                .isEqualTo(
                        1
                );
    }


    @Test
    void buyerDetailIncludesCanonicalTenderHistory() {

        seedTender(
                "BUYER-INTELLIGENCE-DETAIL-1",
                "BUYER-DETAIL-001",
                "Métropole Buyer Detail",
                "FR",
                "Lyon"
        );


        var buyer =
                intelligenceService
                        .search(
                                "Métropole Buyer Detail",
                                "FR",
                                20
                        )
                        .getFirst();


        var detail =
                intelligenceService
                        .detail(
                                buyer.buyerId()
                        );


        assertThat(
                detail.summary().buyerId()
        )
                .isEqualTo(
                        buyer.buyerId()
                );


        assertThat(
                detail.recentTenders()
        )
                .hasSize(
                        1
                );


        assertThat(
                detail
                        .recentTenders()
                        .getFirst()
                        .title()
        )
                .contains(
                        "BUYER-INTELLIGENCE-DETAIL-1"
                );


        assertThat(
                detail.summary()
                        .medianResponseDays()
        )
                .isNotNull();
    }


    @Test
    void countryFilterDoesNotReturnDifferentCountryBuyer() {

        seedTender(
                "BUYER-INTELLIGENCE-FR-2",
                "BUYER-FRANCE-002",
                "Buyer Country Filter France",
                "FR",
                "Paris"
        );


        seedTender(
                "BUYER-INTELLIGENCE-BE-2",
                "BUYER-BELGIUM-002",
                "Buyer Country Filter Belgium",
                "BE",
                "Brussels"
        );


        var france =
                intelligenceService
                        .search(
                                "Buyer Country Filter",
                                "FR",
                                20
                        );


        assertThat(
                france
        )
                .hasSize(
                        1
                );


        assertThat(
                france
                        .getFirst()
                        .name()
        )
                .isEqualTo(
                        "Buyer Country Filter France"
                );
    }


    @Test
    void unknownBuyerReturnsResourceNotFound() {

        assertThatThrownBy(
                () ->
                        intelligenceService
                                .detail(
                                        UUID.fromString(
                                                "99999999-9999-9999-9999-999999999999"
                                        )
                                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );
    }


    private UUID seedTender(
            String externalId,
            String buyerExternalId,
            String buyerName,
            String country,
            String city
    ) {

        return tenderImportService
                .upsert(
                        new CanonicalTenderUpsertRequest(
                                ProcurementSource.MANUAL_DEMO,

                                externalId,

                                "1",

                                "BUYER_INTELLIGENCE_TEST",

                                "https://example.test/"
                                        +
                                        externalId,

                                "buyer-intelligence-checksum-"
                                        +
                                        externalId,

                                Instant.parse(
                                        "2026-09-10T08:00:00Z"
                                ),

                                new CanonicalTenderUpsertRequest
                                        .BuyerInput(
                                                buyerExternalId,

                                                buyerName,

                                                BuyerCategory.LOCAL_AUTHORITY,

                                                country,

                                                city,

                                                null,

                                                null
                                        ),

                                "Buyer intelligence tender "
                                        +
                                        externalId,

                                "Buyer behavior analytics integration fixture.",

                                ProcurementStatus.PUBLISHED,

                                ProcurementProcedure.OPEN,

                                ContractNature.WORKS,

                                LocalDate.of(
                                        2026,
                                        9,
                                        10
                                ),

                                Instant.parse(
                                        "2026-10-10T16:00:00Z"
                                ),

                                new BigDecimal(
                                        "1000000"
                                ),

                                new BigDecimal(
                                        "2000000"
                                ),

                                "EUR",

                                country,

                                country.equals(
                                        "FR"
                                )
                                        ? "IDF"
                                        : null,

                                country.equals(
                                        "FR"
                                )
                                        ? "Île-de-France"
                                        : null,

                                false,

                                false,

                                true,

                                List.of(
                                        new CanonicalTenderUpsertRequest
                                                .CpvInput(
                                                        "45453000",
                                                        "Overhaul and refurbishment work"
                                                )
                                ),

                                List.of(
                                        new CanonicalTenderUpsertRequest
                                                .LocationInput(
                                                        country,
                                                        country.equals(
                                                                "FR"
                                                        )
                                                                ? "IDF"
                                                                : null,
                                                        country.equals(
                                                                "FR"
                                                        )
                                                                ? "Île-de-France"
                                                                : null,
                                                        city
                                                )
                                ),

                                List.of()
                        )
                );
    }

}
