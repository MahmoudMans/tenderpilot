package com.tenderpilot.datasource.boamp;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;

import java.util.List;

public record BoampNotice(

        String externalId,

        String internalId,

        String object,

        LocalDate publicationDate,

        Instant responseDeadline,

        String buyerName,

        String procedure,

        String nature,

        String noticeType,

        String sourceSchema,

        String sourceUrl,

        String departmentCode,

        List<String> cpvCodes,

        BigDecimal estimatedValue,

        String currency

) {
}
