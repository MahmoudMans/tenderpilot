package com.tenderpilot.buyerintelligence;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.buyerintelligence.application.BuyerBehaviorAnalyzer;

import com.tenderpilot.buyerintelligence.domain.DeadlineTempo;
import com.tenderpilot.buyerintelligence.domain.SupplierConcentration;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class BuyerBehaviorAnalyzerTest {

    private final BuyerBehaviorAnalyzer analyzer =
            new BuyerBehaviorAnalyzer();


    @Test
    void missingSupplierHistoryProducesUnknownConcentration() {

        var result =
                analyzer.analyze(
                        1,
                        Map.of(),
                        Map.of(),
                        List.of()
                );


        assertThat(
                result.supplierConcentration()
        )
                .isEqualTo(
                        SupplierConcentration.UNKNOWN
                );


        assertThat(
                result.topSupplierSharePercentage()
        )
                .isZero();


        assertThat(
                result.supplierHhi()
        )
                .isZero();
    }


    @Test
    void dominantSupplierProducesConcentratedMarket() {

        var result =
                analyzer.analyze(
                        10,

                        Map.of(
                                "Supplier A",
                                8,

                                "Supplier B",
                                2
                        ),

                        Map.of(),

                        List.of()
                );


        assertThat(
                result.topSupplierSharePercentage()
        )
                .isEqualTo(
                        80
                );


        assertThat(
                result.supplierHhi()
        )
                .isEqualTo(
                        6800
                );


        assertThat(
                result.supplierConcentration()
        )
                .isEqualTo(
                        SupplierConcentration.CONCENTRATED
                );
    }


    @Test
    void fourBalancedSuppliersAreDiversified() {

        var result =
                analyzer.analyze(
                        12,

                        Map.of(
                                "A",
                                3,
                                "B",
                                3,
                                "C",
                                3,
                                "D",
                                3
                        ),

                        Map.of(),

                        List.of()
                );


        assertThat(
                result.topSupplierSharePercentage()
        )
                .isEqualTo(
                        25
                );


        assertThat(
                result.supplierHhi()
        )
                .isEqualTo(
                        2500
                );


        /*
         * Exact 2500 sits on the moderate boundary.
         */
        assertThat(
                result.supplierConcentration()
        )
                .isEqualTo(
                        SupplierConcentration.MODERATE
                );
    }


    @Test
    void repeatedCpvCreatesRecurringProcurementSignal() {

        var result =
                analyzer.analyze(
                        5,

                        Map.of(),

                        Map.of(
                                "45453000",
                                4,

                                "45210000",
                                1
                        ),

                        List.of()
                );


        assertThat(
                result.topCpvSharePercentage()
        )
                .isEqualTo(
                        80
                );


        assertThat(
                result.recurringProcurement()
        )
                .isTrue();
    }


    @Test
    void medianDeadlineTempoIsCalculated() {

        var result =
                analyzer.analyze(
                        4,

                        Map.of(),

                        Map.of(),

                        List.of(
                                20,
                                30,
                                40,
                                50
                        )
                );


        assertThat(
                result.medianResponseDays()
        )
                .isEqualTo(
                        35
                );


        assertThat(
                result.deadlineTempo()
        )
                .isEqualTo(
                        DeadlineTempo.STANDARD
                );
    }

}
