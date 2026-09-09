package com.tenderpilot.procurement.application;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CanonicalTenderUpsertRequest(

        ProcurementSource source,

        String externalId,

        String externalVersion,

        String noticeType,

        String sourceUrl,

        String payloadChecksum,

        Instant sourcePublishedAt,

        BuyerInput buyer,

        String title,

        String description,

        ProcurementStatus status,

        ProcurementProcedure procedureType,

        ContractNature contractNature,

        LocalDate publicationDate,

        Instant submissionDeadline,

        BigDecimal estimatedMinValue,

        BigDecimal estimatedMaxValue,

        String currency,

        String countryCode,

        String primaryRegionCode,

        String primaryRegionName,

        boolean frameworkAgreement,

        boolean jointProcurement,

        boolean electronicSubmission,

        List<CpvInput> cpvs,

        List<LocationInput> locations,

        List<LotInput> lots

) {

    public record BuyerInput(

            String canonicalKey,

            String legalName,

            BuyerCategory category,

            String countryCode,

            String city,

            String registrationNumber,

            String websiteUrl

    ) {
    }


    public record CpvInput(

            String code,

            String label

    ) {
    }


    public record LocationInput(

            String countryCode,

            String regionCode,

            String regionName,

            String city

    ) {
    }


    public record LotInput(

            String lotNumber,

            String title,

            String description,

            BigDecimal estimatedMinValue,

            BigDecimal estimatedMaxValue,

            String currency,

            List<CpvInput> cpvs

    ) {
    }

}
