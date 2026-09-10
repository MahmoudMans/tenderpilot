package com.tenderpilot.supplierintelligence.domain;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.UUID;

public record SupplierAwardObservation(

        String supplierName,

        UUID awardId,

        UUID tenderId,

        UUID buyerId,

        String buyerName,

        String buyerCountryCode,

        String buyerCity,

        String tenderTitle,

        String tenderStatus,

        LocalDate publicationDate,

        BigDecimal awardedAmount,

        String awardCurrency

) {
}
