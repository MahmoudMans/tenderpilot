package com.tenderpilot.procurement.quality.api;

import com.tenderpilot.procurement.quality.application.ProcurementDataQualityService;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/data-quality"
)
public class ProcurementDataQualityController {

    private final ProcurementDataQualityService
            qualityService;


    public ProcurementDataQualityController(
            ProcurementDataQualityService qualityService
    ) {

        this.qualityService =
                qualityService;
    }


    @GetMapping(
            "/overview"
    )
    public DataQualityOverviewResponse overview() {

        return qualityService
                .overview();
    }


    @PostMapping(
            "/recalculate"
    )
    @PreAuthorize(
            "hasRole('OWNER')"
    )
    public DataQualityRecalculationResponse
    recalculate() {

        return new DataQualityRecalculationResponse(
                qualityService
                        .recalculateAll()
        );
    }

}
