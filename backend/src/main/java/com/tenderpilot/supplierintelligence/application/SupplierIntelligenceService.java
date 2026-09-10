package com.tenderpilot.supplierintelligence.application;

import com.tenderpilot.common.domain.ResourceNotFoundException;

import com.tenderpilot.supplierintelligence.api.SupplierIntelligenceDetailResponse;
import com.tenderpilot.supplierintelligence.api.SupplierIntelligenceSummaryResponse;

import com.tenderpilot.supplierintelligence.domain.SupplierAwardObservation;
import com.tenderpilot.supplierintelligence.domain.SupplierAwardParty;
import com.tenderpilot.supplierintelligence.domain.SupplierLandscapeMetrics;
import com.tenderpilot.supplierintelligence.domain.SupplierTenderCpv;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;

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
public class SupplierIntelligenceService {

    private static final String VALUE_INTERPRETATION =
            "Award values are public contract values associated with this supplier. "
                    +
                    "When an award contains multiple suppliers, TenderPilot does not assume "
                    +
                    "that the full contract value represents revenue attributable to one supplier.";


    private static final String LANDSCAPE_INTERPRETATION =
            "Competitive-landscape signals describe observed public award relationships only. "
                    +
                    "They do not identify unsuccessful bidders or prove who competed for an award "
                    +
                    "unless the underlying public source explicitly provides that information.";


    private final SupplierObservationGateway
            observationGateway;


    private final SupplierLandscapeAnalyzer
            landscapeAnalyzer;


    private final SupplierKeyCodec
            keyCodec;


    public SupplierIntelligenceService(
            SupplierObservationGateway observationGateway,
            SupplierLandscapeAnalyzer landscapeAnalyzer,
            SupplierKeyCodec keyCodec
    ) {

        this.observationGateway =
                observationGateway;

        this.landscapeAnalyzer =
                landscapeAnalyzer;

        this.keyCodec =
                keyCodec;
    }


    public List<SupplierIntelligenceSummaryResponse> search(
            String query,
            String buyerCountry,
            Integer limit
    ) {

        int resolvedLimit =
                limit == null
                        ? 50
                        : Math.max(
                                1,
                                Math.min(
                                        100,
                                        limit
                                )
                        );


        List<String> names =
                observationGateway
                        .searchSupplierNames(
                                query,
                                buyerCountry,
                                resolvedLimit
                        );


        Map<String, SupplierIntelligenceSummaryResponse> unique =
                new LinkedHashMap<>();


        for (
                String name :
                names
        ) {

            List<SupplierAwardObservation> observations =
                    observationGateway
                            .observationsBySupplier(
                                    name
                            );


            if (
                    observations.isEmpty()
            ) {

                continue;
            }


            SupplierIntelligenceSummaryResponse summary =
                    summary(
                            name,
                            observations
                    );


            unique.putIfAbsent(
                    summary.supplierKey(),
                    summary
            );
        }


        return unique
                .values()
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        SupplierIntelligenceSummaryResponse::awardCount
                                )
                                .reversed()
                                .thenComparing(
                                        SupplierIntelligenceSummaryResponse::name,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .toList();
    }


