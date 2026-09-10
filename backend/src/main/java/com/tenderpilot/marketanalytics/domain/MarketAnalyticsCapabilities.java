package com.tenderpilot.marketanalytics.domain;

public record MarketAnalyticsCapabilities(

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
