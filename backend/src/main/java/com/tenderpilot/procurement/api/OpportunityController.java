package com.tenderpilot.procurement.api;

import com.tenderpilot.procurement.application.OpportunityQueryService;

import com.tenderpilot.procurement.domain.ProcurementSource;
import com.tenderpilot.procurement.domain.ProcurementStatus;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/opportunities"
)
public class OpportunityController {

    private final OpportunityQueryService
            opportunityQueryService;


    public OpportunityController(
            OpportunityQueryService opportunityQueryService
    ) {

        this.opportunityQueryService =
                opportunityQueryService;

    }


    @GetMapping
    public List<OpportunitySummaryResponse> list(

            @RequestParam(
                    required = false
            )
            String q,


            @RequestParam(
                    required = false
            )
            ProcurementStatus status,


            @RequestParam(
                    required = false
            )
            ProcurementSource source

    ) {

        return opportunityQueryService
                .list(

                        q,

                        status,

                        source

                );

    }


    @GetMapping(
            "/{id}"
    )
    public OpportunityDetailResponse detail(

            @PathVariable
            UUID id

    ) {

        return opportunityQueryService
                .detail(
                        id
                );

    }

}
