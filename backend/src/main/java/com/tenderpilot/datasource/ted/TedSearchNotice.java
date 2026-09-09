package com.tenderpilot.datasource.ted;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;

public record TedSearchNotice(

        String externalId,

        String externalVersion,

        String noticeType,

        String title,

        String description,

        LocalDate publicationDate,

        TedBuyerData buyer,

        String procedureType,

        String contractNature,

        List<LocalDate> deadlineDates,

        LocalTime deadlineTime,

        BigDecimal estimatedValue,

        String currency,

        List<String> cpvCodes,

        List<TedLocationData> locations,

        boolean frameworkAgreement,

        boolean electronicSubmission,

        String sourceUrl

) {
}
