package com.tenderpilot.marketanalytics.api;

import com.tenderpilot.marketanalytics.application.MarketAnalyticsService;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/market"
)
@PreAuthorize(
        "isAuthenticated()"
)
public class MarketAnalyticsController {

    private final MarketAnalyticsService
            marketAnalyticsService;


    public MarketAnalyticsController(
            MarketAnalyticsService marketAnalyticsService
    ) {

        this.marketAnalyticsService =
                marketAnalyticsService;
    }


    @GetMapping(
            "/explorer"
    )
    public MarketExplorerResponse explorer(

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
            String region,

            @RequestParam(
                    required = false
            )
            String cpv,

            @RequestParam(
                    required = false
            )
            String status,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateFrom,

            @RequestParam(
                    required = false
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateTo

    ) {

        return marketAnalyticsService
                .explore(
                        q,
                        country,
                        region,
                        cpv,
                        status,
                        dateFrom,
                        dateTo
                );
    }

}
