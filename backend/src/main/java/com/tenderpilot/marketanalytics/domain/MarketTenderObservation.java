package com.tenderpilot.marketanalytics.domain;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.UUID;

public record MarketTenderObservation(

        UUID tenderId,

        UUID buyerId,

        String buyerName,

        String buyerCategory,

        String buyerCountryCode,

        String title,

        String status,

        String procedure,

        String contractNature,

        LocalDate publicationDate,

        Instant submissionDeadline,

        BigDecimal estimatedMinValue,

        BigDecimal estimatedMaxValue,

        String currency

) {
}
