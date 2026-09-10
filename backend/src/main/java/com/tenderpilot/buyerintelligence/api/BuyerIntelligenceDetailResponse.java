package com.tenderpilot.buyerintelligence.api;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public record BuyerIntelligenceDetailResponse(

        BuyerIntelligenceSummaryResponse summary,

        List<CpvInsight> topCpvs,

        List<SupplierInsight> topSuppliers,

        List<MonthlyActivity> monthlyActivity,

        List<RecentTender> recentTenders

) {

    public record CpvInsight(

            String code,

            String description,

            int occurrenceCount,

            int sharePercentage

    ) {
    }


    public record SupplierInsight(

            String name,

            int awardCount,

            int sharePercentage

    ) {
    }


    public record MonthlyActivity(

            int month,

            int noticeCount

    ) {
    }


    public record RecentTender(

            UUID tenderId,

            String title,

            String status,

            LocalDate publicationDate,

            Instant submissionDeadline,

            BigDecimal estimatedMinValue,

            BigDecimal estimatedMaxValue,

            String currency

    ) {
    }

}
