package com.tenderpilot.dashboard.domain;

public record DashboardMarketSignal(

        boolean available,

        int noticeCount,

        int activeNoticeCount,

        int uniqueBuyerCount,

        int uniqueObservedSupplierCount,

        String buyerConcentration,

        String supplierConcentration,

        String leadingCpv,

        Integer leadingCpvSharePercentage,

        int awardCoveragePercentage

) {

    public static DashboardMarketSignal unavailable() {

        return new DashboardMarketSignal(
                false,
                0,
                0,
                0,
                0,
                "UNKNOWN",
                "UNKNOWN",
                null,
                null,
                0
        );
    }

}
