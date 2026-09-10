package com.tenderpilot.supplierintelligence.api;

import com.tenderpilot.supplierintelligence.application.SupplierIntelligenceService;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/suppliers"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class SupplierIntelligenceController {

    private final SupplierIntelligenceService
            intelligenceService;


    public SupplierIntelligenceController(
            SupplierIntelligenceService intelligenceService
    ) {

        this.intelligenceService =
                intelligenceService;
    }


    @GetMapping
    public List<SupplierIntelligenceSummaryResponse> suppliers(

            @RequestParam(
                    required = false
            )
            String q,

            @RequestParam(
                    required = false
            )
            String country,

            @RequestParam(
                    required = false
            )
            Integer limit

    ) {

        return intelligenceService
                .search(
                        q,
                        country,
                        limit
                );
    }


    @GetMapping(
            "/{supplierKey}"
    )
    public SupplierIntelligenceDetailResponse supplier(
            @PathVariable
            String supplierKey
    ) {

        return intelligenceService
                .detail(
                        supplierKey
                );
    }

}
