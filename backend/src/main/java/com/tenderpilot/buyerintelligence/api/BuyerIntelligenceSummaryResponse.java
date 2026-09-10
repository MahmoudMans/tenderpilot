package com.tenderpilot.buyerintelligence.api;

import com.tenderpilot.buyerintelligence.domain.DeadlineTempo;
import com.tenderpilot.buyerintelligence.domain.SupplierConcentration;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.UUID;

public record BuyerIntelligenceSummaryResponse(

        UUID buyerId,

        String name,

        String category,

        String countryCode,

        String city,

        int noticeCount,

        int activeNoticeCount,

        int awardCount,

        BigDecimal totalAwardedValue,

        String awardCurrency,

        BigDecimal averageEstimatedValue,

        String estimatedCurrency,

        LocalDate lastPublicationDate,

        boolean recurringProcurement,

        int topCpvSharePercentage,

        SupplierConcentration supplierConcentration,

        int topSupplierSharePercentage,

        int supplierHhi,

        Integer medianResponseDays,

        DeadlineTempo deadlineTempo

) {
}
