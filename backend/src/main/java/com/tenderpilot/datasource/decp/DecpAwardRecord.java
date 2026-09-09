package com.tenderpilot.datasource.decp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DecpAwardRecord(

        String externalId,

        String nature,

        String object,

        String cpvCode,

        String procedure,

        String buyerId,

        String buyerName,

        Integer durationMonths,

        LocalDate notificationDate,

        LocalDate publicationDate,

        BigDecimal awardedAmount,

        String priceForm,

        Integer offersReceived,

        String frameworkAgreementId,

        String executionPlaceCode,

        String executionPlaceType,

        String executionPlaceName,

        String socialConsiderations,

        String environmentalConsiderations,

        String executionModalities,

        String operatorGroupType,

        String subcontractingDeclared,

        String upstreamSource,

        List<DecpSupplierData> suppliers

) {
}
