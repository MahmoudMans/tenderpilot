package com.tenderpilot.procurement.api;

import com.tenderpilot.procurement.domain.ContractNature;
import com.tenderpilot.procurement.domain.ProcurementProcedure;
import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OpportunitySummaryResponse(

        UUID id,

        String title,

        String buyerName,

        ProcurementStatus status,

        ProcurementProcedure procedureType,

        ContractNature contractNature,

        LocalDate publicationDate,

        Instant submissionDeadline,

        BigDecimal estimatedMinValue,

        BigDecimal estimatedMaxValue,

        String currency,

        String countryCode,

        String primaryRegionName,

        List<CpvSummary> cpvs,

        List<ProcurementSource> sources,

        int lotCount

) {

    public record CpvSummary(

            String code,

            String label

    ) {
    }

}
