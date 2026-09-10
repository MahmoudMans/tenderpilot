package com.tenderpilot.marketanalytics.domain;

public record MarketStructureMetrics(

        int topBuyerSharePercentage,

        int buyerHhi,

        MarketConcentration buyerConcentration,

        int topSupplierSharePercentage,

        int supplierHhi,

        MarketConcentration supplierConcentration

) {
}
