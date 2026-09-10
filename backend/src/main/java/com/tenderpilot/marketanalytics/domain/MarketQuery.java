package com.tenderpilot.marketanalytics.domain;

import java.time.LocalDate;

public record MarketQuery(

        String query,

        String country,

        String region,

        String cpvPrefix,

        String status,

        LocalDate dateFrom,

        LocalDate dateTo

) {
}
