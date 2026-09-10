package com.tenderpilot.marketanalytics.application;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.marketanalytics.api.MarketExplorerResponse;

import com.tenderpilot.marketanalytics.domain.MarketAnalyticsCapabilities;
import com.tenderpilot.marketanalytics.domain.MarketAwardObservation;
import com.tenderpilot.marketanalytics.domain.MarketCpvObservation;
import com.tenderpilot.marketanalytics.domain.MarketLocationObservation;
import com.tenderpilot.marketanalytics.domain.MarketQuery;
import com.tenderpilot.marketanalytics.domain.MarketStructureMetrics;
import com.tenderpilot.marketanalytics.domain.MarketSupplierObservation;
import com.tenderpilot.marketanalytics.domain.MarketTenderObservation;

import com.tenderpilot.supplierintelligence.application.SupplierKeyCodec;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class MarketAnalyticsService {

    private static final String VALUE_INTERPRETATION =
            "Estimated-market values use the midpoint of each tender's published estimated range "
                    +
                    "when available. Award totals sum each public award record once. "
                    +
                    "Mixed currencies are never added together.";


    private static final String SUPPLIER_INTERPRETATION =
            "Supplier metrics describe suppliers observed on public award records. "
                    +
                    "They are not a complete bidder census and do not identify unsuccessful bidders.";


    private final MarketAnalyticsGateway
            analyticsGateway;


    private final MarketStructureAnalyzer
            structureAnalyzer;


    private final SupplierKeyCodec
            supplierKeyCodec;


    public MarketAnalyticsService(
            MarketAnalyticsGateway analyticsGateway,
            MarketStructureAnalyzer structureAnalyzer,
            SupplierKeyCodec supplierKeyCodec
    ) {

        this.analyticsGateway =
                analyticsGateway;

        this.structureAnalyzer =
                structureAnalyzer;

        this.supplierKeyCodec =
                supplierKeyCodec;
    }


    public MarketExplorerResponse explore(
            String query,
            String country,
            String region,
            String cpvPrefix,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {

        MarketQuery filters =
                normalize(
                        query,
                        country,
                        region,
                        cpvPrefix,
                        status,
                        dateFrom,
                        dateTo
                );


        MarketAnalyticsCapabilities capabilities =
                analyticsGateway
                        .capabilities();


        validateCapabilities(
                filters,
                capabilities
        );


        List<MarketTenderObservation> tenders =
                analyticsGateway
                        .tenders(
                                filters
                        );


        List<UUID> tenderIds =
                tenders
                        .stream()
                        .map(
                                MarketTenderObservation::tenderId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .distinct()
                        .toList();


        List<MarketCpvObservation> cpvs =
                analyticsGateway
                        .cpvs(
                                tenderIds
                        );


        List<MarketLocationObservation> locations =
                analyticsGateway
                        .locations(
                                tenderIds
                        );


        List<MarketAwardObservation> awards =
                uniqueAwards(
                        analyticsGateway
                                .awards(
                                        tenderIds
                                )
                );


        List<UUID> awardIds =
                awards
                        .stream()
                        .map(
                                MarketAwardObservation::awardId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .distinct()
                        .toList();


        List<MarketSupplierObservation> suppliers =
                uniqueSuppliers(
                        analyticsGateway
                                .suppliers(
                                        awardIds
                                )
                );


        Set<UUID> awardedTenderIds =
                awards
                        .stream()
                        .map(
                                MarketAwardObservation::tenderId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );


        Set<UUID> cpvTenderIds =
                cpvs
                        .stream()
                        .map(
                                MarketCpvObservation::tenderId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );


        Set<UUID> geographyTenderIds =
                locations
                        .stream()
                        .filter(
                                this::meaningfulLocation
                        )
                        .map(
                                MarketLocationObservation::tenderId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );


        Set<UUID> supplierAwardIds =
                suppliers
                        .stream()
                        .map(
                                MarketSupplierObservation::awardId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );


        Map<String, Integer> buyerCounts =
                buyerCounts(
                        tenders
                );


        Map<String, Integer> supplierCounts =
                supplierCounts(
                        suppliers
                );


        MarketStructureMetrics structure =
                structureAnalyzer
                        .analyze(
                                buyerCounts,
                                supplierCounts
                        );


        CurrencyAggregate estimated =
                estimatedValues(
                        tenders
                );


        CurrencyAggregate awarded =
                awardedValues(
                        awards
                );


        int noticeCount =
                tenders.size();


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


        Set<String> uniqueSuppliers =
                suppliers
                        .stream()
                        .map(
                                MarketSupplierObservation::supplierName
                        )
                        .map(
                                this::normalized
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );


        Set<String> uniqueBuyers =
                tenders
                        .stream()
                        .map(
                                this::buyerKey
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );


        return new MarketExplorerResponse(
                new MarketExplorerResponse
                        .Filters(
                                filters.query(),
                                filters.country(),
                                filters.region(),
                                filters.cpvPrefix(),
                                filters.status(),
                                filters.dateFrom(),
                                filters.dateTo()
                        ),

                new MarketExplorerResponse
                        .Capabilities(
                                capabilities.countryAvailable(),
                                capabilities.regionAvailable(),
                                capabilities.cpvAvailable(),
                                capabilities.awardsAvailable(),
                                capabilities.suppliersAvailable(),
                                capabilities.procedureAvailable(),
                                capabilities.contractNatureAvailable(),
                                capabilities.publicationDateAvailable(),
                                capabilities.deadlineAvailable()
                        ),

                new MarketExplorerResponse
                        .Summary(
                                noticeCount,

                                activeNotices,

                                awardedTenderIds.size(),

                                uniqueBuyers.size(),

                                uniqueSuppliers.size(),

                                percentage(
                                        cpvTenderIds.size(),
                                        noticeCount
                                ),

                                percentage(
                                        geographyTenderIds.size(),
                                        noticeCount
                                ),

                                percentage(
                                        awardedTenderIds.size(),
                                        noticeCount
                                ),

                                percentage(
                                        supplierAwardIds.size(),
                                        awards.size()
                                ),

                                medianResponseDays(
                                        tenders
                                ),

                                estimated.average(),

                                estimated.currency(),

                                awarded.total(),

                                awarded.currency(),

                                structure.topBuyerSharePercentage(),

                                structure.buyerHhi(),

                                structure.buyerConcentration(),

                                structure.topSupplierSharePercentage(),

                                structure.supplierHhi(),

                                structure.supplierConcentration()
                        ),

                VALUE_INTERPRETATION,

                SUPPLIER_INTERPRETATION,

                monthlyTrend(
                        tenders,
                        awardedTenderIds
                ),

                topCpvs(
                        cpvs
                ),

                topBuyers(
                        tenders
                ),

                topSuppliers(
                        suppliers
                ),

                geographies(
                        locations,
                        noticeCount
                ),

                categories(
                        tenders
                                .stream()
                                .map(
                                        MarketTenderObservation::procedure
                                )
                                .toList(),

                        noticeCount
                ),

                categories(
                        tenders
                                .stream()
                                .map(
                                        MarketTenderObservation::contractNature
                                )
                                .toList(),

                        noticeCount
                ),

                recentOpportunities(
                        tenders,
                        awardedTenderIds
                )
        );
    }


    private MarketQuery normalize(
            String query,
            String country,
            String region,
            String cpvPrefix,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {

        if (
                dateFrom != null
                &&
                dateTo != null
                &&
                dateFrom.isAfter(
                        dateTo
                )
        ) {

            throw new InvalidRequestException(
                    "Market dateFrom cannot be after dateTo"
            );
        }


        String normalizedCpv =
                compact(
                        cpvPrefix
                );


        if (
                normalizedCpv != null
                &&
                !normalizedCpv.matches(
                        "[0-9]{1,8}"
                )
        ) {

            throw new InvalidRequestException(
                    "CPV prefix must contain between 1 and 8 digits"
            );
        }


        return new MarketQuery(
                clean(
                        query
                ),

                upper(
                        country
                ),

                clean(
                        region
                ),

                normalizedCpv,

                upper(
                        status
                ),

                dateFrom,

                dateTo
        );
    }


    private void validateCapabilities(
            MarketQuery query,
            MarketAnalyticsCapabilities capabilities
    ) {

        if (
                query.country() != null
                &&
                !capabilities.countryAvailable()
        ) {

            throw new InvalidRequestException(
                    "Country filtering is unavailable for the current corpus"
            );
        }


        if (
                query.region() != null
                &&
                !capabilities.regionAvailable()
        ) {

            throw new InvalidRequestException(
                    "Region filtering is unavailable for the current corpus"
            );
        }


        if (
                query.cpvPrefix() != null
                &&
                !capabilities.cpvAvailable()
        ) {

            throw new InvalidRequestException(
                    "CPV filtering is unavailable for the current corpus"
            );
        }


        if (
                query.status() != null
                &&
                !capabilities.publicationDateAvailable()
                &&
                !capabilities.deadlineAvailable()
        ) {

            /*
             * This does not gate status itself. The canonical gateway performs
             * the authoritative status-column validation. Keep this branch
             * intentionally empty to avoid inventing capability state.
             */
        }
    }


    private List<MarketExplorerResponse.TrendPoint> monthlyTrend(
            List<MarketTenderObservation> tenders,
            Set<UUID> awardedTenderIds
    ) {

        Map<YearMonth, TrendAccumulator> values =
                new LinkedHashMap<>();


        for (
                MarketTenderObservation tender :
                tenders
        ) {

            if (
                    tender.publicationDate() == null
            ) {

                continue;
            }


            YearMonth month =
                    YearMonth.from(
                            tender.publicationDate()
                    );


            TrendAccumulator accumulator =
                    values.computeIfAbsent(
                            month,
                            ignored ->
                                    new TrendAccumulator()
                    );


            accumulator.noticeCount++;


            if (
                    active(
                            tender.status()
                    )
            ) {

                accumulator.activeCount++;
            }


            if (
                    awardedTenderIds.contains(
                            tender.tenderId()
                    )
            ) {

                accumulator.awardedCount++;
            }
        }


        return values
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.comparingByKey()
                )
                .map(
                        entry ->
                                new MarketExplorerResponse
                                        .TrendPoint(
                                                entry
                                                        .getKey()
                                                        .toString(),

                                                entry
                                                        .getValue()
                                                        .noticeCount,

                                                entry
                                                        .getValue()
                                                        .activeCount,

                                                entry
                                                        .getValue()
                                                        .awardedCount
                                        )
                )
                .toList();
    }


    private List<MarketExplorerResponse.CpvBreakdown> topCpvs(
            List<MarketCpvObservation> cpvs
    ) {

        Map<String, Set<UUID>> tendersByCode =
                new LinkedHashMap<>();


        Map<String, String> descriptions =
                new LinkedHashMap<>();


        for (
                MarketCpvObservation cpv :
                cpvs
        ) {

            String code =
                    clean(
                            cpv.code()
                    );


            if (
                    code == null
            ) {

                continue;
            }


            tendersByCode
                    .computeIfAbsent(
                            code,
                            ignored ->
                                    new LinkedHashSet<>()
                    )
                    .add(
                            cpv.tenderId()
                    );


            if (
                    cpv.description() != null
            ) {

                descriptions.putIfAbsent(
                        code,
                        cpv.description()
                );
            }
        }


        int total =
                tendersByCode
                        .values()
                        .stream()
                        .mapToInt(
                                Set::size
                        )
                        .sum();


        return tendersByCode
                .entrySet()
                .stream()
                .sorted(
                        Comparator
                                .<Map.Entry<String, Set<UUID>>>comparingInt(
                                        entry ->
                                                entry
                                                        .getValue()
                                                        .size()
                                )
                                .reversed()
                                .thenComparing(
                                        Map.Entry::getKey
                                )
                )
                .limit(
                        12
                )
                .map(
                        entry ->
                                new MarketExplorerResponse
                                        .CpvBreakdown(
                                                entry.getKey(),

                                                descriptions.get(
                                                        entry.getKey()
                                                ),

                                                entry
                                                        .getValue()
                                                        .size(),

                                                percentage(
                                                        entry
                                                                .getValue()
                                                                .size(),

                                                        total
                                                )
                                        )
                )
                .toList();
    }


    private List<MarketExplorerResponse.BuyerBreakdown> topBuyers(
            List<MarketTenderObservation> tenders
    ) {

        Map<String, BuyerAccumulator> buyers =
                new LinkedHashMap<>();


        for (
                MarketTenderObservation tender :
                tenders
        ) {

            String key =
                    buyerKey(
                            tender
                    );


            if (
                    key == null
            ) {

                continue;
            }


            BuyerAccumulator accumulator =
                    buyers.computeIfAbsent(
                            key,

                            ignored ->
                                    new BuyerAccumulator(
                                            tender.buyerId(),
                                            tender.buyerName(),
                                            tender.buyerCountryCode()
                                    )
                    );


            accumulator.noticeCount++;
        }


        int total =
                buyers
                        .values()
                        .stream()
                        .mapToInt(
                                BuyerAccumulator::noticeCount
                        )
                        .sum();


        return buyers
                .values()
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        BuyerAccumulator::noticeCount
                                )
                                .reversed()
                                .thenComparing(
                                        BuyerAccumulator::name,
                                        Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                )
                .limit(
                        12
                )
                .map(
                        buyer ->
                                new MarketExplorerResponse
                                        .BuyerBreakdown(
                                                buyer.id,

                                                buyer.name,

                                                buyer.countryCode,

                                                buyer.noticeCount,

                                                percentage(
                                                        buyer.noticeCount,
                                                        total
                                                )
                                        )
                )
                .toList();
    }


    private List<MarketExplorerResponse.SupplierBreakdown> topSuppliers(
            List<MarketSupplierObservation> suppliers
    ) {

        Map<String, SupplierAccumulator> values =
                new LinkedHashMap<>();


        for (
                MarketSupplierObservation supplier :
                suppliers
        ) {

            String normalizedName =
                    normalized(
                            supplier.supplierName()
                    );


            if (
                    normalizedName == null
            ) {

                continue;
            }


            SupplierAccumulator accumulator =
                    values.computeIfAbsent(
                            normalizedName,

                            ignored ->
                                    new SupplierAccumulator(
                                            supplier.supplierName()
                                    )
                    );


            accumulator.awards.add(
                    supplier.awardId()
            );
        }


        int total =
                values
                        .values()
                        .stream()
                        .mapToInt(
                                SupplierAccumulator::awardCount
                        )
                        .sum();


        return values
                .values()
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        SupplierAccumulator::awardCount
                                )
                                .reversed()
                                .thenComparing(
                                        SupplierAccumulator::name,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .limit(
                        12
                )
                .map(
                        supplier ->
                                new MarketExplorerResponse
                                        .SupplierBreakdown(
                                                supplierKeyCodec
                                                        .encode(
                                                                supplier.name
                                                        ),

                                                supplier.name,

                                                supplier.awardCount(),

                                                percentage(
                                                        supplier.awardCount(),
                                                        total
                                                )
                                        )
                )
                .toList();
    }


    private List<MarketExplorerResponse.GeographyBreakdown> geographies(
            List<MarketLocationObservation> locations,
            int noticeCount
    ) {

        Map<String, GeographyAccumulator> values =
                new LinkedHashMap<>();


        Set<String> seen =
                new LinkedHashSet<>();


        for (
                MarketLocationObservation location :
                locations
        ) {

            if (
                    !meaningfulLocation(
                            location
                    )
            ) {

                continue;
            }


            String geographyKey =
                    String.join(
                            "|",
                            nullSafe(
                                    upper(
                                            location.countryCode()
                                    )
                            ),
                            nullSafe(
                                    upper(
                                            location.regionCode()
                                    )
                            ),
                            nullSafe(
                                    clean(
                                            location.regionName()
                                    )
                            )
                    );


            String tenderGeographyKey =
                    location.tenderId()
                            +
                            "::"
                            +
                            geographyKey;


            if (
                    !seen.add(
                            tenderGeographyKey
                    )
            ) {

                continue;
            }


            GeographyAccumulator accumulator =
                    values.computeIfAbsent(
                            geographyKey,

                            ignored ->
                                    new GeographyAccumulator(
                                            upper(
                                                    location.countryCode()
                                            ),

                                            upper(
                                                    location.regionCode()
                                            ),

                                            clean(
                                                    location.regionName()
                                            )
                                    )
                    );


            accumulator.noticeCount++;
        }


        return values
                .values()
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        GeographyAccumulator::noticeCount
                                )
                                .reversed()
                                .thenComparing(
                                        GeographyAccumulator::displayKey
                                )
                )
                .limit(
                        16
                )
                .map(
                        item ->
                                new MarketExplorerResponse
                                        .GeographyBreakdown(
                                                item.countryCode,

                                                item.regionCode,

                                                item.regionName,

                                                item.noticeCount,

                                                percentage(
                                                        item.noticeCount,
                                                        noticeCount
                                                )
                                        )
                )
                .toList();
    }


    private List<MarketExplorerResponse.CategoryBreakdown> categories(
            List<String> rawValues,
            int noticeCount
    ) {

        Map<String, Integer> values =
                new LinkedHashMap<>();


        for (
                String raw :
                rawValues
        ) {

            String value =
                    clean(
                            raw
                    );


            if (
                    value == null
            ) {

                continue;
            }


            values.merge(
                    value,
                    1,
                    Integer::sum
            );
        }


        return values
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<String, Integer>comparingByValue()
                                .reversed()
                                .thenComparing(
                                        Map.Entry::getKey
                                )
                )
                .limit(
                        12
                )
                .map(
                        entry ->
                                new MarketExplorerResponse
                                        .CategoryBreakdown(
                                                entry.getKey(),

                                                entry.getValue(),

                                                percentage(
                                                        entry.getValue(),
                                                        noticeCount
                                                )
                                        )
                )
                .toList();
    }


    private List<MarketExplorerResponse.RecentOpportunity>
    recentOpportunities(
            List<MarketTenderObservation> tenders,
            Set<UUID> awardedTenderIds
    ) {

        return tenders
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        MarketTenderObservation::publicationDate,

                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                                .thenComparing(
                                        MarketTenderObservation::title,
                                        Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                )
                .limit(
                        15
                )
                .map(
                        tender ->
                                new MarketExplorerResponse
                                        .RecentOpportunity(
                                                tender.tenderId(),

                                                tender.buyerId(),

                                                tender.buyerName(),

                                                tender.title(),

                                                tender.status(),

                                                tender.procedure(),

                                                tender.contractNature(),

                                                tender.publicationDate(),

                                                tender.submissionDeadline(),

                                                tender.estimatedMinValue(),

                                                tender.estimatedMaxValue(),

                                                tender.currency(),

                                                awardedTenderIds.contains(
                                                        tender.tenderId()
                                                )
                                        )
                )
                .toList();
    }


    private Map<String, Integer> buyerCounts(
            List<MarketTenderObservation> tenders
    ) {

        Map<String, Integer> result =
                new LinkedHashMap<>();


        for (
                MarketTenderObservation tender :
                tenders
        ) {

            String key =
                    buyerKey(
                            tender
                    );


            if (
                    key != null
            ) {

                result.merge(
                        key,
                        1,
                        Integer::sum
                );
            }
        }


        return result;
    }


    private Map<String, Integer> supplierCounts(
            List<MarketSupplierObservation> suppliers
    ) {

        Map<String, Set<UUID>> awardsBySupplier =
                new LinkedHashMap<>();


        for (
                MarketSupplierObservation supplier :
                suppliers
        ) {

            String key =
                    normalized(
                            supplier.supplierName()
                    );


            if (
                    key == null
            ) {

                continue;
            }


            awardsBySupplier
                    .computeIfAbsent(
                            key,
                            ignored ->
                                    new LinkedHashSet<>()
                    )
                    .add(
                            supplier.awardId()
                    );
        }


        Map<String, Integer> result =
                new LinkedHashMap<>();


        for (
                Map.Entry<String, Set<UUID>> entry :
                awardsBySupplier.entrySet()
        ) {

            result.put(
                    entry.getKey(),
                    entry.getValue().size()
            );
        }


        return result;
    }


    private List<MarketAwardObservation> uniqueAwards(
            List<MarketAwardObservation> awards
    ) {

        Map<String, MarketAwardObservation> unique =
                new LinkedHashMap<>();


        for (
                MarketAwardObservation award :
                awards
        ) {

            String key =
                    award.awardId() != null
                            ? "A:"
                                    +
                                    award.awardId()
                            : "T:"
                                    +
                                    award.tenderId();


            unique.putIfAbsent(
                    key,
                    award
            );
        }


        return List.copyOf(
                unique.values()
        );
    }


    private List<MarketSupplierObservation> uniqueSuppliers(
            List<MarketSupplierObservation> suppliers
    ) {

        Map<String, MarketSupplierObservation> unique =
                new LinkedHashMap<>();


        for (
                MarketSupplierObservation supplier :
                suppliers
        ) {

            String normalizedName =
                    normalized(
                            supplier.supplierName()
                    );


            if (
                    supplier.awardId() == null
                    ||
                    normalizedName == null
            ) {

                continue;
            }


            String key =
                    supplier.awardId()
                            +
                            "::"
                            +
                            normalizedName;


            unique.putIfAbsent(
                    key,
                    supplier
            );
        }


        return List.copyOf(
                unique.values()
        );
    }


    private CurrencyAggregate estimatedValues(
            List<MarketTenderObservation> tenders
    ) {

        Map<String, List<BigDecimal>> values =
                new LinkedHashMap<>();


        for (
                MarketTenderObservation tender :
                tenders
        ) {

            BigDecimal midpoint =
                    midpoint(
                            tender.estimatedMinValue(),
                            tender.estimatedMaxValue()
                    );


            if (
                    midpoint == null
                    ||
                    tender.currency() == null
            ) {

                continue;
            }


            String currency =
                    tender
                            .currency()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );


            if (
                    currency.isBlank()
            ) {

                continue;
            }


            values
                    .computeIfAbsent(
                            currency,
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            midpoint
                    );
        }


        if (
                values.isEmpty()
        ) {

            return new CurrencyAggregate(
                    null,
                    null,
                    null
            );
        }


        if (
                values.size() > 1
        ) {

            return new CurrencyAggregate(
                    null,
                    null,
                    "MIXED"
            );
        }


        Map.Entry<String, List<BigDecimal>> entry =
                values
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
                                entry.getValue().size()
                        ),
                        2,
                        RoundingMode.HALF_UP
                );


        return new CurrencyAggregate(
                total,
                average,
                entry.getKey()
        );
    }


    private CurrencyAggregate awardedValues(
            List<MarketAwardObservation> awards
    ) {

        Map<String, List<BigDecimal>> values =
                new LinkedHashMap<>();


        for (
                MarketAwardObservation award :
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
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );


            if (
                    currency.isBlank()
            ) {

                continue;
            }


            values
                    .computeIfAbsent(
                            currency,
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            award.amount()
                    );
        }


        if (
                values.isEmpty()
        ) {

            return new CurrencyAggregate(
                    null,
                    null,
                    null
            );
        }


        if (
                values.size() > 1
        ) {

            return new CurrencyAggregate(
                    null,
                    null,
                    "MIXED"
            );
        }


        Map.Entry<String, List<BigDecimal>> entry =
                values
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
                                entry.getValue().size()
                        ),
                        2,
                        RoundingMode.HALF_UP
                );


        return new CurrencyAggregate(
                total,
                average,
                entry.getKey()
        );
    }


    private Integer medianResponseDays(
            List<MarketTenderObservation> tenders
    ) {

        List<Integer> days =
                tenders
                        .stream()
                        .map(
                                this::responseDays
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .sorted()
                        .toList();


        if (
                days.isEmpty()
        ) {

            return null;
        }


        int middle =
                days.size()
                        /
                        2;


        if (
                days.size()
                        %
                        2
                        ==
                        1
        ) {

            return days.get(
                    middle
            );
        }


        return (int)
                Math.round(
                        (
                                days.get(
                                        middle - 1
                                )
                                +
                                days.get(
                                        middle
                                )
                        )
                                /
                                2.0
                );
    }


    private Integer responseDays(
            MarketTenderObservation tender
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


    private boolean meaningfulLocation(
            MarketLocationObservation location
    ) {

        return location != null
                &&
                (
                        clean(
                                location.countryCode()
                        )
                                !=
                                null
                        ||
                        clean(
                                location.regionCode()
                        )
                                !=
                                null
                        ||
                        clean(
                                location.regionName()
                        )
                                !=
                                null
                        ||
                        clean(
                                location.city()
                        )
                                !=
                                null
                );
    }


    private boolean active(
            String status
    ) {

        String value =
                upper(
                        status
                );


        if (
                value == null
        ) {

            return false;
        }


        return value.equals(
                "PUBLISHED"
        )
                ||
                value.equals(
                        "OPEN"
                )
                ||
                value.equals(
                        "ACTIVE"
                );
    }


    private String buyerKey(
            MarketTenderObservation tender
    ) {

        if (
                tender.buyerId() != null
        ) {

            return tender
                    .buyerId()
                    .toString();
        }


        return normalized(
                tender.buyerName()
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


    private String clean(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String result =
                value
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        return result.isBlank()
                ? null
                : result;
    }


    private String upper(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );


        return cleaned == null
                ? null
                : cleaned.toUpperCase(
                        Locale.ROOT
                );
    }


    private String compact(
            String value
    ) {

        if (
                value == null
        ) {

            return null;
        }


        String result =
                value
                        .replaceAll(
                                "\\s+",
                                ""
                        )
                        .trim();


        return result.isBlank()
                ? null
                : result;
    }


    private String normalized(
            String value
    ) {

        String cleaned =
                clean(
                        value
                );


        return cleaned == null
                ? null
                : cleaned.toLowerCase(
                        Locale.ROOT
                );
    }


    private String nullSafe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }


    private record CurrencyAggregate(

            BigDecimal total,

            BigDecimal average,

            String currency

    ) {
    }


    private static final class TrendAccumulator {

        private int noticeCount;

        private int activeCount;

        private int awardedCount;

    }


    private static final class BuyerAccumulator {

        private final UUID id;

        private final String name;

        private final String countryCode;

        private int noticeCount;


        private BuyerAccumulator(
                UUID id,
                String name,
                String countryCode
        ) {

            this.id =
                    id;

            this.name =
                    name;

            this.countryCode =
                    countryCode;
        }


        private int noticeCount() {

            return noticeCount;
        }


        private String name() {

            return name;
        }

    }


    private static final class SupplierAccumulator {

        private final String name;

        private final Set<UUID> awards =
                new LinkedHashSet<>();


        private SupplierAccumulator(
                String name
        ) {

            this.name =
                    name;
        }


        private int awardCount() {

            return awards.size();
        }


        private String name() {

            return name;
        }

    }


    private static final class GeographyAccumulator {

        private final String countryCode;

        private final String regionCode;

        private final String regionName;

        private int noticeCount;


        private GeographyAccumulator(
                String countryCode,
                String regionCode,
                String regionName
        ) {

            this.countryCode =
                    countryCode;

            this.regionCode =
                    regionCode;

            this.regionName =
                    regionName;
        }


        private int noticeCount() {

            return noticeCount;
        }


        private String displayKey() {

            return String.join(
                    "|",
                    countryCode == null
                            ? ""
                            : countryCode,

                    regionCode == null
                            ? ""
                            : regionCode,

                    regionName == null
                            ? ""
                            : regionName
            );
        }

    }

}
