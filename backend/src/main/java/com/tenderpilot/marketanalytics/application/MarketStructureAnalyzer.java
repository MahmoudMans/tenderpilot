package com.tenderpilot.marketanalytics.application;

import com.tenderpilot.marketanalytics.domain.MarketConcentration;
import com.tenderpilot.marketanalytics.domain.MarketStructureMetrics;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MarketStructureAnalyzer {

    public MarketStructureMetrics analyze(
            Map<String, Integer> buyerNoticeCounts,
            Map<String, Integer> supplierAwardCounts
    ) {

        Map<String, Integer> buyers =
                buyerNoticeCounts == null
                        ? Map.of()
                        : buyerNoticeCounts;


        Map<String, Integer> suppliers =
                supplierAwardCounts == null
                        ? Map.of()
                        : supplierAwardCounts;


        int buyerHhi =
                hhi(
                        buyers
                );


        int supplierHhi =
                hhi(
                        suppliers
                );


        return new MarketStructureMetrics(
                topShare(
                        buyers
                ),

                buyerHhi,

                concentration(
                        buyers.size(),
                        buyerHhi
                ),

                topShare(
                        suppliers
                ),

                supplierHhi,

                concentration(
                        suppliers.size(),
                        supplierHhi
                )
        );
    }


    private int topShare(
            Map<String, Integer> counts
    ) {

        int total =
                counts
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        int maximum =
                counts
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .max()
                        .orElse(
                                0
                        );


        if (
                total <= 0
        ) {

            return 0;
        }


        return (int)
                Math.round(
                        maximum
                                *
                                100.0
                                /
                                total
                );
    }


    private int hhi(
            Map<String, Integer> counts
    ) {

        int total =
                counts
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        if (
                total <= 0
        ) {

            return 0;
        }


        double value =
                0;


        for (
                Integer count :
                counts.values()
        ) {

            double share =
                    count
                            /
                            (double)
                                    total;


            value +=
                    share
                            *
                            share;
        }


        return (int)
                Math.round(
                        value
                                *
                                10000.0
                );
    }


    private MarketConcentration concentration(
            int entityCount,
            int hhi
    ) {

        if (
                entityCount <= 0
        ) {

            return MarketConcentration.UNKNOWN;
        }


        if (
                entityCount >= 4
                &&
                hhi < 2500
        ) {

            return MarketConcentration.DIVERSIFIED;
        }


        if (
                hhi < 5000
        ) {

            return MarketConcentration.MODERATE;
        }


        return MarketConcentration.CONCENTRATED;
    }

}
