package com.tenderpilot.company.api;

import com.tenderpilot.company.domain.AnnualRevenueBand;
import com.tenderpilot.company.domain.BuyerType;
import com.tenderpilot.company.domain.EmployeeBand;

import jakarta.validation.Valid;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateCompanyProfileRequest(

        @NotBlank
        @Size(
                max = 220
        )
        String legalName,


        @Size(
                max = 220
        )
        String tradingName,


        @NotBlank
        @Size(
                max = 180
        )
        String industry,


        @NotBlank
        @Size(
                max = 3000
        )
        String description,


        @Size(
                max = 500
        )
        String websiteUrl,


        @NotBlank
        @Pattern(
                regexp = "(?i)[A-Z]{2}",
                message = "Country code must contain exactly two letters"
        )
        String countryCode,


        @NotBlank
        @Size(
                max = 160
        )
        String headquartersCity,


        @NotNull
        EmployeeBand employeeBand,


        @NotNull
        AnnualRevenueBand annualRevenueBand,


        @DecimalMin(
                value = "0.0"
        )
        BigDecimal preferredMinContractValue,


        @DecimalMin(
                value = "0.0"
        )
        BigDecimal preferredMaxContractValue,


        @Pattern(
                regexp = "(?i)[A-Z]{3}",
                message = "Currency must contain exactly three letters"
        )
        String preferredCurrency,


        @Min(
                0
        )
        @Max(
                250
        )
        Integer yearsExperience,


        @Min(
                0
        )
        @Max(
                365
        )
        Integer minimumNoticeDays,


        @NotNull
        @Size(
                max = 50
        )
        List<
                @NotBlank
                @Size(max = 180)
                String
                > capabilities,


        @NotNull
        @Size(
                max = 30
        )
        List<
                @Valid
                OperatingRegionInput
                > operatingRegions,


        @NotNull
        @Size(
                max = 100
        )
        List<
                @Valid
                TargetCpvInput
                > targetCpvs,


        @NotNull
        @Size(
                max = 50
        )
        List<
                @Valid
                CertificationInput
                > certifications,


        @NotNull
        @Size(
                max = 50
        )
        List<
                @Valid
                ReferenceProjectInput
                > referenceProjects,


        @NotNull
        @Size(
                max = 20
        )
        List<BuyerType> preferredBuyerTypes,


        @NotNull
        @Size(
                max = 50
        )
        List<
                @NotBlank
                @Size(max = 180)
                String
                > excludedKeywords

) {

    public record OperatingRegionInput(

            @NotBlank
            @Pattern(
                    regexp = "(?i)[A-Z]{2}"
            )
            String countryCode,


            @NotBlank
            @Size(
                    max = 40
            )
            String regionCode,


            @NotBlank
            @Size(
                    max = 160
            )
            String regionName

    ) {
    }


    public record TargetCpvInput(

            @NotBlank
            @Pattern(
                    regexp = "\\d{8}",
                    message = "CPV code must contain exactly 8 digits"
            )
            String code,


            @NotBlank
            @Size(
                    max = 300
            )
            String label

    ) {
    }


    public record CertificationInput(

            @NotBlank
            @Size(
                    max = 180
            )
            String name,


            @NotBlank
            @Size(
                    max = 180
            )
            String issuer,


            @Size(
                    max = 160
            )
            String certificateNumber,


            LocalDate validFrom,


            LocalDate expiresOn

    ) {
    }


    public record ReferenceProjectInput(

            @NotBlank
            @Size(
                    max = 220
            )
            String projectName,


            @NotBlank
            @Size(
                    max = 220
            )
            String clientName,


            @NotBlank
            @Pattern(
                    regexp = "(?i)[A-Z]{2}"
            )
            String countryCode,


            @Size(
                    max = 160
            )
            String regionName,


            @DecimalMin(
                    value = "0.0"
            )
            BigDecimal contractValue,


            @Pattern(
                    regexp = "(?i)[A-Z]{3}"
            )
            String currency,


            LocalDate startedOn,


            LocalDate completedOn,


            @NotBlank
            @Size(
                    max = 3000
            )
            String description

    ) {
    }

}
