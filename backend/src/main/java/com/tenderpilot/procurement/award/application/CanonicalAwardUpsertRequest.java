package com.tenderpilot.procurement.award.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CanonicalAwardUpsertRequest(

        LocalDate notificationDate,

        BigDecimal awardedAmount,

        String currency,

        Integer durationMonths,

        String priceForm,

        Integer offersReceived,

        String frameworkAgreementId,

        String socialConsiderations,

        String environmentalConsiderations,

        String executionModalities,

        String operatorGroupType,

        String subcontractingDeclared,

        List<SupplierInput> suppliers

) {

    public record SupplierInput(
            String identifier,
            String identifierType,
            String legalName
    ) {
    }
}
