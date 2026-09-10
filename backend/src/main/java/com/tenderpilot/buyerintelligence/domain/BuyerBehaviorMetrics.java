package com.tenderpilot.buyerintelligence.domain;

public record BuyerBehaviorMetrics(

        int topSupplierSharePercentage,

        int supplierHhi,

        SupplierConcentration supplierConcentration,

        int topCpvSharePercentage,

        boolean recurringProcurement,

        Integer medianResponseDays,

        DeadlineTempo deadlineTempo

) {
}
