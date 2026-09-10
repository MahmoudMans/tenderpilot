package com.tenderpilot.supplierintelligence.domain;

public record SupplierLandscapeMetrics(

        int topBuyerSharePercentage,

        int buyerHhi,

        BuyerConcentration buyerConcentration,

        int topCpvSharePercentage,

        CpvSpecialization cpvSpecialization,

        boolean repeatBuyerSignal

) {
}
