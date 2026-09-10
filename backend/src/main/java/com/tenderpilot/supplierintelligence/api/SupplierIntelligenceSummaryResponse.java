package com.tenderpilot.supplierintelligence.api;

import com.tenderpilot.supplierintelligence.domain.BuyerConcentration;
import com.tenderpilot.supplierintelligence.domain.CpvSpecialization;

import java.math.BigDecimal;

import java.time.LocalDate;

public record SupplierIntelligenceSummaryResponse(

        String supplierKey,

        String name,

        int awardCount,

        int uniqueBuyerCount,

        int uniqueCountryCount,

        BigDecimal associatedAwardValue,

        BigDecimal averageAssociatedAwardValue,

        String awardCurrency,

        LocalDate lastObservedPublicationDate,

        int topBuyerSharePercentage,

        int buyerHhi,

        BuyerConcentration buyerConcentration,

        boolean repeatBuyerSignal,

        int topCpvSharePercentage,

        CpvSpecialization cpvSpecialization

) {
}
