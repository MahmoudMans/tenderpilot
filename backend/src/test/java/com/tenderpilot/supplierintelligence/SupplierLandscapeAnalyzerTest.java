package com.tenderpilot.supplierintelligence;

import static org.assertj.core.api.Assertions.assertThat;

import com.tenderpilot.supplierintelligence.application.SupplierLandscapeAnalyzer;

import com.tenderpilot.supplierintelligence.domain.BuyerConcentration;
import com.tenderpilot.supplierintelligence.domain.CpvSpecialization;

import java.util.Map;

import org.junit.jupiter.api.Test;

class SupplierLandscapeAnalyzerTest {

    private final SupplierLandscapeAnalyzer analyzer =
            new SupplierLandscapeAnalyzer();


    @Test
    void noBuyerOrCpvHistoryProducesUnknownSignals() {

        var result =
                analyzer.analyze(
                        0,
                        Map.of(),
                        Map.of()
                );


        assertThat(
                result.buyerConcentration()
        )
                .isEqualTo(
                        BuyerConcentration.UNKNOWN
                );


        assertThat(
                result.cpvSpecialization()
        )
                .isEqualTo(
                        CpvSpecialization.UNKNOWN
                );


        assertThat(
                result.repeatBuyerSignal()
        )
                .isFalse();
    }


    @Test
    void dominantBuyerCreatesConcentrationSignal() {

        var result =
                analyzer.analyze(
                        10,

                        Map.of(
                                "buyer-a",
                                8,

                                "buyer-b",
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
                        BuyerConcentration.CONCENTRATED
                );
    }


    @Test
    void repeatedBuyerWithThreeAwardsCreatesRepeatBuyerSignal() {

        var result =
                analyzer.analyze(
                        3,

                        Map.of(
                                "buyer-a",
                                2,

                                "buyer-b",
                                1
                        ),

                        Map.of()
                );


        assertThat(
                result.topBuyerSharePercentage()
        )
                .isEqualTo(
                        67
                );


        assertThat(
                result.repeatBuyerSignal()
        )
                .isTrue();
    }


    @Test
    void dominantCpvCreatesSpecializedSupplierSignal() {

        var result =
                analyzer.analyze(
                        5,

                        Map.of(),

                        Map.of(
                                "45453000",
                                4,

                                "45210000",
                                1
                        )
                );


        assertThat(
                result.topCpvSharePercentage()
        )
                .isEqualTo(
                        80
                );


        assertThat(
                result.cpvSpecialization()
        )
                .isEqualTo(
                        CpvSpecialization.SPECIALIZED
                );
    }


    @Test
    void balancedCpvHistoryIsBroad() {

        var result =
                analyzer.analyze(
                        4,

                        Map.of(),

                        Map.of(
                                "A",
                                1,

                                "B",
                                1,

                                "C",
                                1,

                                "D",
                                1
                        )
                );


        assertThat(
                result.topCpvSharePercentage()
        )
                .isEqualTo(
                        25
                );


        assertThat(
                result.cpvSpecialization()
        )
                .isEqualTo(
                        CpvSpecialization.BROAD
                );
    }

}
