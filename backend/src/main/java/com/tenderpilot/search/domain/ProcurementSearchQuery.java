package com.tenderpilot.search.domain;

import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import com.tenderpilot.procurement.quality.domain.DataQualityGrade;

public record ProcurementSearchQuery(

        String text,

        ProcurementStatus status,

        ProcurementSource source,

        ContractNature contractNature,

        String countryCode,

        String region,

        Integer minimumQualityScore,

        DataQualityGrade qualityGrade,

        Integer size

) {
}
