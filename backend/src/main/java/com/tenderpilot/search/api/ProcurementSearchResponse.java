package com.tenderpilot.search.api;

import com.tenderpilot.procurement.api.OpportunitySummaryResponse;

import java.util.List;

public record ProcurementSearchResponse(

        String engine,

        boolean fallback,

        long total,

        List<Hit> hits

) {

    public record Hit(

            Double score,

            OpportunitySummaryResponse opportunity

    ) {
    }

}
