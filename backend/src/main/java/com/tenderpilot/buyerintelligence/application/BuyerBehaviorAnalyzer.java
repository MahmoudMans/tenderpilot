package com.tenderpilot.buyerintelligence.application;

import com.tenderpilot.buyerintelligence.domain.BuyerBehaviorMetrics;
import com.tenderpilot.buyerintelligence.domain.DeadlineTempo;
import com.tenderpilot.buyerintelligence.domain.SupplierConcentration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class BuyerBehaviorAnalyzer {

    public BuyerBehaviorMetrics analyze(
            int noticeCount,
            Map<String, Integer> supplierAwardCounts,
            Map<String, Integer> cpvCounts,
            List<Integer> responseDays
    ) {

        Map<String, Integer> safeSuppliers =
                supplierAwardCounts == null
                        ? Map.of()
                        : supplierAwardCounts;


        Map<String, Integer> safeCpvs =
                cpvCounts == null
                        ? Map.of()
                        : cpvCounts;


        List<Integer> safeResponseDays =
                responseDays == null
                        ? List.of()
                        : responseDays
                                .stream()
                                .filter(
                                        value ->
                                                value != null
                                                &&
                                                value >= 0
                                )
                                .toList();


        int totalSupplierAwards =
                safeSuppliers
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        int topSupplierAwards =
                safeSuppliers
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .max()
                        .orElse(
                                0
                        );


        int topSupplierShare =
                percentage(
                        topSupplierAwards,
                        totalSupplierAwards
                );


        int hhi =
                hhi(
                        safeSuppliers
                );


        SupplierConcentration concentration =
                concentration(
                        safeSuppliers.size(),
                        hhi
                );


        int totalCpvOccurrences =
                safeCpvs
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        int topCpvOccurrences =
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
                        topCpvOccurrences,
                        totalCpvOccurrences
                );


        boolean recurring =
                noticeCount >= 3
                &&
                topCpvShare >= 40;


        Integer medianDays =
                median(
                        safeResponseDays
                );


        return new BuyerBehaviorMetrics(
                topSupplierShare,
                hhi,
                concentration,
                topCpvShare,
                recurring,
                medianDays,
                tempo(
                        medianDays
                )
        );
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
                                10000.0
                );
    }


    private SupplierConcentration concentration(
            int supplierCount,
            int hhi
    ) {

        if (
                supplierCount <= 0
        ) {

            return SupplierConcentration.UNKNOWN;
        }


        if (
                supplierCount >= 4
                &&
                hhi < 2500
        ) {

            return SupplierConcentration.DIVERSIFIED;
        }


        if (
                hhi < 5000
        ) {

            return SupplierConcentration.MODERATE;
        }


        return SupplierConcentration.CONCENTRATED;
    }


    private Integer median(
            List<Integer> values
    ) {

        if (
                values.isEmpty()
        ) {

            return null;
        }


        List<Integer> sorted =
                new ArrayList<>(
                        values
                );


        sorted.sort(
                Comparator.naturalOrder()
        );


        int middle =
                sorted.size()
                        /
                        2;


        if (
                sorted.size()
                        %
                        2
                        ==
                        1
        ) {

            return sorted.get(
                    middle
            );
        }


        return (int)
                Math.round(
                        (
                                sorted.get(
                                        middle - 1
                                )
                                +
                                sorted.get(
                                        middle
                                )
                        )
                                /
                                2.0
                );
    }


    private DeadlineTempo tempo(
            Integer medianDays
    ) {

        if (
                medianDays == null
        ) {

            return DeadlineTempo.UNKNOWN;
        }


        if (
                medianDays < 15
        ) {

            return DeadlineTempo.SHORT;
        }


        if (
                medianDays <= 45
        ) {

            return DeadlineTempo.STANDARD;
        }


        return DeadlineTempo.LONG;
    }

}
