package com.tenderpilot.marketanalytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.marketanalytics.application.MarketAnalyticsGateway;
import com.tenderpilot.marketanalytics.application.MarketAnalyticsService;
import com.tenderpilot.marketanalytics.application.MarketStructureAnalyzer;

import com.tenderpilot.marketanalytics.domain.MarketAnalyticsCapabilities;
import com.tenderpilot.marketanalytics.domain.MarketAwardObservation;
import com.tenderpilot.marketanalytics.domain.MarketCpvObservation;
import com.tenderpilot.marketanalytics.domain.MarketLocationObservation;
import com.tenderpilot.marketanalytics.domain.MarketQuery;
import com.tenderpilot.marketanalytics.domain.MarketSupplierObservation;
import com.tenderpilot.marketanalytics.domain.MarketTenderObservation;

import com.tenderpilot.supplierintelligence.application.SupplierKeyCodec;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MarketAnalyticsServiceTest {

    private final FakeGateway gateway =
            new FakeGateway();


    private final MarketAnalyticsService service =
            new MarketAnalyticsService(
                    gateway,
                    new MarketStructureAnalyzer(),
                    new SupplierKeyCodec()
            );


    @Test
    void marketExplorerAggregatesPublicProcurementObservations() {

        var result =
                service.explore(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );


        assertThat(
                result.summary().noticeCount()
        )
                .isEqualTo(
                        3
                );


        assertThat(
                result.summary().activeNoticeCount()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                result.summary().awardedNoticeCount()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                result.summary().uniqueBuyerCount()
        )
                .isEqualTo(
                        2
                );


        assertThat(
                result.summary().uniqueObservedSupplierCount()
        )
                .isEqualTo(
                        3
                );


        assertThat(
                result.summary().cpvCoveragePercentage()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.summary().geographyCoveragePercentage()
        )
                .isEqualTo(
                        100
                );


        assertThat(
                result.summary().awardCoveragePercentage()
        )
                .isEqualTo(
                        67
                );


        assertThat(
                result.topCpvs().getFirst().sharePercentage()
        )
                .isEqualTo(
                        67
                );


        assertThat(
                result.topBuyers().getFirst().sharePercentage()
        )
                .isEqualTo(
                        67
                );
    }


    @Test
    void marketExplorerPreservesMixedCurrencyBoundary() {

        gateway.mixedCurrencies =
                true;


        var result =
                service.explore(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );


        assertThat(
                result.summary().totalAwardedValue()
        )
                .isNull();


        assertThat(
                result.summary().awardedCurrency()
        )
                .isEqualTo(
                        "MIXED"
                );
    }


    @Test
    void filtersAreNormalizedBeforeGatewayExecution() {

        var result =
                service.explore(
                        "  renovation  ",
                        " fr ",
                        " Île-de-France ",
                        " 4545 ",
                        " published ",
                        LocalDate.of(
                                2026,
                                1,
                                1
                        ),
                        LocalDate.of(
                                2026,
                                12,
                                31
                        )
                );


        assertThat(
                result.filters().query()
        )
                .isEqualTo(
                        "renovation"
                );


        assertThat(
                result.filters().country()
        )
                .isEqualTo(
                        "FR"
                );


        assertThat(
                result.filters().region()
        )
                .isEqualTo(
                        "Île-de-France"
                );


        assertThat(
                result.filters().cpvPrefix()
        )
                .isEqualTo(
                        "4545"
                );


        assertThat(
                result.filters().status()
        )
                .isEqualTo(
                        "PUBLISHED"
                );
    }


    @Test
    void invalidDateRangeIsRejected() {

        assertThatThrownBy(
                () ->
                        service.explore(
                                null,
                                null,
                                null,
                                null,
                                null,
                                LocalDate.of(
                                        2026,
                                        12,
                                        31
                                ),
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                )
                        )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }


    static class FakeGateway
            implements MarketAnalyticsGateway {

        private static final UUID T1 =
                UUID.fromString(
                        "19000000-0000-0000-0000-000000000001"
                );


        private static final UUID T2 =
                UUID.fromString(
                        "19000000-0000-0000-0000-000000000002"
                );


        private static final UUID T3 =
                UUID.fromString(
                        "19000000-0000-0000-0000-000000000003"
                );


        private static final UUID B1 =
                UUID.fromString(
                        "19100000-0000-0000-0000-000000000001"
                );


        private static final UUID B2 =
                UUID.fromString(
                        "19100000-0000-0000-0000-000000000002"
                );


        private static final UUID A1 =
                UUID.fromString(
                        "19200000-0000-0000-0000-000000000001"
                );


        private static final UUID A2 =
                UUID.fromString(
                        "19200000-0000-0000-0000-000000000002"
                );


        boolean mixedCurrencies;


        @Override
        public MarketAnalyticsCapabilities capabilities() {

            return new MarketAnalyticsCapabilities(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true
            );
        }


        @Override
        public List<MarketTenderObservation> tenders(
                MarketQuery query
        ) {

            return List.of(
                    tender(
                            T1,
                            B1,
                            "Buyer A",
                            "PUBLISHED",
                            LocalDate.of(
                                    2026,
                                    1,
                                    1
                            ),
                            "100000",
                            "200000"
                    ),

                    tender(
                            T2,
                            B1,
                            "Buyer A",
                            "PUBLISHED",
                            LocalDate.of(
                                    2026,
                                    2,
                                    1
                            ),
                            "200000",
                            "300000"
                    ),

                    tender(
                            T3,
                            B2,
                            "Buyer B",
                            "AWARDED",
                            LocalDate.of(
                                    2026,
                                    3,
                                    1
                            ),
                            "300000",
                            "400000"
                    )
            );
        }


        @Override
        public List<MarketCpvObservation> cpvs(
                Collection<UUID> tenderIds
        ) {

            return List.of(
                    new MarketCpvObservation(
                            T1,
                            "45453000",
                            "Refurbishment work"
                    ),

                    new MarketCpvObservation(
                            T2,
                            "45453000",
                            "Refurbishment work"
                    ),

                    new MarketCpvObservation(
                            T3,
                            "45210000",
                            "Building work"
                    )
            );
        }


        @Override
        public List<MarketLocationObservation> locations(
                Collection<UUID> tenderIds
        ) {

            return List.of(
                    new MarketLocationObservation(
                            T1,
                            "FR",
                            "IDF",
                            "Île-de-France",
                            "Paris"
                    ),

                    new MarketLocationObservation(
                            T2,
                            "FR",
                            "IDF",
                            "Île-de-France",
                            "Paris"
                    ),

                    new MarketLocationObservation(
                            T3,
                            "FR",
                            "ARA",
                            "Auvergne-Rhône-Alpes",
                            "Lyon"
                    )
            );
        }


        @Override
        public List<MarketAwardObservation> awards(
                Collection<UUID> tenderIds
        ) {

            return List.of(
                    new MarketAwardObservation(
                            A1,
                            T1,
                            new BigDecimal(
                                    "150000"
                            ),
                            "EUR"
                    ),

                    new MarketAwardObservation(
                            A2,
                            T3,
                            new BigDecimal(
                                    "350000"
                            ),
                            mixedCurrencies
                                    ? "USD"
                                    : "EUR"
                    )
            );
        }


        @Override
        public List<MarketSupplierObservation> suppliers(
                Collection<UUID> awardIds
        ) {

            return List.of(
                    new MarketSupplierObservation(
                            A1,
                            "Supplier Alpha"
                    ),

                    new MarketSupplierObservation(
                            A2,
                            "Supplier Beta"
                    ),

                    new MarketSupplierObservation(
                            A2,
                            "Supplier Gamma"
                    )
            );
        }


        private static MarketTenderObservation tender(
                UUID tenderId,
                UUID buyerId,
                String buyerName,
                String status,
                LocalDate publicationDate,
                String min,
                String max
        ) {

            return new MarketTenderObservation(
                    tenderId,
                    buyerId,
                    buyerName,
                    "LOCAL_AUTHORITY",
                    "FR",
                    "Market fixture "
                            +
                            tenderId,
                    status,
                    "OPEN",
                    "WORKS",
                    publicationDate,
                    publicationDate
                            .plusDays(
                                    30
                            )
                            .atStartOfDay()
                            .toInstant(
                                    java.time.ZoneOffset.UTC
                            ),
                    new BigDecimal(
                            min
                    ),
                    new BigDecimal(
                            max
                    ),
                    "EUR"
            );
        }

    }

}
