package com.tenderpilot.marketanalytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.marketanalytics.application.MarketStructureAnalyzer;

import com.tenderpilot.marketanalytics.domain.MarketConcentration;

import java.util.Map;

import org.junit.jupiter.api.Test;

class MarketStructureAnalyzerTest {

    private final MarketStructureAnalyzer analyzer =
            new MarketStructureAnalyzer();


    @Test
    void emptyMarketHasUnknownConcentration() {

        var result =
                analyzer.analyze(
                        Map.of(),
                        Map.of()
                );


        assertThat(
                result.buyerConcentration()
        )
                .isEqualTo(
                        MarketConcentration.UNKNOWN
                );


        assertThat(
                result.supplierConcentration()
        )
                .isEqualTo(
                        MarketConcentration.UNKNOWN
                );
    }


    @Test
    void dominantBuyerCreatesConcentratedBuyerMarket() {

        var result =
                analyzer.analyze(
                        Map.of(
                                "A",
                                8,
                                "B",
                                2
                        ),
                        Map.of()
                );


        assertThat(
                result.topBuyerSharePercentage()
        )
                .isEqualTo(
                        80
                );


        assertThat(
                result.buyerHhi()
        )
                .isEqualTo(
                        6800
                );


        assertThat(
                result.buyerConcentration()
        )
                .isEqualTo(
                        MarketConcentration.CONCENTRATED
                );
    }


    @Test
    void fourEqualBuyersSitOnModerateBoundary() {

        var result =
                analyzer.analyze(
                        Map.of(
                                "A",
                                1,
                                "B",
                                1,
                                "C",
                                1,
                                "D",
                                1
                        ),
                        Map.of()
                );


        assertThat(
                result.buyerHhi()
        )
                .isEqualTo(
                        2500
                );


        assertThat(
                result.buyerConcentration()
        )
                .isEqualTo(
                        MarketConcentration.MODERATE
                );
    }


    @Test
    void fiveEqualSuppliersAreDiversified() {

        var result =
                analyzer.analyze(
                        Map.of(),
                        Map.of(
                                "A",
                                1,
                                "B",
                                1,
                                "C",
                                1,
                                "D",
                                1,
                                "E",
                                1
                        )
                );


        assertThat(
                result.supplierHhi()
        )
                .isEqualTo(
                        2000
                );


        assertThat(
                result.supplierConcentration()
        )
                .isEqualTo(
                        MarketConcentration.DIVERSIFIED
                );
    }


    @Test
    void supplierTopShareUsesObservedAwardAppearances() {

        var result =
                analyzer.analyze(
                        Map.of(),
                        Map.of(
                                "Alpha",
                                3,
                                "Beta",
                                1,
                                "Gamma",
                                1
                        )
                );


        assertThat(
                result.topSupplierSharePercentage()
        )
                .isEqualTo(
                        60
                );
    }

}
