package com.tenderpilot.procurement.api;

import com.tenderpilot.procurement.domain.BuyerCategory;
import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OpportunityDetailResponse(

        UUID id,

        String canonicalKey,

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

        BuyerResponse buyer,

        List<CpvResponse> cpvs,

        List<LocationResponse> locations,

        List<LotResponse> lots,

        List<SourceResponse> sources

) {

    public record BuyerResponse(

            UUID id,

            String legalName,

            BuyerCategory category,

            String countryCode,

            String city,

            String registrationNumber,

            String websiteUrl

    ) {
    }


    public record CpvResponse(

            String code,

            String label

    ) {
    }


    public record LocationResponse(

            String countryCode,

            String regionCode,

            String regionName,

            String city

    ) {
    }


    public record LotResponse(

            UUID id,

            String lotNumber,

            String title,

            String description,

            BigDecimal estimatedMinValue,

            BigDecimal estimatedMaxValue,

            String currency,

            List<CpvResponse> cpvs

    ) {
    }


    public record SourceResponse(

            ProcurementSource source,

            String externalId,

            String externalVersion,

            String noticeType,

            String sourceUrl,

            String payloadChecksum,

            Instant sourcePublishedAt,

            Instant firstSeenAt,

            Instant lastSeenAt

    ) {
    }

}
