package com.tenderpilot.matching.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CompanyMatchProfile(

        UUID organizationId,

        List<String> capabilities,

        List<OperatingRegion> operatingRegions,

        List<String> targetCpvs,

        List<String> preferredBuyerTypes,

        List<String> excludedKeywords,

        BigDecimal preferredMinimumValue,

        BigDecimal preferredMaximumValue,

        String preferredCurrency,

        Integer minimumNoticeDays

) {

    public boolean hasCoreSignals() {

        return !capabilities.isEmpty()
                ||
                !operatingRegions.isEmpty()
                ||
                !targetCpvs.isEmpty();
    }


    public record OperatingRegion(

            String countryCode,

            String regionCode,

            String regionName

    ) {
    }

}
