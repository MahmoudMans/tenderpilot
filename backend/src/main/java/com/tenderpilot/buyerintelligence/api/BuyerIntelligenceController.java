package com.tenderpilot.buyerintelligence.api;

import com.tenderpilot.buyerintelligence.application.BuyerIntelligenceService;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/buyers"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class BuyerIntelligenceController {

    private final BuyerIntelligenceService
            intelligenceService;


    public BuyerIntelligenceController(
            BuyerIntelligenceService intelligenceService
    ) {

        this.intelligenceService =
                intelligenceService;
    }


    @GetMapping
    public List<BuyerIntelligenceSummaryResponse> buyers(

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
            "/{buyerId}"
    )
    public BuyerIntelligenceDetailResponse buyer(
            @PathVariable
            UUID buyerId
    ) {

        return intelligenceService
                .detail(
                        buyerId
                );
    }

}
