package com.tenderpilot.buyerintelligence.application;

import com.tenderpilot.buyerintelligence.api.BuyerIntelligenceDetailResponse;
import com.tenderpilot.buyerintelligence.api.BuyerIntelligenceSummaryResponse;

import com.tenderpilot.buyerintelligence.domain.BuyerBehaviorMetrics;

import com.tenderpilot.buyerintelligence.infrastructure.BuyerAnalyticsQueryGateway;
import com.tenderpilot.buyerintelligence.infrastructure.BuyerAnalyticsQueryGateway.AwardRecord;
import com.tenderpilot.buyerintelligence.infrastructure.BuyerAnalyticsQueryGateway.BuyerRecord;
import com.tenderpilot.buyerintelligence.infrastructure.BuyerAnalyticsQueryGateway.CpvRecord;
import com.tenderpilot.buyerintelligence.infrastructure.BuyerAnalyticsQueryGateway.SupplierRecord;
import com.tenderpilot.buyerintelligence.infrastructure.BuyerAnalyticsQueryGateway.TenderRecord;

import com.tenderpilot.common.domain.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class BuyerIntelligenceService {

    private final BuyerAnalyticsQueryGateway
            queryGateway;


    private final BuyerBehaviorAnalyzer
            behaviorAnalyzer;


    public BuyerIntelligenceService(
            BuyerAnalyticsQueryGateway queryGateway,
            BuyerBehaviorAnalyzer behaviorAnalyzer
    ) {

        this.queryGateway =
                queryGateway;

        this.behaviorAnalyzer =
                behaviorAnalyzer;
    }


    public List<BuyerIntelligenceSummaryResponse> search(
            String query,
            String country,
            Integer limit
    ) {

        int resolvedLimit =
                limit == null
                        ? 40
                        : Math.max(
                                1,
                                Math.min(
                                        100,
                                        limit
                                )
                        );


        return queryGateway
                .buyers(
                        query,
                        country,
                        resolvedLimit
                )
                .stream()
                .map(
                        this::summary
                )
                .sorted(
                        Comparator
                                .comparingInt(
                                        BuyerIntelligenceSummaryResponse::noticeCount
                                )
                                .reversed()
                                .thenComparing(
                                        BuyerIntelligenceSummaryResponse::name,
                                        Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                )
                .toList();
    }


    public BuyerIntelligenceDetailResponse detail(
            UUID buyerId
    ) {

        BuyerRecord buyer =
                queryGateway
                        .buyer(
                                buyerId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Buyer was not found"
                                        )
                        );


        List<TenderRecord> tenders =
                queryGateway
                        .tenders(
                                buyer.id()
                        );


        List<UUID> tenderIds =
                tenders
                        .stream()
                        .map(
                                TenderRecord::id
                        )
                        .toList();


        List<CpvRecord> cpvs =
                queryGateway
                        .cpvs(
                                tenderIds
                        );


        List<AwardRecord> awards =
                queryGateway
                        .awards(
                                tenderIds
                        );


        List<SupplierRecord> suppliers =
                queryGateway
                        .suppliers(
                                awards
                                        .stream()
                                        .map(
                                                AwardRecord::id
                                        )
                                        .filter(
                                                java.util.Objects::nonNull
                                        )
                                        .toList()
                        );


        return new BuyerIntelligenceDetailResponse(
                summary(
                        buyer,
                        tenders,
                        cpvs,
                        awards,
                        suppliers
                ),

                topCpvs(
                        cpvs
                ),

                topSuppliers(
                        suppliers
                ),

                monthlyActivity(
                        tenders
                ),

                tenders
                        .stream()
                        .limit(
                                12
                        )
                        .map(
                                tender ->
                                        new BuyerIntelligenceDetailResponse
                                                .RecentTender(
                                                        tender.id(),
                                                        tender.title(),
                                                        tender.status(),
                                                        tender.publicationDate(),
                                                        tender.submissionDeadline(),
                                                        tender.estimatedMinValue(),
                                                        tender.estimatedMaxValue(),
                                                        tender.currency()
                                                )
                        )
                        .toList()
        );
    }


    private BuyerIntelligenceSummaryResponse summary(
            BuyerRecord buyer
    ) {

        List<TenderRecord> tenders =
                queryGateway
                        .tenders(
                                buyer.id()
                        );


        List<UUID> tenderIds =
                tenders
                        .stream()
                        .map(
                                TenderRecord::id
                        )
                        .toList();


        List<CpvRecord> cpvs =
                queryGateway
                        .cpvs(
                                tenderIds
                        );


        List<AwardRecord> awards =
                queryGateway
                        .awards(
                                tenderIds
                        );


        List<SupplierRecord> suppliers =
                queryGateway
                        .suppliers(
                                awards
                                        .stream()
                                        .map(
                                                AwardRecord::id
                                        )
                                        .filter(
                                                java.util.Objects::nonNull
                                        )
                                        .toList()
                        );


        return summary(
                buyer,
                tenders,
                cpvs,
                awards,
                suppliers
        );
    }


    private BuyerIntelligenceSummaryResponse summary(
            BuyerRecord buyer,
            List<TenderRecord> tenders,
            List<CpvRecord> cpvs,
            List<AwardRecord> awards,
            List<SupplierRecord> suppliers
    ) {

        Map<String, Integer> cpvCounts =
                counts(
                        cpvs
                                .stream()
                                .map(
                                        CpvRecord::code
                                )
                                .filter(
                                        java.util.Objects::nonNull
                                )
                                .toList()
                );


        Map<String, Integer> supplierCounts =
                counts(
                        suppliers
                                .stream()
                                .map(
                                        SupplierRecord::name
                                )
                                .filter(
                                        java.util.Objects::nonNull
                                )
                                .toList()
                );


        List<Integer> responseDays =
                tenders
                        .stream()
                        .map(
                                this::responseDays
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .toList();


        BuyerBehaviorMetrics behavior =
                behaviorAnalyzer
                        .analyze(
                                tenders.size(),
                                supplierCounts,
                                cpvCounts,
                                responseDays
                        );


        int activeNotices =
                (int)
                        tenders
                                .stream()
                                .filter(
                                        tender ->
                                                active(
                                                        tender.status()
                                                )
                                )
                                .count();


        LocalDate lastPublication =
                tenders
                        .stream()
                        .map(
                                TenderRecord::publicationDate
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .max(
                                Comparator.naturalOrder()
                        )
                        .orElse(
                                null
                        );


        CurrencyAggregate awardAggregate =
                awardAggregate(
                        awards
                );


        CurrencyAggregate estimateAggregate =
                estimateAggregate(
                        tenders
                );


        return new BuyerIntelligenceSummaryResponse(
                buyer.id(),

                buyer.name(),

                buyer.category(),

                buyer.countryCode(),

                buyer.city(),

                tenders.size(),

                activeNotices,

                awards.size(),

                awardAggregate.value(),

                awardAggregate.currency(),

                estimateAggregate.value(),

                estimateAggregate.currency(),

                lastPublication,

                behavior.recurringProcurement(),

                behavior.topCpvSharePercentage(),

                behavior.supplierConcentration(),

                behavior.topSupplierSharePercentage(),

                behavior.supplierHhi(),

                behavior.medianResponseDays(),

                behavior.deadlineTempo()
        );
    }


    private List<BuyerIntelligenceDetailResponse.CpvInsight>
    topCpvs(
            List<CpvRecord> cpvs
    ) {

        Map<String, Integer> counts =
                counts(
                        cpvs
                                .stream()
                                .map(
                                        CpvRecord::code
                                )
                                .filter(
                                        java.util.Objects::nonNull
                                )
                                .toList()
                );


        Map<String, String> descriptions =
                new LinkedHashMap<>();


        for (
                CpvRecord cpv :
                cpvs
        ) {

            if (
                    cpv.code() != null
                    &&
                    cpv.description() != null
            ) {

                descriptions.putIfAbsent(
                        cpv.code(),
                        cpv.description()
                );
            }
        }


        int total =
                counts
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        return counts
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Integer>comparingByValue()
                                .reversed()
                )
                .limit(
                        10
                )
                .map(
                        entry ->
                                new BuyerIntelligenceDetailResponse
                                        .CpvInsight(
                                                entry.getKey(),

                                                descriptions.get(
                                                        entry.getKey()
                                                ),

                                                entry.getValue(),

                                                percentage(
                                                        entry.getValue(),
                                                        total
                                                )
                                        )
                )
                .toList();
    }


    private List<BuyerIntelligenceDetailResponse.SupplierInsight>
    topSuppliers(
            List<SupplierRecord> suppliers
    ) {

        Map<String, Integer> counts =
                counts(
                        suppliers
                                .stream()
                                .map(
                                        SupplierRecord::name
                                )
                                .filter(
                                        java.util.Objects::nonNull
                                )
                                .toList()
                );


        int total =
                counts
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        return counts
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Integer>comparingByValue()
                                .reversed()
                )
                .limit(
                        10
                )
                .map(
                        entry ->
                                new BuyerIntelligenceDetailResponse
                                        .SupplierInsight(
                                                entry.getKey(),

                                                entry.getValue(),

                                                percentage(
                                                        entry.getValue(),
                                                        total
                                                )
                                        )
                )
                .toList();
    }


    private List<BuyerIntelligenceDetailResponse.MonthlyActivity>
    monthlyActivity(
            List<TenderRecord> tenders
    ) {

        Map<Integer, Integer> counts =
                new LinkedHashMap<>();


        for (
                int month = 1;
                month <= 12;
                month++
        ) {

            counts.put(
                    month,
                    0
            );
        }


        for (
                TenderRecord tender :
                tenders
        ) {

            if (
                    tender.publicationDate()
                            ==
                            null
            ) {

                continue;
            }


            counts.compute(
                    tender
                            .publicationDate()
                            .getMonthValue(),

                    (
                            key,
                            current
                    ) ->
                            current == null
                                    ? 1
                                    : current + 1
            );
        }


        return counts
                .entrySet()
                .stream()
                .map(
                        entry ->
                                new BuyerIntelligenceDetailResponse
                                        .MonthlyActivity(
                                                entry.getKey(),
                                                entry.getValue()
                                        )
                )
                .toList();
    }


    private Map<String, Integer> counts(
            List<String> values
    ) {

        Map<String, Integer> result =
                new LinkedHashMap<>();


        for (
                String value :
                values
        ) {

            if (
                    value == null
                    ||
                    value.isBlank()
            ) {

                continue;
            }


            String normalized =
                    value.trim();


            result.compute(
                    normalized,

                    (
                            key,
                            current
                    ) ->
                            current == null
                                    ? 1
                                    : current + 1
            );
        }


        return result;
    }


    private CurrencyAggregate awardAggregate(
            List<AwardRecord> awards
    ) {

        Map<String, BigDecimal> totals =
                new LinkedHashMap<>();


        for (
                AwardRecord award :
                awards
        ) {

            if (
                    award.amount() == null
                    ||
                    award.currency() == null
            ) {

                continue;
            }


            String currency =
                    award
                            .currency()
                            .toUpperCase(
                                    Locale.ROOT
                            );


            totals.merge(
                    currency,
                    award.amount(),
                    BigDecimal::add
            );
        }


        if (
                totals.size() != 1
        ) {

            return new CurrencyAggregate(
                    null,
                    totals.isEmpty()
                            ? null
                            : "MIXED"
            );
        }


        Map.Entry<String, BigDecimal> entry =
                totals
                        .entrySet()
                        .iterator()
                        .next();


        return new CurrencyAggregate(
                entry.getValue(),
                entry.getKey()
        );
    }


    private CurrencyAggregate estimateAggregate(
            List<TenderRecord> tenders
    ) {

        Map<String, List<BigDecimal>> byCurrency =
                new LinkedHashMap<>();


        for (
                TenderRecord tender :
                tenders
        ) {

            BigDecimal midpoint =
                    midpoint(
                            tender
                                    .estimatedMinValue(),

                            tender
                                    .estimatedMaxValue()
                    );


            if (
                    midpoint == null
                    ||
                    tender.currency() == null
            ) {

                continue;
            }


            byCurrency
                    .computeIfAbsent(
                            tender
                                    .currency()
                                    .toUpperCase(
                                            Locale.ROOT
                                    ),

                            ignored ->
                                    new java.util.ArrayList<>()
                    )
                    .add(
                            midpoint
                    );
        }


        if (
                byCurrency.size() != 1
        ) {

            return new CurrencyAggregate(
                    null,
                    byCurrency.isEmpty()
                            ? null
                            : "MIXED"
            );
        }


        Map.Entry<String, List<BigDecimal>> entry =
                byCurrency
                        .entrySet()
                        .iterator()
                        .next();


        BigDecimal total =
                entry
                        .getValue()
                        .stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal average =
                total.divide(
                        BigDecimal.valueOf(
                                entry
                                        .getValue()
                                        .size()
                        ),
                        2,
                        RoundingMode.HALF_UP
                );


        return new CurrencyAggregate(
                average,
                entry.getKey()
        );
    }


    private BigDecimal midpoint(
            BigDecimal min,
            BigDecimal max
    ) {

        if (
                min == null
                &&
                max == null
        ) {

            return null;
        }


        if (
                min == null
        ) {

            return max;
        }


        if (
                max == null
        ) {

            return min;
        }


        return min
                .add(
                        max
                )
                .divide(
                        BigDecimal.valueOf(
                                2
                        ),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    private Integer responseDays(
            TenderRecord tender
    ) {

        if (
                tender.publicationDate() == null
                ||
                tender.submissionDeadline() == null
        ) {

            return null;
        }


        long days =
                Duration
                        .between(
                                tender
                                        .publicationDate()
                                        .atStartOfDay()
                                        .toInstant(
                                                ZoneOffset.UTC
                                        ),

                                tender.submissionDeadline()
                        )
                        .toDays();


        if (
                days < 0
                ||
                days > Integer.MAX_VALUE
        ) {

            return null;
        }


        return (int)
                days;
    }


    private boolean active(
            String status
    ) {

        if (
                status == null
        ) {

            return false;
        }


        String normalized =
                status
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        return normalized.equals(
                "PUBLISHED"
        )
                ||
                normalized.equals(
                        "OPEN"
                )
                ||
                normalized.equals(
                        "ACTIVE"
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


    private record CurrencyAggregate(

            BigDecimal value,

            String currency

    ) {
    }

}
