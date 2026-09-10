package com.tenderpilot.supplierintelligence.application;

import com.tenderpilot.supplierintelligence.domain.BuyerConcentration;
import com.tenderpilot.supplierintelligence.domain.CpvSpecialization;
import com.tenderpilot.supplierintelligence.domain.SupplierLandscapeMetrics;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SupplierLandscapeAnalyzer {

    public SupplierLandscapeMetrics analyze(
            int awardCount,
            Map<String, Integer> buyerAwardCounts,
            Map<String, Integer> cpvCounts
    ) {

        Map<String, Integer> safeBuyers =
                buyerAwardCounts == null
                        ? Map.of()
                        : buyerAwardCounts;


        Map<String, Integer> safeCpvs =
                cpvCounts == null
                        ? Map.of()
                        : cpvCounts;


        int buyerTotal =
                safeBuyers
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        int topBuyerCount =
                safeBuyers
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .max()
                        .orElse(
                                0
                        );


        int topBuyerShare =
                percentage(
                        topBuyerCount,
                        buyerTotal
                );


        int buyerHhi =
                hhi(
                        safeBuyers
                );


        BuyerConcentration buyerConcentration =
                buyerConcentration(
                        safeBuyers.size(),
                        buyerHhi
                );


        int cpvTotal =
                safeCpvs
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        int topCpvCount =
                safeCpvs
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .max()
                        .orElse(
                                0
                        );


        int topCpvShare =
                percentage(
                        topCpvCount,
                        cpvTotal
                );


        CpvSpecialization specialization =
                cpvSpecialization(
                        cpvTotal,
                        topCpvShare
                );


        boolean repeatBuyer =
                awardCount >= 3
                &&
                topBuyerShare >= 50;


        return new SupplierLandscapeMetrics(
                topBuyerShare,
                buyerHhi,
                buyerConcentration,
                topCpvShare,
                specialization,
                repeatBuyer
        );
    }


    private BuyerConcentration buyerConcentration(
            int buyerCount,
            int hhi
    ) {

        if (
                buyerCount <= 0
        ) {

            return BuyerConcentration.UNKNOWN;
        }


        if (
                buyerCount >= 4
                &&
                hhi < 2500
        ) {

            return BuyerConcentration.DIVERSIFIED;
        }


        if (
                hhi < 5000
        ) {

            return BuyerConcentration.MODERATE;
        }


        return BuyerConcentration.CONCENTRATED;
    }


    private CpvSpecialization cpvSpecialization(
            int total,
            int topShare
    ) {

        if (
                total <= 0
        ) {

            return CpvSpecialization.UNKNOWN;
        }


        if (
                topShare >= 70
        ) {

            return CpvSpecialization.SPECIALIZED;
        }


        if (
                topShare >= 50
        ) {

            return CpvSpecialization.FOCUSED;
        }


        return CpvSpecialization.BROAD;
    }


    private int percentage(
            int numerator,
            int denominator
    ) {

        if (
                denominator <= 0
        ) {

            return 0;
        }


        return (int)
                Math.round(
                        numerator
                                *
                                100.0
                                /
                                denominator
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


        double result =
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


            result +=
                    share
                            *
                            share;
        }


        return (int)
                Math.round(
                        result
                                *
                                10000
                );
    }

}
