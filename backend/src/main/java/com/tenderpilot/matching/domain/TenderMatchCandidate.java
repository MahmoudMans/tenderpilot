package com.tenderpilot.matching.domain;

import java.math.BigDecimal;

import java.time.Instant;

import java.util.List;
import java.util.UUID;

public record TenderMatchCandidate(

        UUID tenderId,

        String title,

        String description,

        String buyerCategory,

        String countryCode,

        String regionCode,

        String regionName,

        BigDecimal estimatedMinimumValue,

        BigDecimal estimatedMaximumValue,

        String currency,

        Instant submissionDeadline,

        List<String> cpvCodes,

        List<String> cpvLabels,

        Integer dataQualityScore

) {
}
