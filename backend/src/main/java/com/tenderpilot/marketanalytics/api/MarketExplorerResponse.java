package com.tenderpilot.marketanalytics.api;

import com.tenderpilot.marketanalytics.domain.MarketConcentration;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public record MarketExplorerResponse(

        Filters filters,

        Capabilities capabilities,

        Summary summary,

        String valueInterpretation,

        String supplierInterpretation,

        List<TrendPoint> monthlyTrend,

        List<CpvBreakdown> topCpvs,

        List<BuyerBreakdown> topBuyers,

        List<SupplierBreakdown> topSuppliers,

        List<GeographyBreakdown> geographies,

        List<CategoryBreakdown> procedures,

        List<CategoryBreakdown> contractNatures,

        List<RecentOpportunity> recentOpportunities

) {

    public record Filters(

            String query,

            String country,

            String region,

            String cpvPrefix,

            String status,

            LocalDate dateFrom,

            LocalDate dateTo

    ) {
    }


    public record Capabilities(

            boolean countryAvailable,

            boolean regionAvailable,

            boolean cpvAvailable,

            boolean awardsAvailable,

            boolean suppliersAvailable,

            boolean procedureAvailable,

            boolean contractNatureAvailable,

            boolean publicationDateAvailable,

            boolean deadlineAvailable

    ) {
    }


    public record Summary(

            int noticeCount,

            int activeNoticeCount,

            int awardedNoticeCount,

            int uniqueBuyerCount,

            int uniqueObservedSupplierCount,

            int cpvCoveragePercentage,

            int geographyCoveragePercentage,

            int awardCoveragePercentage,

            int supplierCoveragePercentage,

            Integer medianResponseDays,

            BigDecimal averageEstimatedValue,

            String estimatedCurrency,

            BigDecimal totalAwardedValue,

            String awardedCurrency,

            int topBuyerSharePercentage,

            int buyerHhi,

            MarketConcentration buyerConcentration,

            int topObservedSupplierSharePercentage,

            int supplierHhi,

            MarketConcentration supplierConcentration

    ) {
    }


    public record TrendPoint(

            String month,

            int noticeCount,

            int activeNoticeCount,

            int awardedNoticeCount

    ) {
    }


    public record CpvBreakdown(

            String code,

            String description,

            int occurrenceCount,

            int sharePercentage

    ) {
    }


    public record BuyerBreakdown(

            UUID buyerId,

            String name,

            String countryCode,

            int noticeCount,

            int sharePercentage

    ) {
    }


    public record SupplierBreakdown(

            String supplierKey,

            String name,

            int observedAwardCount,

            int sharePercentage

    ) {
    }


    public record GeographyBreakdown(

            String countryCode,

            String regionCode,

            String regionName,

            int noticeCount,

            int sharePercentage

    ) {
    }


    public record CategoryBreakdown(

            String value,

            int noticeCount,

            int sharePercentage

    ) {
    }


    public record RecentOpportunity(

            UUID tenderId,

            UUID buyerId,

            String buyerName,

            String title,

            String status,

            String procedure,

            String contractNature,

            LocalDate publicationDate,

            Instant submissionDeadline,

            BigDecimal estimatedMinValue,

            BigDecimal estimatedMaxValue,

            String currency,

            boolean awarded

    ) {
    }

}
