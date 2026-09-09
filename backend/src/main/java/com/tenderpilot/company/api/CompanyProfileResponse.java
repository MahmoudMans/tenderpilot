package com.tenderpilot.company.api;

import com.tenderpilot.company.domain.AnnualRevenueBand;
import com.tenderpilot.company.domain.BuyerType;
import com.tenderpilot.company.domain.EmployeeBand;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CompanyProfileResponse(

        boolean exists,

        UUID organizationId,

        UUID profileId,

        String legalName,

        String tradingName,

        String industry,

        String description,

        String websiteUrl,

        String countryCode,

        String headquartersCity,

        EmployeeBand employeeBand,

        AnnualRevenueBand annualRevenueBand,

        BigDecimal preferredMinContractValue,

        BigDecimal preferredMaxContractValue,

        String preferredCurrency,

        Integer yearsExperience,

        Integer minimumNoticeDays,

        List<String> capabilities,

        List<OperatingRegionResponse> operatingRegions,

        List<TargetCpvResponse> targetCpvs,

        List<CertificationResponse> certifications,

        List<ReferenceProjectResponse> referenceProjects,

        List<BuyerType> preferredBuyerTypes,

        List<String> excludedKeywords,

        ProfileCompletenessResponse completeness,

        Instant createdAt,

        Instant updatedAt

) {

    public static CompanyProfileResponse empty(

            UUID organizationId,

            ProfileCompletenessResponse completeness

    ) {

        return new CompanyProfileResponse(

                false,

                organizationId,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                null,

                List.of(),

                List.of(),

                List.of(),

                List.of(),

                List.of(),

                List.of(),

                List.of(),

                completeness,

                null,

                null

        );

    }


    public record OperatingRegionResponse(

            String countryCode,

            String regionCode,

            String regionName

    ) {
    }


    public record TargetCpvResponse(

            String code,

            String label

    ) {
    }


    public record CertificationResponse(

            String name,

            String issuer,

            String certificateNumber,

            LocalDate validFrom,

            LocalDate expiresOn,

            String validityStatus

    ) {
    }


    public record ReferenceProjectResponse(

            String projectName,

            String clientName,

            String countryCode,

            String regionName,

            BigDecimal contractValue,

            String currency,

            LocalDate startedOn,

            LocalDate completedOn,

            String description

    ) {
    }


    public record ProfileCompletenessResponse(

            int score,

            List<CompletenessSectionResponse> sections

    ) {
    }


    public record CompletenessSectionResponse(

            String key,

            String label,

            int points,

            int maximumPoints

    ) {
    }

}
