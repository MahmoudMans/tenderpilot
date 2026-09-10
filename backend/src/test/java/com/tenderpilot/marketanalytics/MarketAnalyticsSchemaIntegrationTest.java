package com.tenderpilot.marketanalytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.marketanalytics.application.MarketAnalyticsService;

import com.tenderpilot.marketanalytics.domain.MarketQuery;

import com.tenderpilot.marketanalytics.infrastructure.CanonicalMarketAnalyticsGateway;
import com.tenderpilot.marketanalytics.infrastructure.MarketAnalyticsSchemaInspector;

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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tenderpilot_market_schema;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
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
class MarketAnalyticsSchemaIntegrationTest {

    @Autowired
    private MarketAnalyticsSchemaInspector
            schemaInspector;


    @Autowired
    private CanonicalMarketAnalyticsGateway
            analyticsGateway;


    @Autowired
    private CanonicalTenderImportService
            tenderImportService;


    @Autowired
    private MarketAnalyticsService
            marketAnalyticsService;


    @Test
    void canonicalSchemaExposesMarketAnalyticsCapabilities() {

        var schema =
                schemaInspector.schema();


        var capabilities =
                analyticsGateway.capabilities();


        assertThat(
                schema.base().tenderTable()
        )
                .isEqualToIgnoringCase(
                        "procurement_tenders"
                );


        assertThat(
                schema.locationTable()
        )
                .isEqualToIgnoringCase(
                        "procurement_tender_locations"
                );


        assertThat(
                capabilities.countryAvailable()
        )
                .isTrue();


        assertThat(
                capabilities.regionAvailable()
        )
                .isTrue();


        assertThat(
                capabilities.cpvAvailable()
        )
                .isTrue();


        assertThat(
                capabilities.procedureAvailable()
        )
                .isTrue();


        assertThat(
                capabilities.contractNatureAvailable()
        )
                .isTrue();
    }


    @Test
    void canonicalGatewayFiltersTenderByCountryRegionCpvAndStatus() {

        seed(
                "MARKET-SCHEMA-FILTER-001"
        );


        var tenders =
                analyticsGateway
                        .tenders(
                                new MarketQuery(
                                        "MARKET-SCHEMA-FILTER-001",
                                        "FR",
                                        "IDF",
                                        "4545",
                                        "PUBLISHED",
                                        LocalDate.of(
                                                2026,
                                                9,
                                                1
                                        ),
                                        LocalDate.of(
                                                2026,
                                                9,
                                                30
                                        )
                                )
                        );


        assertThat(
                tenders
        )
                .hasSize(
                        1
                );


        assertThat(
                tenders
                        .getFirst()
                        .procedure()
        )
                .isEqualTo(
                        "OPEN"
                );


        assertThat(
                tenders
                        .getFirst()
                        .contractNature()
        )
                .isEqualTo(
                        "WORKS"
                );
    }


    @Test
    void marketServiceBuildsOverviewFromCanonicalImportedTender() {

        seed(
                "MARKET-SERVICE-OVERVIEW-001"
        );


        var result =
                marketAnalyticsService
                        .explore(
                                "MARKET-SERVICE-OVERVIEW-001",
                                "FR",
                                "IDF",
                                "4545",
                                "PUBLISHED",
                                null,
                                null
                        );


        assertThat(
                result.summary().noticeCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.summary().uniqueBuyerCount()
        )
                .isEqualTo(
                        1
                );


        assertThat(
                result.topCpvs()
        )
                .hasSize(
                        1
                );


        assertThat(
                result.topCpvs()
                        .getFirst()
                        .sharePercentage()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.geographies()
        )
                .isNotEmpty();
    }


    private void seed(
            String externalId
    ) {

        tenderImportService
                .upsert(
                        new CanonicalTenderUpsertRequest(
                                ProcurementSource.MANUAL_DEMO,

                                externalId,

                                "1",

                                "MARKET_ANALYTICS_TEST",

                                "https://example.test/"
                                        +
                                        externalId,

                                "market-checksum-"
                                        +
                                        externalId,

                                Instant.parse(
                                        "2026-09-10T08:00:00Z"
                                ),

                                new CanonicalTenderUpsertRequest
                                        .BuyerInput(
                                                "MARKET-BUYER-"
                                                        +
                                                        externalId,

                                                "Market Analytics Buyer",

                                                BuyerCategory.LOCAL_AUTHORITY,

                                                "FR",

                                                "Paris",

                                                null,

                                                null
                                        ),

                                "Market analytics opportunity "
                                        +
                                        externalId,

                                "Public procurement market analytics fixture.",

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

                                "FR",

                                "IDF",

                                "Île-de-France",

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
                                                        "FR",
                                                        "IDF",
                                                        "Île-de-France",
                                                        "Paris"
                                                )
                                ),

                                List.of()
                        )
                );
    }

}