    public SupplierIntelligenceDetailResponse detail(
            String supplierKey
    ) {

        String supplierName =
                keyCodec
                        .decode(
                                supplierKey
                        );


        List<SupplierAwardObservation> observations =
                observationGateway
                        .observationsBySupplier(
                                supplierName
                        );


        if (
                observations.isEmpty()
        ) {

            throw new ResourceNotFoundException(
                    "Supplier was not found in the public award corpus"
            );
        }


        List<UUID> tenderIds =
                observations
                        .stream()
                        .map(
                                SupplierAwardObservation::tenderId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .distinct()
                        .toList();


        List<UUID> awardIds =
                observations
                        .stream()
                        .map(
                                SupplierAwardObservation::awardId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .distinct()
                        .toList();


        List<SupplierTenderCpv> cpvs =
                observationGateway
                        .cpvsForTenders(
                                tenderIds
                        );


        List<SupplierAwardParty> parties =
                observationGateway
                        .suppliersForAwards(
                                awardIds
                        );


        Map<UUID, Integer> supplierCountsByAward =
                supplierCountsByAward(
                        parties
                );


        return new SupplierIntelligenceDetailResponse(
                summary(
                        supplierName,
                        observations,
                        cpvs
                ),

                VALUE_INTERPRETATION,

                LANDSCAPE_INTERPRETATION,

                topBuyers(
                        observations
                ),

                topCpvs(
                        cpvs
                ),

                coAwardSuppliers(
                        supplierName,
                        awardIds.size(),
                        parties
                ),

                recentAwards(
                        observations,
                        supplierCountsByAward
                )
        );
    }


    private SupplierIntelligenceSummaryResponse summary(
            String supplierName,
            List<SupplierAwardObservation> observations
    ) {

        List<UUID> tenderIds =
                observations
                        .stream()
                        .map(
                                SupplierAwardObservation::tenderId
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .distinct()
                        .toList();


        List<SupplierTenderCpv> cpvs =
                observationGateway
                        .cpvsForTenders(
                                tenderIds
                        );


        return summary(
                supplierName,
                observations,
                cpvs
        );
    }


    private SupplierIntelligenceSummaryResponse summary(
            String supplierName,
            List<SupplierAwardObservation> observations,
            List<SupplierTenderCpv> cpvs
    ) {

        List<SupplierAwardObservation> uniqueAwards =
                uniqueAwards(
                        observations
                );


        Map<String, Integer> buyerCounts =
                buyerAwardCounts(
                        uniqueAwards
                );


        Map<String, Integer> cpvCounts =
                counts(
                        cpvs
                                .stream()
                                .map(
                                        SupplierTenderCpv::code
                                )
                                .filter(
                                        java.util.Objects::nonNull
                                )
                                .toList()
                );


        SupplierLandscapeMetrics landscape =
                landscapeAnalyzer
                        .analyze(
                                uniqueAwards.size(),
                                buyerCounts,
                                cpvCounts
                        );


        Set<UUID> buyers =
                new LinkedHashSet<>();


        Set<String> countries =
                new LinkedHashSet<>();


        for (
                SupplierAwardObservation observation :
                uniqueAwards
        ) {

            if (
                    observation.buyerId()
                            !=
                            null
            ) {

                buyers.add(
                        observation.buyerId()
                );
            }


            if (
                    observation.buyerCountryCode()
                            !=
                            null
            ) {

                countries.add(
                        observation
                                .buyerCountryCode()
                                .toUpperCase(
                                        Locale.ROOT
                                )
                );
            }
        }


        CurrencyAggregate values =
                awardValues(
                        uniqueAwards
                );


        LocalDate lastPublication =
                uniqueAwards
                        .stream()
                        .map(
                                SupplierAwardObservation::publicationDate
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


        return new SupplierIntelligenceSummaryResponse(
                keyCodec
                        .encode(
                                supplierName
                        ),

                supplierName,

                uniqueAwards.size(),

                buyers.size(),

                countries.size(),

                values.total(),

                values.average(),

                values.currency(),

                lastPublication,

                landscape.topBuyerSharePercentage(),

                landscape.buyerHhi(),

                landscape.buyerConcentration(),

                landscape.repeatBuyerSignal(),

                landscape.topCpvSharePercentage(),

                landscape.cpvSpecialization()
        );
    }


    private List<SupplierIntelligenceDetailResponse.BuyerInsight>
    topBuyers(
            List<SupplierAwardObservation> observations
    ) {

        List<SupplierAwardObservation> uniqueAwards =
                uniqueAwards(
                        observations
                );


        Map<String, BuyerAccumulator> buyers =
                new LinkedHashMap<>();


        for (
                SupplierAwardObservation observation :
                uniqueAwards
        ) {

            String key =
                    observation.buyerId() != null
                            ? observation
                                    .buyerId()
                                    .toString()
                            : normalized(
                                    observation.buyerName()
                            );


            if (
                    key == null
            ) {

                continue;
            }


            BuyerAccumulator current =
                    buyers.computeIfAbsent(
                            key,
                            ignored ->
                                    new BuyerAccumulator(
                                            observation.buyerId(),
                                            observation.buyerName(),
                                            observation.buyerCountryCode(),
                                            observation.buyerCity()
                                    )
                    );


            current.awardCount++;
        }


        int total =
                uniqueAwards.size();


        return buyers
                .values()
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        BuyerAccumulator::awardCount
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
                        10
                )
                .map(
                        buyer ->
                                new SupplierIntelligenceDetailResponse
                                        .BuyerInsight(
                                                buyer.id,
                                                buyer.name,
                                                buyer.countryCode,
                                                buyer.city,
                                                buyer.awardCount,
                                                percentage(
                                                        buyer.awardCount,
                                                        total
                                                )
                                        )
                )
                .toList();
    }


    private List<SupplierIntelligenceDetailResponse.CpvInsight>
    topCpvs(
            List<SupplierTenderCpv> cpvs
    ) {

        Map<String, Integer> counts =
                counts(
                        cpvs
                                .stream()
                                .map(
                                        SupplierTenderCpv::code
                                )
                                .filter(
                                        java.util.Objects::nonNull
                                )
                                .toList()
                );


        Map<String, String> descriptions =
                new LinkedHashMap<>();


        for (
                SupplierTenderCpv cpv :
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
                                new SupplierIntelligenceDetailResponse
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


    private List<SupplierIntelligenceDetailResponse.CoAwardSupplierInsight>
    coAwardSuppliers(
            String supplierName,
            int supplierAwardCount,
            List<SupplierAwardParty> parties
    ) {

        Map<String, CoAwardAccumulator> counts =
                new LinkedHashMap<>();


        for (
                SupplierAwardParty party :
                parties
        ) {

            if (
                    party.supplierName() == null
                    ||
                    party.supplierName()
                            .equalsIgnoreCase(
                                    supplierName
                            )
            ) {

                continue;
            }


            String normalizedName =
                    normalized(
                            party.supplierName()
                    );


            if (
                    normalizedName == null
            ) {

                continue;
            }


            CoAwardAccumulator current =
                    counts.computeIfAbsent(
                            normalizedName,

                            ignored ->
                                    new CoAwardAccumulator(
                                            party.supplierName()
                                    )
                    );


            current.awardIds.add(
                    party.awardId()
            );
        }


        return counts
                .values()
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        CoAwardAccumulator::sharedAwardCount
                                )
                                .reversed()
                                .thenComparing(
                                        CoAwardAccumulator::name,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .limit(
                        12
                )
                .map(
                        item ->
                                new SupplierIntelligenceDetailResponse
                                        .CoAwardSupplierInsight(
                                                keyCodec
                                                        .encode(
                                                                item.name
                                                        ),

                                                item.name,

                                                item.sharedAwardCount(),

                                                percentage(
                                                        item.sharedAwardCount(),
                                                        supplierAwardCount
                                                )
                                        )
                )
                .toList();
    }


    private List<SupplierIntelligenceDetailResponse.RecentAward>
    recentAwards(
            List<SupplierAwardObservation> observations,
            Map<UUID, Integer> supplierCountsByAward
    ) {

        return uniqueAwards(
                observations
        )
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        SupplierAwardObservation::publicationDate,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                )
                .limit(
                        15
                )
                .map(
                        observation ->
                                new SupplierIntelligenceDetailResponse
                                        .RecentAward(
                                                observation.awardId(),

                                                observation.tenderId(),

                                                observation.tenderTitle(),

                                                observation.tenderStatus(),

                                                observation.buyerId(),

                                                observation.buyerName(),

                                                observation.buyerCountryCode(),

                                                observation.buyerCity(),

                                                observation.publicationDate(),

                                                observation.awardedAmount(),

                                                observation.awardCurrency(),

                                                supplierCountsByAward
                                                        .getOrDefault(
                                                                observation.awardId(),
                                                                1
                                                        )
                                                        >
                                                        1
                                        )
                )
                .toList();
    }


    private Map<UUID, Integer> supplierCountsByAward(
            List<SupplierAwardParty> parties
    ) {

        Map<UUID, Set<String>> names =
                new LinkedHashMap<>();


        for (
                SupplierAwardParty party :
                parties
        ) {

            if (
                    party.awardId() == null
                    ||
                    party.supplierName() == null
            ) {

                continue;
            }


            names
                    .computeIfAbsent(
                            party.awardId(),
                            ignored ->
                                    new LinkedHashSet<>()
                    )
                    .add(
                            normalized(
                                    party.supplierName()
                            )
                    );
        }


        Map<UUID, Integer> result =
                new LinkedHashMap<>();


        for (
                Map.Entry<UUID, Set<String>> entry :
                names.entrySet()
        ) {

            result.put(
                    entry.getKey(),
                    entry.getValue().size()
            );
        }


        return result;
    }


    private List<SupplierAwardObservation> uniqueAwards(
            List<SupplierAwardObservation> observations
    ) {

        Map<String, SupplierAwardObservation> unique =
                new LinkedHashMap<>();


        for (
                SupplierAwardObservation observation :
                observations
        ) {

            String key =
                    observation.awardId() != null
                            ? "A:"
                                    +
                                    observation.awardId()
                            : "T:"
                                    +
                                    observation.tenderId();


            unique.putIfAbsent(
                    key,
                    observation
            );
        }


        return List.copyOf(
                unique.values()
        );
    }


    private Map<String, Integer> buyerAwardCounts(
            List<SupplierAwardObservation> observations
    ) {

        Map<String, Integer> result =
                new LinkedHashMap<>();


        for (
                SupplierAwardObservation observation :
                observations
        ) {

            String key =
                    observation.buyerId() != null
                            ? observation
                                    .buyerId()
                                    .toString()
                            : normalized(
                                    observation.buyerName()
                            );


            if (
                    key == null
            ) {

                continue;
            }


            result.merge(
                    key,
                    1,
                    Integer::sum
            );
        }


        return result;
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

            String normalized =
                    normalized(
                            value
                    );


            if (
                    normalized == null
            ) {

                continue;
            }


            result.merge(
                    normalized,
                    1,
                    Integer::sum
            );
        }


        return result;
    }


    private CurrencyAggregate awardValues(
            List<SupplierAwardObservation> observations
    ) {

        Map<String, List<BigDecimal>> byCurrency =
                new LinkedHashMap<>();


        for (
                SupplierAwardObservation observation :
                observations
        ) {

            if (
                    observation.awardedAmount() == null
                    ||
                    observation.awardCurrency() == null
            ) {

                continue;
            }


            String currency =
                    observation
                            .awardCurrency()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );


            if (
                    currency.isBlank()
            ) {

                continue;
            }


            byCurrency
                    .computeIfAbsent(
                            currency,
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            observation.awardedAmount()
                    );
        }


        if (
                byCurrency.isEmpty()
        ) {

            return new CurrencyAggregate(
                    null,
                    null,
                    null
            );
        }


        if (
                byCurrency.size() > 1
        ) {

            return new CurrencyAggregate(
                    null,
                    null,
                    "MIXED"
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


    private String normalized(
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
                        )
                        .toLowerCase(
                                Locale.ROOT
                        );


        return result.isBlank()
                ? null
                : result;
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

            BigDecimal total,

            BigDecimal average,

            String currency

    ) {
    }


    private static final class BuyerAccumulator {

        private final UUID id;

        private final String name;

        private final String countryCode;

        private final String city;

        private int awardCount;


        private BuyerAccumulator(
                UUID id,
                String name,
                String countryCode,
                String city
        ) {

            this.id =
                    id;

            this.name =
                    name;

            this.countryCode =
                    countryCode;

            this.city =
                    city;
        }


        private int awardCount() {

            return awardCount;
        }


        private String name() {

            return name;
        }

    }


    private static final class CoAwardAccumulator {

        private final String name;

        private final Set<UUID> awardIds =
                new LinkedHashSet<>();


        private CoAwardAccumulator(
                String name
        ) {

            this.name =
                    name;
        }


        private int sharedAwardCount() {

            return awardIds.size();
        }


        private String name() {

            return name;
        }

    }

}
