package com.tenderpilot.company.api;

import com.tenderpilot.company.domain.BuyerType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MatchingProfileResponse(

        UUID organizationId,

        UUID profileId,

        int completenessScore,

        boolean readyForMatching,

        String industry,

        String countryCode,

        BigDecimal preferredMinContractValue,

        BigDecimal preferredMaxContractValue,

        String preferredCurrency,

        Integer yearsExperience,

        Integer minimumNoticeDays,

        List<String> capabilities,

        List<CompanyProfileResponse.OperatingRegionResponse>
                operatingRegions,

        List<CompanyProfileResponse.TargetCpvResponse>
                targetCpvs,

        List<String> certificationNames,

        List<ReferenceSignal> referenceProjects,

        List<BuyerType> preferredBuyerTypes,

        List<String> excludedKeywords

) {

    public record ReferenceSignal(

            String projectName,

            String clientName,

            String countryCode,

            String regionName,

            BigDecimal contractValue,

            String currency,

            String description

    ) {
    }

}
