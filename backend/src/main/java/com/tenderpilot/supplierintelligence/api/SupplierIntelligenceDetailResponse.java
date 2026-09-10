package com.tenderpilot.supplierintelligence.api;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public record SupplierIntelligenceDetailResponse(

        SupplierIntelligenceSummaryResponse summary,

        String valueInterpretation,

        String competitiveLandscapeInterpretation,

        List<BuyerInsight> topBuyers,

        List<CpvInsight> topCpvs,

        List<CoAwardSupplierInsight> coAwardSuppliers,

        List<RecentAward> recentAwards

) {

    public record BuyerInsight(

            UUID buyerId,

            String name,

            String countryCode,

            String city,

            int awardCount,

            int sharePercentage

    ) {
    }


    public record CpvInsight(

            String code,

            String description,

            int occurrenceCount,

            int sharePercentage

    ) {
    }


    public record CoAwardSupplierInsight(

            String supplierKey,

            String name,

            int sharedAwardCount,

            int sharePercentage

    ) {
    }


    public record RecentAward(

            UUID awardId,

            UUID tenderId,

            String tenderTitle,

            String tenderStatus,

            UUID buyerId,

            String buyerName,

            String buyerCountryCode,

            String buyerCity,

            LocalDate publicationDate,

            BigDecimal associatedAwardValue,

            String awardCurrency,

            boolean jointAward

    ) {
    }

}
